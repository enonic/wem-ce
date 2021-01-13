package com.enonic.xp.portal.impl.url;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.base.Splitter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.UrlEscapers;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.exception.NotFoundException;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.impl.exception.OutOfScopeException;
import com.enonic.xp.portal.url.AbstractUrlParams;
import com.enonic.xp.portal.url.UrlTypeConstants;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryUtils;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.web.servlet.ServletRequestUrlHelper;
import com.enonic.xp.web.servlet.UriRewritingResult;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

abstract class PortalUrlBuilder<T extends AbstractUrlParams>
{
    protected PortalRequest portalRequest;

    protected T params;

    protected ContentService contentService;

    protected ResourceService resourceService;

    private String getBaseUri()
    {
        return this.portalRequest.getBaseUri();
    }

    private Branch getBranch()
    {
        return this.portalRequest.getBranch();
    }

    private RepositoryId getRepositoryId()
    {
        return this.portalRequest.getRepositoryId();
    }

    public final void setParams( final T params )
    {
        this.params = params;
        this.portalRequest = this.params.getPortalRequest();
    }

    protected final void appendPart( final StringBuilder str, final String urlPart )
    {
        if ( isNullOrEmpty( urlPart ) )
        {
            return;
        }

        final boolean endsWithSlash = ( str.length() > 0 ) && ( str.charAt( str.length() - 1 ) == '/' );
        final String normalized = normalizePath( urlPart );

        if ( !endsWithSlash )
        {
            str.append( "/" );
        }

        str.append( normalized );
    }

    private void appendParams( final StringBuilder str, final Collection<Map.Entry<String, String>> params )
    {
        if ( params.isEmpty() )
        {
            return;
        }
        str.append( "?" );
        final Iterator<Map.Entry<String, String>> it = params.iterator();
        appendParam( str, it.next() );
        while ( it.hasNext() )
        {
            str.append( "&" );
            appendParam( str, it.next() );
        }
    }

    private void appendParam( final StringBuilder str, final Map.Entry<String, String> param )
    {
        appendParam( str, param.getKey(), param.getValue() );
    }

    private void appendParam( final StringBuilder str, final String key, final String value )
    {
        final String encoded = urlEncode( nullToEmpty( value ) );
        str.append( key ).append( "=" ).append( encoded );
    }

    private String urlEncode( final String value )
    {
        return UrlEscapers.urlFormParameterEscaper().escape( value );
    }

    private String urlEncodePathSegment( final String value )
    {
        // plus sign remains the same after UrlEscapers
        return UrlEscapers.urlPathSegmentEscaper().escape( value ).replaceAll( "\\+", "%2B" );
    }

    String normalizePath( final String value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( !value.contains( "/" ) )
        {
            return urlEncodePathSegment( value );
        }

        return StreamSupport.stream( Splitter.on( '/' ).trimResults().omitEmptyStrings().split( value ).spliterator(), false ).
            map( this::urlEncodePathSegment ).collect( Collectors.joining( "/" ) );
    }

    public final String build()
    {
        try
        {
            return doBuild();
        }
        catch ( final Exception e )
        {
            return buildErrorUrl( e );
        }
    }

    private String doBuild()
    {
        final StringBuilder str = new StringBuilder();
        appendPart( str, getBaseUri() );

        final Multimap<String, String> params = LinkedListMultimap.create();
        buildUrl( str, params );
        appendParams( str, params.entries() );

        final UriRewritingResult rewritingResult = ServletRequestUrlHelper.rewriteUri( str.toString() );

        if ( rewritingResult.isOutOfScope() )
        {
            throw new OutOfScopeException( "URI out of scope" );
        }

        final String uri = postUriRewriting( rewritingResult );

        if ( UrlTypeConstants.ABSOLUTE.equals( this.params.getType() ) )
        {
            return ServletRequestUrlHelper.getServerUrl() + uri;
        }
        else
        {
            return uri;
        }
    }

    @SuppressWarnings("unchecked")
    protected void buildUrl( final StringBuilder url, final Multimap<String, String> params )
    {
        params.putAll( this.params.getParams() );

        if ( isSiteBase() )
        {
            appendPart( url, RepositoryUtils.getContentRepoName( getRepositoryId() ) );
            appendPart( url, getBranch().toString() );
        }
    }

    protected String postUriRewriting( final UriRewritingResult uriRewritingResult )
    {
        return uriRewritingResult.getRewrittenUri();
    }

    protected String buildErrorUrl( final Exception e )
    {
        if ( e instanceof NotFoundException )
        {
            return buildErrorUrl( 404, e.getMessage() );
        }
        else if ( e instanceof OutOfScopeException )
        {
            return buildErrorUrl( 400, e.getMessage() );
        }
        else
        {
            return buildErrorUrl( 500, e.getMessage() );
        }
    }

    private boolean isSiteBase()
    {
        return this.portalRequest.isSiteBase();
    }

    protected final String buildErrorUrl( final int code, final String message )
    {
        final StringBuilder str = new StringBuilder();
        appendPart( str, getBaseUri() );

        if ( isSiteBase() )
        {
            appendPart( str, RepositoryUtils.getContentRepoName( getRepositoryId() ) );
            appendPart( str, getBranch().toString() );
            appendPart( str, this.portalRequest.getContentPath().toString() );
        }

        appendPart( str, "_" );
        appendPart( str, "error" );
        appendPart( str, String.valueOf( code ) );

        final Multimap<String, String> params = LinkedListMultimap.create();

        if ( message != null )
        {
            params.put( "message", message );
        }

        appendParams( str, params.entries() );
        final String uri = str.toString();
        return ServletRequestUrlHelper.rewriteUri( uri ).getRewrittenUri();
    }

    @Override
    public final String toString()
    {
        return build();
    }
}
