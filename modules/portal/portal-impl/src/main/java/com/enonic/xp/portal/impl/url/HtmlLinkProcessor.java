package com.enonic.xp.portal.impl.url;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.AttachmentUrlParams;
import com.enonic.xp.portal.url.ImageUrlParams;
import com.enonic.xp.portal.url.PageUrlParams;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.site.Site;
import com.enonic.xp.style.ImageStyle;
import com.enonic.xp.style.StyleDescriptorService;
import com.enonic.xp.style.StyleDescriptors;

public class HtmlLinkProcessor
{
    private static final ApplicationKey SYSTEM_APPLICATION_KEY = ApplicationKey.from( "com.enonic.xp.app.system" );

    /*
     * Pattern Constants
     */

    private static final int MATCH_INDEX = 1;

    private static final int TAG_NAME_INDEX = MATCH_INDEX + 1;

    private static final int ATTR_INDEX = TAG_NAME_INDEX + 1;

    private static final int ATTR_VALUE_INDEX = ATTR_INDEX + 1;

    private static final int LINK_INDEX = ATTR_VALUE_INDEX + 1;

    private static final int TYPE_INDEX = LINK_INDEX + 1;

    private static final int MODE_INDEX = TYPE_INDEX + 1;

    public static final int ID_INDEX = MODE_INDEX + 1;

    private static final int PARAMS_INDEX = ID_INDEX + 1;

    public static final int NB_GROUPS = ID_INDEX;

    private static final String CONTENT_TYPE = "content";

    private static final String MEDIA_TYPE = "media";

    private static final String IMAGE_TYPE = "image";

    private static final String DOWNLOAD_MODE = "download";

    private static final String INLINE_MODE = "inline";

    /*
     * Parameters Keys
     */

    private static final String SCALE_PARAM = "scale";

    private static final String STYLE_PARAM = "style";

    /*
     * Default Values
     */

    private static final String IMAGE_SCALE = "width(768)";

    private static final int DEFAULT_WIDTH = 768;

    public static final Pattern CONTENT_PATTERN = Pattern.compile(
        "(<(\\w+)[^>]+?(href|src)=(\"((" + CONTENT_TYPE + "|" + MEDIA_TYPE + "|" + IMAGE_TYPE + ")://(?:(" + DOWNLOAD_MODE + "|" +
            INLINE_MODE + ")/)?([0-9a-z-/]+)(\\?[^\"]+)?)\"))", Pattern.MULTILINE | Pattern.UNIX_LINES );

    private static final Pattern ASPECT_RATIO_PATTEN = Pattern.compile( "^(?<horizontalProportion>\\d+):(?<verticalProportion>\\d+)$" );

    private final StyleDescriptorService styleDescriptorService;

    private final PortalUrlService portalUrlService;

    public HtmlLinkProcessor( final StyleDescriptorService styleDescriptorService, final PortalUrlService portalUrlService )
    {
        this.styleDescriptorService = styleDescriptorService;
        this.portalUrlService = portalUrlService;
    }

    public String process( final String text, final String urlType, final PortalRequest portalRequest, final List<Integer> imageWidths)
    {
        String processedHtml = text;
        final ImmutableMap<String, ImageStyle> imageStyleMap = getImageStyleMap( portalRequest );
        final Matcher contentMatcher = CONTENT_PATTERN.matcher( text );

        while ( contentMatcher.find() )
        {
            if ( contentMatcher.groupCount() >= NB_GROUPS )
            {
                final String tagName = contentMatcher.group( TAG_NAME_INDEX );
                final String attr = contentMatcher.group( ATTR_INDEX );
                final String attrValue = contentMatcher.group( ATTR_VALUE_INDEX );
                final String link = contentMatcher.group( LINK_INDEX );
                final String type = contentMatcher.group( TYPE_INDEX );
                final String mode = contentMatcher.group( MODE_INDEX );
                final String id = contentMatcher.group( ID_INDEX );
                final String urlParamsString = contentMatcher.groupCount() == PARAMS_INDEX ? contentMatcher.group( PARAMS_INDEX ) : null;

                switch ( type )
                {
                    case CONTENT_TYPE:
                        PageUrlParams pageUrlParams = new PageUrlParams().
                            type( urlType ).
                            id( id ).
                            portalRequest( portalRequest );
                        final String pageUrl = portalUrlService.pageUrl( pageUrlParams );
                        processedHtml = processedHtml.replaceFirst( Pattern.quote( attrValue ), "\"" + pageUrl + "\"" );
                        break;
                    case IMAGE_TYPE:
                        final Map<String, String> urlParams = extractUrlParams( urlParamsString );

                        ImageStyle imageStyle = getImageStyle( imageStyleMap, urlParams );

                        ImageUrlParams imageUrlParams = new ImageUrlParams().
                            type( urlType ).
                            id( id ).
                            scale( getScale( imageStyle, urlParams, null ) ).
                            filter( getFilter( imageStyle ) ).
                            portalRequest( portalRequest );

                        final String imageUrl = portalUrlService.imageUrl( imageUrlParams );

                        final StringBuilder replacement = new StringBuilder( "\"" + imageUrl + "\"" );

                        if ( "img".equals( tagName ) && "src".equals( attr ) )
                        {
                            final String srcsetValues =
                                Objects.requireNonNullElse( imageWidths, List.<Integer>of() ).stream().map( imageWidth -> {
                                    final ImageUrlParams imageParams = new ImageUrlParams().
                                        type( urlType ).
                                        id( id ).
                                        scale( getScale( imageStyle, urlParams, imageWidth ) ).
                                        filter( getFilter( imageStyle ) ).
                                        portalRequest( portalRequest );

                                    return portalUrlService.imageUrl( imageParams ) + " " + imageWidth + "w";
                                } ).collect( Collectors.joining( "," ) );

                            if ( !srcsetValues.isEmpty() )
                            {
                                replacement.append( " srcset=\"" ).append( srcsetValues ).append( "\"" );
                            }
                        }

                        processedHtml = processedHtml.replaceFirst( Pattern.quote( attrValue ), replacement.toString() );
                        break;
                    default:
                        AttachmentUrlParams attachmentUrlParams = new AttachmentUrlParams().
                            type( urlType ).
                            id( id ).
                            download( DOWNLOAD_MODE.equals( mode ) ).
                            portalRequest( portalRequest );

                        final String attachmentUrl = portalUrlService.attachmentUrl( attachmentUrlParams );

                        processedHtml = processedHtml.replaceFirst( Pattern.quote( attrValue ), "\"" + attachmentUrl + "\"" );
                        break;
                }
            }
        }
        return processedHtml;
    }

