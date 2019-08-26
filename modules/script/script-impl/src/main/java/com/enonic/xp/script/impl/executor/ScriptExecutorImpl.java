package com.enonic.xp.script.impl.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;

import com.google.common.util.concurrent.Striped;

import org.graalvm.polyglot.Value;

import com.enonic.xp.app.Application;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceError;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.ScriptExports;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.function.ApplicationInfoBuilder;
import com.enonic.xp.script.impl.function.ScriptFunctions;
import com.enonic.xp.script.impl.service.ServiceRegistry;
import com.enonic.xp.script.impl.util.ErrorHelper;
import com.enonic.xp.script.impl.util.JavascriptHelper;
import com.enonic.xp.script.impl.util.JavascriptHelperFactory;
import com.enonic.xp.script.impl.util.NashornHelper;
import com.enonic.xp.script.impl.value.ScriptValueFactory;
import com.enonic.xp.script.impl.value.ScriptValueFactoryImpl;
import com.enonic.xp.script.runtime.ScriptSettings;
import com.enonic.xp.server.RunMode;

import static com.google.common.base.Strings.nullToEmpty;

public final class ScriptExecutorImpl
    implements ScriptExecutor
{
    private final static String PRE_SCRIPT = "(function(log, require, resolve, __, exports, module) { ";

    private final static String POST_SCRIPT = "\n});";

    private ScriptEngine engine;

    private ScriptSettings scriptSettings;

    private ScriptExportsCache exportsCache;

    private ClassLoader classLoader;

    private ServiceRegistry serviceRegistry;

    private ResourceService resourceService;

    private Application application;

    private Map<String, Object> mocks;

    private Map<ResourceKey, Runnable> disposers;

    private RunMode runMode;

    private ScriptValueFactory scriptValueFactory;

    private JavascriptHelper javascriptHelper;

    private final static Striped<Lock> REQUIRE_LOCKS = Striped.lazyWeakLock( 1000 );

    public void setScriptSettings( final ScriptSettings scriptSettings )
    {
        this.scriptSettings = scriptSettings;
    }

    public void setClassLoader( final ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    public void setServiceRegistry( final ServiceRegistry serviceRegistry )
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }

    public void setApplication( final Application application )
    {
        this.application = application;
    }

    public void setRunMode( final RunMode runMode )
    {
        this.runMode = runMode;
    }

    public void initialize()
    {
        this.engine = NashornHelper.getScriptEngine( this.classLoader );
        this.mocks = new HashMap<>();
        this.disposers = new HashMap<>();
        this.exportsCache = new ScriptExportsCache();

        final JavascriptHelperFactory javascriptHelperFactory = new JavascriptHelperFactory( this.engine );
        this.javascriptHelper = javascriptHelperFactory.create();
        this.scriptValueFactory = new ScriptValueFactoryImpl( this.javascriptHelper );

        final Bindings global = new SimpleBindings();
        global.putAll( this.scriptSettings.getGlobalVariables() );
        global.put( "app", buildAppInfo() );
        this.engine.setBindings( global, ScriptContext.GLOBAL_SCOPE );
    }

    private Value buildAppInfo()
    {
        final ApplicationInfoBuilder builder = new ApplicationInfoBuilder();
        builder.application( this.application );
        builder.javascriptHelper( this.javascriptHelper );
        return builder.build();
    }

    @Override
    public ScriptExports executeMain( final ResourceKey key )
    {
        expireCacheIfNeeded();

        final Object exports = executeRequire( key );
        final ScriptValue value = newScriptValue( exports );
        return new ScriptExportsImpl( key, value, exports );
    }

    private void expireCacheIfNeeded()
    {
        if ( this.runMode != RunMode.DEV )
        {
            return;
        }

        if ( this.exportsCache.isExpired() )
        {
            this.exportsCache.clear();
            runDisposers();
        }
    }

    @Override
    public Object executeRequire( final ResourceKey key )
    {
        final Object mock = this.mocks.get( key.getPath() );
        if ( mock != null )
        {
            return mock;
        }

        Object cached = this.exportsCache.get( key );
        if ( cached != null )
        {
            return cached;
        }

        final Lock lock = REQUIRE_LOCKS.get( key );
        try
        {
            if ( lock.tryLock( 5, TimeUnit.MINUTES ) )
            {
                try
                {
                    cached = this.exportsCache.get( key );
                    final Resource resource = loadIfNeeded( key, cached );
                    if ( resource == null )
                    {
                        return cached;
                    }

                    final Object result = requireJsOrJson( resource );
                    this.exportsCache.put( resource, result );
                    return result;
                }
                finally
                {
                    lock.unlock();
                }
            }
            else
            {
                throw new RuntimeException( "Script require failed: [" + key + "]" );
            }
        }
        catch ( InterruptedException e )
        {
            throw new RuntimeException( "Script require failed: [" + key + "]", e );
        }
    }

    private String getFileName( final Resource resource )
    {
        if ( this.scriptSettings.getDebug() != null )
        {
            return this.scriptSettings.getDebug().scriptName( resource );
        }

        return resource.getKey().toString();
    }

    private Object executeRequire( final ResourceKey key, final Value func )
    {
        try
        {
            final Value exports = this.javascriptHelper.newJsObject();

            final Value module = this.javascriptHelper.newJsObject();
            module.putMember( "id", key.toString() );
            module.putMember( "exports", exports );

            final ScriptFunctions functions = new ScriptFunctions( key, this );
            func.execute( exports, functions.getLog(), functions.getRequire(), functions.getResolve(), functions, exports, module );
            return module.getMember( "exports" );
        }
        catch ( final Exception e )
        {
            throw ErrorHelper.handleError( e );
        }
        catch ( final StackOverflowError e )
        {
            throw new ResourceError( key, "Script require failed: [" + key + "]", e );
        }
    }

    @Override
    public ScriptValue newScriptValue( final Object value )
    {
        return this.scriptValueFactory.newValue( value );
    }

    private Value doExecute( final Bindings bindings, final Resource script )
    {
        try
        {
            final String text = script.readString();
            final String source = PRE_SCRIPT + text + POST_SCRIPT;
            return (Value) this.engine.eval( source, bindings );
        }
        catch ( final Exception e )
        {
            throw ErrorHelper.handleError( e );
        }
        catch ( final StackOverflowError e )
        {
            throw new ResourceError( script.getKey(), "Script execute failed: [" + script.getKey() + "]", e );
        }
    }

    private Resource loadResource( final ResourceKey key )
    {
        return this.resourceService.getResource( key );
    }

    private Resource loadIfNeeded( final ResourceKey key, final Object cached )
    {
        if ( cached == null )
        {
            return loadResource( key );
        }

        return null;
    }

    private Object requireJs( final Resource resource )
    {
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put( ScriptEngine.FILENAME, getFileName( resource ) );

        final Value func = doExecute( bindings, resource );
        final Object result = executeRequire( resource.getKey(), func );

        this.exportsCache.put( resource, result );
        return result;
    }

    private Object requireJsOrJson( final Resource resource )
    {
        final String ext = nullToEmpty( resource.getKey().getExtension() );
        if ( ext.equals( "json" ) )
        {
            return requireJson( resource );
        }

        return requireJs( resource );
    }

    private Object requireJson( final Resource resource )
    {
        try
        {
            final String text = resource.readString();
            return this.javascriptHelper.parseJson( text );
        }
        catch ( final Exception e )
        {
            throw ErrorHelper.handleError( e );
        }
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    @Override
    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    @Override
    public ResourceService getResourceService()
    {
        return resourceService;
    }

    @Override
    public Application getApplication()
    {
        return this.application;
    }

    @Override
    public ScriptSettings getScriptSettings()
    {
        return this.scriptSettings;
    }

    @Override
    public void registerMock( final String name, final Object value )
    {
        this.mocks.put( name, value );
    }

    @Override
    public JavascriptHelper getJavascriptHelper()
    {
        return this.javascriptHelper;
    }

    @Override
    public void registerDisposer( final ResourceKey key, final Runnable callback )
    {
        this.disposers.put( key, callback );
    }

    @Override
    public void runDisposers()
    {
        this.disposers.values().forEach( Runnable::run );
    }
}
