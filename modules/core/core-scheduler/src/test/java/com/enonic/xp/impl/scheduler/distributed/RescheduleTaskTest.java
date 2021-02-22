package com.enonic.xp.impl.scheduler.distributed;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.internal.osgi.OsgiSupportMock;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.scheduler.CronCalendar;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.scheduler.SchedulerService;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RescheduleTaskTest
{
    @Mock(stubOnly = true)
    ServiceReference<SchedulerService> serviceReference;

    @Captor
    ArgumentCaptor<SchedulableTask> taskCaptor;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private IScheduledExecutorService schedulerExecutorService;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock(stubOnly = true)
    private BundleContext bundleContext;

    private Bundle bundle;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        Mockito.when( hazelcastInstance.getScheduledExecutorService( Mockito.isA( String.class ) ) ).thenReturn( schedulerExecutorService );

        bundle = OsgiSupportMock.mockBundle();
        when( bundle.getBundleContext() ).thenReturn( bundleContext );
        when( bundleContext.getServiceReferences( SchedulerService.class, null ) ).thenReturn( List.of( serviceReference ) );
        when( bundleContext.getService( serviceReference ) ).thenReturn( schedulerService );
    }

    @AfterEach
    void tearDown()
    {
        OsgiSupportMock.reset();
    }

    @Test
    public void rescheduleWithDoneAndDisabledTasks()
    {
        mockFutures();
        mockJobs();

        createAndRunTask();

        Mockito.verify( schedulerExecutorService, Mockito.times( 2 ) ).schedule( taskCaptor.capture(), Mockito.anyLong(),
                                                                                 Mockito.isA( TimeUnit.class ) );

        Assertions.assertEquals( 2, taskCaptor.getAllValues().size() );
        Assertions.assertEquals( "task2", taskCaptor.getAllValues().get( 0 ).getName() );
        Assertions.assertEquals( "task3", taskCaptor.getAllValues().get( 1 ).getName() );
    }

    private RescheduleTask createAndRunTask()
    {
        final RescheduleTask task = new RescheduleTask();
        task.setHazelcastInstance( hazelcastInstance );

        task.run();

        return task;
    }

    private void mockFutures()
    {
        final ScheduledTaskHandler handler4 = Mockito.mock( ScheduledTaskHandler.class );

        final IScheduledFuture<?> future1 = Mockito.mock( IScheduledFuture.class );
        final IScheduledFuture<?> future2 = Mockito.mock( IScheduledFuture.class );
        final IScheduledFuture<?> future3 = Mockito.mock( IScheduledFuture.class );
        final IScheduledFuture<?> future4 = Mockito.mock( IScheduledFuture.class );

        Mockito.when( future4.getHandler() ).thenReturn( handler4 );

        Mockito.when( handler4.getTaskName() ).thenReturn( "task4" );

        Mockito.when( future1.isDone() ).thenReturn( true );
        Mockito.when( future2.isDone() ).thenReturn( true );
        Mockito.when( future3.isDone() ).thenReturn( true );

        final Map futures =
            Map.of( Mockito.mock( Member.class ), List.of( future1, future2 ), Mockito.mock( Member.class ), List.of( future3, future4 ) );
        Mockito.when( schedulerExecutorService.getAllScheduledFutures() ).
            thenReturn( futures );

    }

    private void mockJobs()
    {
        final ScheduledJob job1 = ScheduledJob.create().
            name( SchedulerName.from( "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getDefault() ).
                build() ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            payload( new PropertyTree() ).
            enabled( false ).
            build();

        final ScheduledJob job2 = ScheduledJob.create().
            name( SchedulerName.from( "task2" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getDefault() ).
                build() ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task2" ) ).
            payload( new PropertyTree() ).
            enabled( true ).
            build();

        final ScheduledJob job3 = ScheduledJob.create().
            name( SchedulerName.from( "task3" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getDefault() ).
                build() ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task3" ) ).
            payload( new PropertyTree() ).
            enabled( true ).
            build();

        final ScheduledJob job4 = ScheduledJob.create().
            name( SchedulerName.from( "task4" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getDefault() ).
                build() ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task4" ) ).
            payload( new PropertyTree() ).
            enabled( true ).
            build();

        Mockito.when( schedulerService.list() ).
            thenReturn( List.of( job1, job2, job3, job4 ) );
    }

}
