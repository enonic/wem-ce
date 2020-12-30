package com.enonic.xp.core.impl.app;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.app.ApplicationBundleUtils;
import com.enonic.xp.server.RunMode;

@Component(immediate = true)
public class ApplicationFactoryServiceImpl
    implements ApplicationFactoryService
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplicationFactoryServiceImpl.class );

    private final BundleTracker<ApplicationImpl> bundleTracker;

    @Activate
    public ApplicationFactoryServiceImpl( final BundleContext context )
    {
        bundleTracker =
            new BundleTracker<>( context, Bundle.RESOLVED + Bundle.STARTING + Bundle.STOPPING + Bundle.ACTIVE, new Customizer() );
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
    public ApplicationImpl getApplication( final Bundle bundle )
    {
        return bundleTracker.getObject( bundle );
    }

    private static class Customizer
        implements BundleTrackerCustomizer<ApplicationImpl>
    {
        private final ApplicationFactory factory = new ApplicationFactory( RunMode.get() );

        @Override
        public ApplicationImpl addingBundle( final Bundle bundle, final BundleEvent event )
        {
            if ( ApplicationBundleUtils.isApplication( bundle ) )
            {
                return factory.create( bundle );
            }
            else
            {
                return null;
            }
        }

        @Override
        public void modifiedBundle( final Bundle bundle, final BundleEvent event, final ApplicationImpl object )
        {

        }

        @Override
        public void removedBundle( final Bundle bundle, final BundleEvent event, final ApplicationImpl object )
        {
        }
    }
}
