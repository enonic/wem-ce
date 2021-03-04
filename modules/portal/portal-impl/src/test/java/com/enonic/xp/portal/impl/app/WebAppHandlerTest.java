package com.enonic.xp.portal.impl.app;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.controller.ControllerScript;
import com.enonic.xp.portal.controller.ControllerScriptFactory;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.trace.Trace;
import com.enonic.xp.trace.TraceManager;
import com.enonic.xp.trace.Tracer;
import com.enonic.xp.web.HttpStatus;
import com.enonic.xp.web.WebResponse;
import com.enonic.xp.web.exception.ExceptionRenderer;
import com.enonic.xp.web.handler.WebHandlerChain;
import com.enonic.xp.web.impl.exception.ExceptionMapperImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebAppHandlerTest
{
    private WebAppHandler handler;

    private ControllerScriptFactory controllerScriptFactory;

    private ExceptionRenderer exceptionRenderer;

    private PortalRequest request;

    private WebHandlerChain chain;

    @BeforeEach
    public void setup()
    {
        this.controllerScriptFactory = Mockito.mock( ControllerScriptFactory.class );
        this.exceptionRenderer = Mockito.mock( ExceptionRenderer.class );

        this.handler = new WebAppHandler();
        this.handler.setControllerScriptFactory( this.controllerScriptFactory );
        this.handler.setExceptionMapper( new ExceptionMapperImpl() );
        this.handler.setExceptionRenderer( this.exceptionRenderer );

        this.request = new PortalRequest();
        this.request.setRawRequest( Mockito.mock( HttpServletRequest.class ) );
        this.chain = Mockito.mock( WebHandlerChain.class );

        Mockito.when( this.exceptionRenderer.render( Mockito.any(), Mockito.any() ) ).thenReturn(
            WebResponse.create().status( HttpStatus.INTERNAL_SERVER_ERROR ).build() );

        final TraceManager manager = Mockito.mock( TraceManager.class );
        final Trace trace = Mockito.mock( Trace.class );
        Mockito.when( manager.newTrace( Mockito.any(), Mockito.any() ) ).thenReturn( trace );
        Tracer.setManager( manager );
    }

    @AfterEach
    public void tearDown()
    {
        Tracer.setManager( null );
    }

    @Test
    public void canHandle()
    {
        this.request.setRawPath( "/site/a/b" );
        assertFalse( this.handler.canHandle( this.request ) );

        this.request.setRawPath( "/webapp/myapp" );
        assertTrue( this.handler.canHandle( this.request ) );

        this.request.setRawPath( "/webapp/myapp/a/b" );
        assertTrue( this.handler.canHandle( this.request ) );

        this.request.setRawPath( "/a/webapp/myapp/a/b" );
        assertFalse( this.handler.canHandle( this.request ) );
    }

    @Test
    public void handle_executeController()
        throws Exception
    {

        this.request.setApplicationKey( ApplicationKey.from( "myapp" ) );
        this.request.setBaseUri( "/webapp/myapp" );
        this.request.setRawPath( "/webapp/myapp/a.txt" );

        final ControllerScript script = Mockito.mock( ControllerScript.class );
        Mockito.when( this.controllerScriptFactory.fromScript( ResourceKey.from( "myapp:/webapp/webapp.js" ) ) ).thenReturn( script );

        final PortalResponse response = PortalResponse.create().build();
        Mockito.when( script.execute( Mockito.any() ) ).thenReturn( response );

        assertSame( response, this.handler.doHandle( this.request, null, this.chain ) );
        assertEquals( "/webapp/myapp", this.request.getContextPath() );
    }

    @Test
    public void handle_executeController_error()
        throws Exception
    {
        this.request.setApplicationKey( ApplicationKey.from( "myapp" ) );
        this.request.setRawPath( "/webapp/myapp/a.txt" );

        final WebResponse response = this.handler.doHandle( this.request, null, this.chain );
        assertEquals( HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus() );
    }
}