    private ImmutableMap<String, ImageStyle> getImageStyleMap( final PortalRequest portalRequest )
    {
        final ImmutableMap.Builder<String, ImageStyle> imageStyleMap = ImmutableMap.builder();
        final StyleDescriptors styleDescriptors = getStyleDescriptors( portalRequest );
        styleDescriptors.stream().
            flatMap( styleDescriptor -> styleDescriptor.getElements().stream() ).
            filter( elementStyle -> ImageStyle.STYLE_ELEMENT_NAME.equals( elementStyle.getElement() ) ).
            forEach( elementStyle -> imageStyleMap.put( elementStyle.getName(), (ImageStyle) elementStyle ) );
        return imageStyleMap.build();
    }

    private StyleDescriptors getStyleDescriptors( final PortalRequest portalRequest )
    {
        final ImmutableList.Builder<ApplicationKey> applicationKeyList = new ImmutableList.Builder<ApplicationKey>().
            add( SYSTEM_APPLICATION_KEY );
        if ( portalRequest != null )
        {
            final Site site = portalRequest.getSite();
            if ( site != null )
            {
                final ImmutableSet<ApplicationKey> siteApplicationKeySet = site.getSiteConfigs().getApplicationKeys();
                applicationKeyList.addAll( siteApplicationKeySet );
            }
        }

        final ApplicationKeys applicationKeys = ApplicationKeys.from( applicationKeyList.build() );
        return styleDescriptorService.getByApplications( applicationKeys );
    }

    private ImageStyle getImageStyle( final Map<String, ImageStyle> imageStyleMap, final Map<String, String> urlParams )
    {
        final String styleString = urlParams.get( STYLE_PARAM );
        if ( styleString != null )
        {
            return imageStyleMap.get( styleString );
        }
        return null;
    }

    private String getScale( final ImageStyle imageStyle, final Map<String, String> urlParams, final Integer expectedWidth )
    {
        final String aspectRatio = getAspectRation( imageStyle, urlParams );

        if ( aspectRatio != null )
        {
            final Matcher matcher = ASPECT_RATIO_PATTEN.matcher( aspectRatio );
            if ( !matcher.matches() )
            {
                throw new IllegalArgumentException( "Invalid aspect ratio: " + aspectRatio );
            }
            final String horizontalProportion = matcher.group( "horizontalProportion" );
            final String verticalProportion = matcher.group( "verticalProportion" );

            final int width = Objects.requireNonNullElse( expectedWidth, DEFAULT_WIDTH );
            final int height = width / Integer.parseInt( horizontalProportion ) * Integer.parseInt( verticalProportion );

            return "block(" + width + "," + height + ")";
        }

        return expectedWidth != null ? "width(" + expectedWidth + ")" : IMAGE_SCALE;
    }

    private String getFilter( final ImageStyle imageStyle )
    {
        return imageStyle == null ? null : imageStyle.getFilter();
    }

    private String getAspectRation( final ImageStyle imageStyle, final Map<String, String> urlParams )
    {
        if ( imageStyle != null )
        {
            final String aspectRatio = imageStyle.getAspectRatio();
            if ( aspectRatio != null )
            {
                return aspectRatio;
            }
        }
        return urlParams.get( SCALE_PARAM );
    }

    private Map<String, String> extractUrlParams( final String urlQuery )
    {
        if ( urlQuery == null )
        {
            return Collections.emptyMap();
        }
        final String query = urlQuery.startsWith( "?" ) ? urlQuery.substring( 1 ) : urlQuery;
        return Splitter.on( '&' ).
            trimResults().
            withKeyValueSeparator( "=" ).
            split( query.replace( "&amp;", "&" ) );
    }

}
