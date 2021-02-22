package com.enonic.xp.impl.scheduler;

import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.DuplicateTaskException;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;

import com.enonic.xp.impl.scheduler.distributed.RescheduleTask;
import com.enonic.xp.index.IndexService;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.scheduler.SchedulerService;

@Component(immediate = true)
public final class SchedulerServiceActivator
{
    private final RepositoryService repositoryService;

    private final IndexService indexService;

    private final NodeService nodeService;

    private final HazelcastInstance hazelcastInstance;

    private ServiceRegistration<SchedulerService> service;

    @Activate
    public SchedulerServiceActivator( @Reference final RepositoryService repositoryService, @Reference final IndexService indexService,
                                      @Reference final NodeService nodeService, @Reference final HazelcastInstance hazelcastInstance )
    {
        this.repositoryService = repositoryService;
        this.indexService = indexService;
        this.nodeService = nodeService;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Activate
    public void activate( final BundleContext context )
    {
        final SchedulerServiceImpl schedulerService =
            new SchedulerServiceImpl( indexService, repositoryService, nodeService, hazelcastInstance );

        schedulerService.initialize();
        service = context.registerService( SchedulerService.class, schedulerService, null );

        /*if ( hazelcastInstance.getCluster().getLocalMember().getAddress().getPort() == 5701 )
        {
            SchedulerContext.createAdminContext().runWith( () -> {

                final SchedulerName name = SchedulerName.from( "test" );

                schedulerService.delete( name );

                try
                {
                    Thread.sleep( 1000 );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

                schedulerService.create( CreateScheduledJobParams.create().
                    name( name ).
                    descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" ) ).
                    calendar( CronCalendar.create().
                        value( "* * * * *" ).
                        timeZone( TimeZone.getDefault() ).
                        build() ).
                    payload( new PropertyTree() ).
                    build() );
            } );
        }*/

        final IScheduledExecutorService schedulerExecutorService = hazelcastInstance.getScheduledExecutorService( "scheduler" );

        try
        {
            schedulerExecutorService.scheduleAtFixedRate( new RescheduleTask(), 1, 1, TimeUnit.SECONDS );
        }
        catch ( DuplicateTaskException e )
        {
        }
    }

    @Deactivate
    public void deactivate()
    {
        service.unregister();
    }
}
