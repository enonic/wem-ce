package com.enonic.xp.core.impl.app.resource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import com.enonic.xp.app.ApplicationBundleUtils;
import com.enonic.xp.app.ApplicationInvalidationLevel;
import com.enonic.xp.app.ApplicationInvalidator;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.impl.app.ApplicationFactoryService;
import com.enonic.xp.core.impl.app.ApplicationImpl;
import com.enonic.xp.core.impl.app.ApplicationRegistry;
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

    private ApplicationRegistry applicationRegistry;

    private ApplicationFactoryService applicationFactoryService;

    private final BundleTracker<ResourceKeys> bundleTracker;

    @Activate
    public ResourceServiceImpl( final BundleContext context, @Reference final ApplicationRegistry applicationRegistry,
                                @Reference final ApplicationFactoryService applicationFactoryService )
    {
        this.applicationRegistry = applicationRegistry;
        this.applicationFactoryService = applicationFactoryService;
        bundleTracker = new BundleTracker<>( context, Bundle.ACTIVE, new Customizer() );
        this.cache = new ProcessingCache( this::getResource, RunMode.get() );
    }

    @Activate
    public void activate()
    {
        bundleTracker.open();
    }

    @Deactivate
    public void deactivate()
    {
        bundleTracker.close();
    }

    @Override
    public Resource getResource( final ResourceKey key )
    {
        return Optional.ofNullable( applicationRegistry.get( rectifySystemKey( key.getApplicationKey() ) ) ).
            map( app -> app.resolveFile( key.getPath() ) ).
            map( url -> new UrlResource( key, url ) ).
            orElse( new UrlResource( key, null ) );
    }

    @Override
    public ResourceKeys findFiles( final ApplicationKey key, final String pattern )
    {
        final Predicate<String> compiled = Pattern.compile( pattern ).asPredicate();

        return ResourceKeys.from( findFiles( key ).
            filter( rk -> compiled.test( rk.getPath() ) ).iterator() );
    }

    @Override
    public Stream<ResourceKey> findFiles( final ApplicationKey key )
    {
        return findForApplication( key ).stream();
    }

    private ResourceKeys findForApplication( final ApplicationKey key )
    {
        final ApplicationKey applicationKey = rectifySystemKey( key );

        return bundleTracker.getTracked().
            entrySet().stream().
            filter( bundleEntry -> applicationKey.equals( ApplicationKey.from( bundleEntry.getKey() ) ) ).
            findAny().
            map( Map.Entry::getValue ).orElse( ResourceKeys.empty() );
    }

    private ApplicationKey rectifySystemKey( final ApplicationKey key )
    {
        return ApplicationKey.SYSTEM_RESERVED_APPLICATION_KEYS.contains( key ) ? SYSTEM_APPLICATION_KEY : key;
    }

    @Override
    public <K, V> V processResource( final ResourceProcessor<K, V> processor )
    {
        return this.cache.process( processor );
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

    private class Customizer
        implements BundleTrackerCustomizer<ResourceKeys>
    {
        @Override
        public ResourceKeys addingBundle( final Bundle bundle, final BundleEvent event )
        {
            if ( ApplicationBundleUtils.isApplication( bundle ) )
            {
                final ApplicationImpl application = applicationFactoryService.
                    getApplication( bundle );
                return ResourceKeys.from( application.
                    getUrlResolver().
                    findFiles().
                    stream().
                    map( name -> ResourceKey.from( application.getKey(), name ) ).
                    collect( ImmutableList.toImmutableList() ) );
            }
            else
            {
                return null;
            }
        }

        @Override
        public void modifiedBundle( final Bundle bundle, final BundleEvent event, final ResourceKeys object )
        {
        }

        @Override
        public void removedBundle( final Bundle bundle, final BundleEvent event, final ResourceKeys object )
        {
        }
    }
}
