package com.enonic.xp.testing;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.config.ConfigBuilder;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.core.impl.app.ApplicationBuilder;
import com.enonic.xp.core.impl.app.resolver.ClassLoaderApplicationUrlResolver;
import com.enonic.xp.core.internal.Dictionaries;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalRequestAccessor;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;
import com.enonic.xp.script.ScriptExports;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.executor.ScriptExecutor;
import com.enonic.xp.script.impl.executor.ScriptExecutorImpl;
import com.enonic.xp.script.runtime.ScriptSettings;
import com.enonic.xp.server.RunMode;
import com.enonic.xp.testing.mock.MockBeanContext;
import com.enonic.xp.testing.mock.MockServiceRegistry;
import com.enonic.xp.testing.mock.MockViewFunctionService;
import com.enonic.xp.testing.resource.ClassLoaderResourceService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ScriptTestSupport
{
    private ApplicationKey appKey;

    private String appVersion;

    protected PortalRequest portalRequest;

    protected ContentService contentService;

    private ResourceService resourceService;

    private ScriptSettings.Builder scriptSettings;

    private BundleContext bundleContext;

    private MockServiceRegistry serviceRegistry;

    private ScriptExecutor executor;

    public ScriptTestSupport()
    {
        setAppKey( "myapplication" );
        setAppVersion( "1.0.0" );
    }

    protected final void setAppKey( final String value )
    {
        this.appKey = ApplicationKey.from( value );
    }

    protected final void setAppVersion( final String value )
    {
        this.appVersion = value;
    }

    @Before
    @BeforeEach
    public final void setup()
        throws Exception
    {
        initialize();
    }

    protected void initialize()
        throws Exception
    {
        this.serviceRegistry = new MockServiceRegistry();

        this.portalRequest = createPortalRequest();
        PortalRequestAccessor.set( this.portalRequest );

        this.bundleContext = createBundleContext();
        this.contentService = Mockito.mock( ContentService.class );
        this.resourceService = createResourceService();

        addService( ContentService.class, this.contentService );
        addService( ResourceService.class, this.resourceService );
        addService( ViewFunctionService.class, new MockViewFunctionService() );

        this.scriptSettings = ScriptSettings.create();
        this.scriptSettings.binding( Context.class, ContextAccessor::current );
        this.scriptSettings.binding( PortalRequest.class, () -> this.portalRequest );
        this.scriptSettings.debug( new ScriptDebugSettings() );
        this.scriptSettings.globalVariable( "testInstance", this );

        this.executor = createExecutor();
    }

    protected final <T> void addService( final Class<T> type, final T instance )
    {
        this.serviceRegistry.register( type, instance );
    }

    protected final <T> void addBinding( final Class<T> type, final T instance )
    {
        this.scriptSettings.binding( type, () -> instance );
    }

    protected final void addGlobalVariable( final String name, final Object value )
    {
        this.scriptSettings.globalVariable( name, value );
    }

    protected final PortalRequest getPortalRequest()
    {
        return this.portalRequest;
    }

    protected final MockBeanContext newBeanContext( final ResourceKey key )
    {
        return new MockBeanContext( key, this.serviceRegistry );
    }

    protected PortalRequest createPortalRequest()
    {
        final PortalRequest request = new PortalRequest();

        request.setMode( RenderMode.LIVE );
        request.setBranch( Branch.from( "draft" ) );
        request.setApplicationKey( this.appKey );
        request.setBaseUri( "/site" );

        final Content content = Content.create().id( ContentId.from( "123" ) ).path( "some/path" ).build();
        request.setContent( content );
        return request;
    }

    private ResourceService createResourceService()
    {
        return new ClassLoaderResourceService( getClass().getClassLoader() );
    }

    protected final Resource loadResource( final ResourceKey key )
    {
        final URL url = findResource( key.getPath() );
        return new UrlResource( key, url );
    }

    private URL findResource( final String path )
    {
        return getClass().getResource( path );
    }

    public final ScriptExports runScript( final String path )
    {
        final ResourceKey key = ResourceKey.from( this.appKey, path );
        return runScript( key );
    }

    public final ScriptExports runScript( final ResourceKey key )
    {
        return this.executor.executeMain( key );
    }

    protected final ScriptValue runFunction( final String path, final String funcName, final Object... funcParams )
    {
        final ScriptExports exports = runScript( path );

        assertNotNull( exports, "No exports in [" + path + "]" );
        assertTrue( exports.hasMethod( funcName ), "No functions exported named [" + funcName + "] in [" + path + "]" );
        return exports.executeMethod( funcName, funcParams );
    }

    private ScriptExecutor createExecutor()
        throws Exception
    {
        return new ScriptExecutorImpl( Executors.newSingleThreadExecutor(), this.scriptSettings.build(), getClass().getClassLoader(),
                                       this.serviceRegistry, this.resourceService, createApplication(), RunMode.DEV );
    }

    private Application createApplication()
        throws Exception
    {
        final ApplicationBuilder builder = new ApplicationBuilder();
        builder.classLoader( getClass().getClassLoader() );
        builder.urlResolver( ClassLoaderApplicationUrlResolver.create( Path.of( "src/test/resources" ).toUri().toURL() ) );
        builder.config( ConfigBuilder.create().build() );
        builder.bundle( createBundle() );
        return builder.build();
    }

    private Bundle createBundle()
    {
        final Bundle bundle = Mockito.mock( Bundle.class );

        Mockito.lenient().when( bundle.getBundleContext() ).thenReturn( this.bundleContext );
        Mockito.when( bundle.getSymbolicName() ).thenReturn( this.appKey.getName() );
        Mockito.when( bundle.getVersion() ).thenReturn( Version.valueOf( this.appVersion ) );
        Mockito.lenient().when( bundle.getState() ).thenReturn( Bundle.ACTIVE );

        Mockito.when( bundle.getHeaders() ).thenReturn( Dictionaries.of() );

        return bundle;
    }

    private BundleContext createBundleContext()
    {
        return Mockito.mock( BundleContext.class );
    }
}
