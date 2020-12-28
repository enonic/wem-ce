package com.enonic.xp.core.impl.app.resource;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationInvalidationLevel;
import com.enonic.xp.app.ApplicationInvalidator;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.core.impl.app.resolver.ApplicationUrlResolver;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceKeys;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;
import com.enonic.xp.server.RunMode;

@Component(immediate = true)
public final class ResourceServiceImpl
    implements ResourceService, ApplicationInvalidator
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceServiceImpl.class );

    private static final ApplicationKey SYSTEM_APPLICATION_KEY = ApplicationKey.from( "com.enonic.xp.app.system" );

    private final ProcessingCache cache;

    private ApplicationService applicationService;

    public ResourceServiceImpl()
    {
        this.cache = new ProcessingCache( this::getResource, RunMode.get() );
    }

    @Override
    public Resource getResource( final ResourceKey key )
    {
        return findApplication( key.getApplicationKey() ).
            map( app -> app.resolveFile( key.getPath() ) ).
            map( url -> new UrlResource( key, url ) ).
            orElse( new UrlResource( key, null ) );
    }

    @Override
    public ResourceKeys findFiles( final ApplicationKey key, final String pattern )
    {
        final Pattern compiled = Pattern.compile( ApplicationUrlResolver.normalizePath( pattern ) );

        return ResourceKeys.from( findApplication( key ).
            map( Application::getFiles ).orElse( Set.of() ).
            stream().
            filter( compiled.asPredicate() ).
            map( name -> ResourceKey.from( key, name ) ).iterator() );
    }

    private Optional<Application> findApplication( final ApplicationKey key )
    {
        final ApplicationKey applicationKey = isSystemApp( key ) ? SYSTEM_APPLICATION_KEY : key;
        return Optional.ofNullable( applicationService.getInstalledApplication( applicationKey ) ).
            filter( Application::isStarted );
    }

    private boolean isSystemApp( final ApplicationKey key )
    {
        return ApplicationKey.SYSTEM_RESERVED_APPLICATION_KEYS.contains( key );
    }

    @Override
    public <K, V> V processResource( final ResourceProcessor<K, V> processor )
    {
        return this.cache.process( processor );
    }

    @Reference
    public void setApplicationService( final ApplicationService applicationService )
    {
        this.applicationService = applicationService;
    }

    @Override
    @Deprecated
    public void invalidate( final ApplicationKey key )
    {
        invalidate( key, ApplicationInvalidationLevel.FULL );
    }

    @Override
    public void invalidate( final ApplicationKey key, final ApplicationInvalidationLevel level )
    {
        LOG.debug( "Cleanup Resource cache for {}", key );
        this.cache.invalidate( key );
    }
}
