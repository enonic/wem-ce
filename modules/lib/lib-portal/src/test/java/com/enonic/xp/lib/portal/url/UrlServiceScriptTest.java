package com.enonic.xp.lib.portal.url;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.script.ScriptExports;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.testing.ScriptTestSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlServiceScriptTest
    extends ScriptTestSupport
{
    @Override
    protected void initialize()
        throws Exception
    {
        super.initialize();
        addService( PortalUrlService.class, Mockito.mock( PortalUrlService.class, this::urlAnswer ) );
    }

    private Object urlAnswer( final InvocationOnMock invocation )
        throws Exception
    {
        return invocation.getArguments()[0].toString();
    }

    private boolean execute( final String method )
    {
        final ScriptExports exports = runScript( "/test/url-test.js" );
        final ScriptValue value = exports.executeMethod( method );
        return value != null ? value.getValue( Boolean.class ) : false;
    }

    @Test
    public void assertUrlTest()
    {
        assertTrue( execute( "assetUrlTest" ) );
    }

    @Test
    public void assertUrlTest_unknownProperty()
    {
        assertTrue( execute( "assetUrlTest_unknownProperty" ) );
    }

    @Test
    public void assertUrlTest_invalidProperty()
    {
        assertTrue( execute( "assetUrlTest_invalidProperty" ) );
    }

    @Test
    public void attachmentUrlTest()
    {
        assertTrue( execute( "attachmentUrlTest" ) );
    }

    @Test
    public void attachmentUrlTest_unknownProperty()
    {
        assertTrue( execute( "attachmentUrlTest_unknownProperty" ) );
    }

    @Test
    public void componentUrlTest()
    {
        assertTrue( execute( "componentUrlTest" ) );
    }

    @Test
    public void componentUrlTest_unknownProperty()
    {
        assertTrue( execute( "componentUrlTest_unknownProperty" ) );
    }

    @Test
    public void imageUrlTest()
    {
        assertTrue( execute( "imageUrlTest" ) );
    }

    @Test
    public void imageUrlTest_unknownProperty()
    {
        assertTrue( execute( "imageUrlTest_unknownProperty" ) );
    }

    @Test
    public void pageUrlTest()
    {
        assertTrue( execute( "pageUrlTest" ) );
    }

    @Test
    public void pageUrlTest_unknownProperty()
    {
        assertTrue( execute( "pageUrlTest_unknownProperty" ) );
    }

    @Test
    public void serviceUrlTest()
    {
        assertTrue( execute( "serviceUrlTest" ) );
    }

    @Test
    public void serviceUrlTest_unknownProperty()
    {
        assertTrue( execute( "serviceUrlTest_unknownProperty" ) );
    }

    @Test
    public void processHtmlTest()
    {
        assertTrue( execute( "processHtmlTest" ) );
    }

    @Test
    public void processHtmlTest_unknownProperty()
    {
        assertTrue( execute( "processHtmlTest_unknownProperty" ) );
    }

    @Test
    public void processHtmlTest_imageUrlProcessing()
    {
        assertTrue( execute( "processHtmlTest_imageUrlProcessing" ) );
    }

    @Test
    public void imagePlaceholderTest()
    {
        assertTrue( execute( "imagePlaceholderTest" ) );
    }

    @Test
    public void testExample_assetUrl()
    {
        runScript( "/lib/xp/examples/portal/assetUrl.js" );
    }

    @Test
    public void testExample_imageUrl()
    {
        runScript( "/lib/xp/examples/portal/imageUrl.js" );
    }

    @Test
    public void testExample_componentUrl()
    {
        runScript( "/lib/xp/examples/portal/componentUrl.js" );
    }

    @Test
    public void testExample_attachmentUrl()
    {
        runScript( "/lib/xp/examples/portal/attachmentUrl.js" );
    }

    @Test
    public void testExample_pageUrl()
    {
        runScript( "/lib/xp/examples/portal/pageUrl.js" );
    }

    @Test
    public void testExample_serviceUrl()
    {
        runScript( "/lib/xp/examples/portal/serviceUrl.js" );
    }

    @Test
    public void testExample_generateUrl()
    {
        runScript( "/lib/xp/examples/portal/url.js" );
    }

    @Test
    public void testExample_processHtml()
    {
        runScript( "/lib/xp/examples/portal/processHtml.js" );
    }

    @Test
    public void testExample_imagePlaceholder()
    {
        runScript( "/lib/xp/examples/portal/imagePlaceholder.js" );
    }
}
