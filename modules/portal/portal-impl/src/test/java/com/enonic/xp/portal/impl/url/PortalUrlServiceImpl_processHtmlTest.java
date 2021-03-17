package com.enonic.xp.portal.impl.url;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.attachment.Attachment;
import com.enonic.xp.attachment.Attachments;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.Media;
import com.enonic.xp.portal.impl.ContentFixtures;
import com.enonic.xp.portal.url.ProcessHtmlParams;
import com.enonic.xp.portal.url.UrlTypeConstants;
import com.enonic.xp.style.ImageStyle;
import com.enonic.xp.style.StyleDescriptor;
import com.enonic.xp.style.StyleDescriptors;
import com.enonic.xp.web.servlet.ServletRequestHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortalUrlServiceImpl_processHtmlTest
    extends AbstractPortalUrlServiceImplTest
{
    @BeforeEach
    public void before()
    {

    }

    @Test
    public void process_empty_value()
    {
        //Checks the process for a null value
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest );
        String processedHtml = this.service.processHtml( params );
        assertEquals( "", processedHtml );

        //Checks the process for an empty string value
        params.value( "" );
        processedHtml = this.service.processHtml( params );
        assertEquals( "", processedHtml );
    }

    @Test
    public void process_single_content()
    {
        //Creates a content
        final Content content = ContentFixtures.newContent();
        Mockito.when( this.contentService.getById( content.getId() ) ).thenReturn( content );

        //Process an html text containing a link to this content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"content://" + content.getId() + "\">Content</a>" );

        //Checks that the page URL of the content is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft" + content.getPath() + "\">Content</a>", processedHtml );
    }

    @Test
    public void process_single_image()
    {
        //Creates a content
        final Media media = ContentFixtures.newMedia();
        Mockito.when( this.contentService.getById( media.getId() ) ).thenReturn( media );
        Mockito.when( this.contentService.getBinaryKey( media.getId(), media.getMediaAttachment().getBinaryReference() ) ).thenReturn(
            "binaryHash" );

        //Process an html text containing a link to this content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"image://" + media.getId() + "\">Image</a>" );

        //Checks that the page URL of the content is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft/context/path/_/image/" + media.getId() + ":8cf45815bba82c9711c673c9bb7304039a790026/" +
                          "width-768" + "/" + media.getName() + "\">Image</a>", processedHtml );
    }

    @Test
    public void process_single_media()
    {
        //Creates a content with attachments
        final Attachment thumb = Attachment.
            create().
            label( "thumb" ).
            name( "a1.jpg" ).
            mimeType( "image/jpg" ).
            build();
        final Attachment source = Attachment.
            create().
            label( "source" ).
            name( "a2.jpg" ).
            mimeType( "image/jpg" ).
            build();
        final Attachments attachments = Attachments.from( thumb, source );
        final Content content = Content.
            create( ContentFixtures.newContent() ).
            attachments( attachments ).
            build();
        Mockito.when( this.contentService.getById( content.getId() ) ).thenReturn( content );
        Mockito.when( this.contentService.getBinaryKey( content.getId(), source.getBinaryReference() ) ).thenReturn( "binaryHash2" );

        //Process an html text containing an inline link to this content
        ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"media://inline/" + content.getId() + "\">Media</a>" );

        //Checks that the URL of the source attachment of the content is returned
        String processedHtml = this.service.processHtml( params );
        assertEquals(
            "<a href=\"/site/default/draft/context/path/_/attachment/inline/" + content.getId() + ":binaryHash2/" + source.getName() +
                "\">Media</a>", processedHtml );

        //Process an html text containing a download link to this content
        params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"media://download/" + content.getId() + "\">Media</a>" );

        //Checks that the URL of the source attachment of the content is returned
        processedHtml = this.service.processHtml( params );
        assertEquals(
            "<a href=\"/site/default/draft/context/path/_/attachment/download/" + content.getId() + ":binaryHash2/" + source.getName() +
                "\">Media</a>", processedHtml );

        //Process an html text containing an inline link to this content in a img tag
        params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"/some/page\"><img src=\"media://inline/" + content.getId() + "\">Media</a>" );

        //Checks that the URL of the source attachment of the content is returned
        processedHtml = this.service.processHtml( params );
        assertEquals(
            "<a href=\"/some/page\"><img src=\"/site/default/draft/context/path/_/attachment/inline/" + content.getId() + ":binaryHash2/" +
                source.getName() + "\">Media</a>", processedHtml );

    }

    @Test
    public void process_multiple_links()
    {
        //Creates a content with attachments
        final Attachment thumb = Attachment.
            create().
            label( "thumb" ).
            name( "a1.jpg" ).
            mimeType( "image/jpg" ).
            build();
        final Attachment source = Attachment.
            create().
            label( "source" ).
            name( "a2.jpg" ).
            mimeType( "image/jpg" ).
            build();
        final Attachments attachments = Attachments.from( thumb, source );
        final Content content = Content.
            create( ContentFixtures.newContent() ).
            attachments( attachments ).
            build();
        Mockito.when( this.contentService.getById( content.getId() ) ).thenReturn( content );
        Mockito.when( this.contentService.getBinaryKey( content.getId(), source.getBinaryReference() ) ).thenReturn( "binaryHash2" );

        //Process an html text containing multiple links, on multiple lines, to this content as a media and as a content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<p>A content link:&nbsp;<a href=\"content://" + content.getId() + "\">FirstLink</a></p>\n" +
                       "<p>A second content link:&nbsp;<a href=\"content://" + content.getId() + "\">SecondLink</a>" +
                       "&nbsp;and a download link:&nbsp;<a href=\"media://download/" + content.getId() + "\">Download</a></p>\n" +
                       "<p>An external link:&nbsp;<a href=\"http://www.enonic.com\">An external  link</a></p>\n" + "<p>&nbsp;</p>\n" +
                       "<a href=\"media://inline/" + content.getId() + "\">Inline</a>" );

        //Checks the returned value
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<p>A content link:&nbsp;<a href=\"/site/default/draft" + content.getPath() + "\">FirstLink</a></p>\n" +
                          "<p>A second content link:&nbsp;<a href=\"/site/default/draft" + content.getPath() + "\">SecondLink</a>" +
                          "&nbsp;and a download link:&nbsp;<a href=\"/site/default/draft/context/path/_/attachment/download/" +
                          content.getId() + ":binaryHash2/" + source.getName() + "\">Download</a></p>\n" +
                          "<p>An external link:&nbsp;<a href=\"http://www.enonic.com\">An external  link</a></p>\n" + "<p>&nbsp;</p>\n" +
                          "<a href=\"/site/default/draft/context/path/_/attachment/inline/" + content.getId() + ":binaryHash2/" +
                          source.getName() + "\">Inline</a>", processedHtml );
    }

    @Test
    public void process_unknown_content()
    {

        //Process an html text containing a link to an unknown content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"content://123\">Content</a>" );

        //Checks that the error 500 page is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft/context/path/_/error/500\">Content</a>", processedHtml );
    }

    @Test
    public void process_unknown_media()
    {

        //Process an html text containing a link to an unknown media
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"media://inline/123\">Media</a>" );

        //Checks that the error 500 page is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft/context/path/_/error/500\">Media</a>", processedHtml );
    }

    @Test
    public void process_unknown_image()
    {

        //Process an html text containing a link to an unknown media
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"image://123\">Image</a>" );

        //Checks that the error 500 page is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft/context/path/_/error/500\">Image</a>", processedHtml );
    }

    @Test
    public void process_absolute()
    {
        //Creates a content
        final Content content = ContentFixtures.newContent();
        Mockito.when( this.contentService.getById( content.getId() ) ).thenReturn( content );

        //Process an html text containing a link to this content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            type( UrlTypeConstants.ABSOLUTE ).
            portalRequest( this.portalRequest ).
            value( "<a href=\"content://" + content.getId() + "\">Content</a>" );

        HttpServletRequest req = mock( HttpServletRequest.class );
        when( req.getServerName() ).thenReturn( "localhost" );
        when( req.getScheme() ).thenReturn( "http" );
        when( req.getServerPort() ).thenReturn( 80 );
        ServletRequestHolder.setRequest( req );

        //Checks that the page URL of the content is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"http://localhost/site/default/draft" + content.getPath() + "\">Content</a>", processedHtml );
    }

    @Test
    public void process_image_with_scale()
    {
        //Creates a content
        final Media media = ContentFixtures.newMedia();
        Mockito.when( this.contentService.getById( media.getId() ) ).thenReturn( media );
        Mockito.when( this.contentService.getBinaryKey( media.getId(), media.getMediaAttachment().getBinaryReference() ) ).thenReturn(
            "binaryHash" );

        //Process an html text containing a link to this content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<a href=\"image://" + media.getId() + "?scale=21:9\">Image</a>" );

        //Checks that the page URL of the content is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals( "<a href=\"/site/default/draft/context/path/_/image/" + media.getId() + ":8cf45815bba82c9711c673c9bb7304039a790026/" +
                          "block-768-324" + "/" + media.getName() + "\">Image</a>", processedHtml );
    }

    @Test
    public void process_html_with_macros()
        throws IOException
    {
        assertProcessHtml( "html-with-macros-input.txt", "html-with-macros-output.txt" );
        assertProcessHtml( "html-with-unclosed-macro-input.txt", "html-with-unclosed-macro-output.txt" );
    }

    @Test
    public void process_image_with_styles()
    {
        //Creates a content
        final Media media = ContentFixtures.newMedia();
        Mockito.when( this.contentService.getById( media.getId() ) ).thenReturn( media );
        Mockito.when( this.contentService.getBinaryKey( media.getId(), media.getMediaAttachment().getBinaryReference() ) ).thenReturn(
            "binaryHash" );

        final ImageStyle imageStyle = ImageStyle.create().name( "mystyle" ).
            aspectRatio( "2:1" ).
            filter( "myfilter" ).
            build();
        final StyleDescriptor styleDescriptor = StyleDescriptor.create().
            application( ApplicationKey.from( "myapp" ) ).
            addStyleElement( imageStyle ).
            build();
        Mockito.when( styleDescriptorService.getByApplications( Mockito.any() ) ).
            thenReturn( StyleDescriptors.from( styleDescriptor ) );

        //Process an html text containing a style
        final String link1 = "<a href=\"image://" + media.getId() + "?style=mystyle\">Image</a>";
        final String link2 = "<a href=\"image://" + media.getId() + "?style=missingstyle\">Image</a>";
        final ProcessHtmlParams params1 = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( link1 );
        final ProcessHtmlParams params2 = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( link2 );
        final String processedLink1 = this.service.processHtml( params1 );
        final String processedLink2 = this.service.processHtml( params2 );

        //Checks that the page URL of the content is returned
        final String expectedResult1 =
            "<a href=\"/site/default/draft/context/path/_/image/" + media.getId() + ":8cf45815bba82c9711c673c9bb7304039a790026/" +
                "block-768-384" + "/" + media.getName() + "?filter=myfilter\">Image</a>";
        final String expectedResult2 =
            "<a href=\"/site/default/draft/context/path/_/image/" + media.getId() + ":8cf45815bba82c9711c673c9bb7304039a790026/" +
                "width-768" + "/" + media.getName() + "\">Image</a>";
        assertEquals( expectedResult1, processedLink1 );
        assertEquals( expectedResult2, processedLink2 );
    }

    @Test
    public void processHtml_image_imageWidths()
    {
        //Creates a content
        final Media media = ContentFixtures.newMedia();
        Mockito.when( this.contentService.getById( media.getId() ) ).thenReturn( media );
        Mockito.when( this.contentService.getBinaryKey( media.getId(), media.getMediaAttachment().getBinaryReference() ) ).thenReturn(
            "binaryHash" );

        //Process an html text containing a link to this content
        final ProcessHtmlParams params = new ProcessHtmlParams().
            portalRequest( this.portalRequest ).
            value( "<p><figure class=\"editor-align-justify\"><img alt=\"Alt text\" src=\"image://" + media.getId() +
                       "\"/><figcaption>Caption text</figcaption></figure></p>" ).
            imageWidths( List.of( 660, 1024 ) );

        //Checks that the page URL of the content is returned
        final String processedHtml = this.service.processHtml( params );
        assertEquals(
            "<p><figure class=\"editor-align-justify\">" + "<img alt=\"Alt text\" src=\"/site/default/draft/context/path/_/image/" +
                media.getId() + ":8cf45815bba82c9711c673c9bb7304039a790026/width-768/mycontent\" " +
                "srcset=\"/site/default/draft/context/path/_/image/" + media.getId() +
                ":8cf45815bba82c9711c673c9bb7304039a790026/width-660/mycontent 660w," + "/site/default/draft/context/path/_/image/" +
                media.getId() +
                ":8cf45815bba82c9711c673c9bb7304039a790026/width-1024/mycontent 1024w\"/><figcaption>Caption text</figcaption></figure></p>",
            processedHtml );
    }

    private void assertProcessHtml( String inputName, String expectedOutputName )
        throws IOException
    {
        final String input;
        final String expected;
        //Reads the input and output files
        try (InputStream is = this.getClass().getResourceAsStream( inputName ))
        {
            input = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
        }
        try (InputStream is = this.getClass().getResourceAsStream( expectedOutputName ))
        {
            expected = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
        }

        //Processes the input file
        final ProcessHtmlParams processHtmlParams = new ProcessHtmlParams().
            value( input );
        final String processedHtml = this.service.processHtml( processHtmlParams );

        //Checks that the processed text is equal to the expected output
        assertEquals( expected, processedHtml );
    }
}
