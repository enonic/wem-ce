package com.enonic.xp.impl.scheduler.distributed;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.scheduledexecutor.DuplicateTaskException;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.NamedTask;

import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.core.internal.osgi.OsgiSupport;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.scheduler.SchedulerService;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.auth.AuthenticationInfo;

public class RescheduleTask
    implements NamedTask, HazelcastInstanceAware, Runnable, Serializable
{
    private static final long serialVersionUID = 0;

    private static final Logger LOG = LoggerFactory.getLogger( RescheduleTask.class );

    private transient IScheduledExecutorService schedulerExecutorService;

    @Override
    public String getName()
    {
        return "rescheduleTask";
    }

    @Override
    public void setHazelcastInstance( final HazelcastInstance hazelcastInstance )
    {
        this.schedulerExecutorService = hazelcastInstance.getScheduledExecutorService( "scheduler" );
    }

    @Override
    public void run()
    {
        try
        {
            this.doRun();
        }
        catch ( Exception e )
        {
            LOG.warn( "Problem during task scheduling.", e );
        }
    }

    private void doRun()
    {
        final List<ScheduledJob> jobs =
            OsgiSupport.withService( SchedulerService.class, schedulerService -> adminContext().callWith( schedulerService::list ) );

        final List<SchedulerName> liveJobNames = schedulerExecutorService.getAllScheduledFutures().values().stream().
            flatMap( Collection::stream ).
            filter( future -> {
                if ( future.isDone() )
                {
                    future.dispose();
                    return false;
                }
                return true;
            } ).
            map( future -> SchedulerName.from( future.getHandler().getTaskName() ) ).
            collect( Collectors.toList() );

        final List<ScheduledJob> jobsToSchedule = jobs.
            stream().
            filter( job -> !liveJobNames.contains( job.getName() ) ).
            filter( ScheduledJob::isEnabled ).
            collect( Collectors.toList() );

        jobsToSchedule.forEach( job -> job.getCalendar().
            nextExecution().
            ifPresent( duration -> {
                try
                {
                    schedulerExecutorService.schedule( SchedulableTask.create().
                        job( job ).
                        build(), duration.toMillis(), TimeUnit.MILLISECONDS );
                }
                catch ( DuplicateTaskException e )
                {

                }
                catch ( Exception e )
                {
                    LOG.warn( "[{}] job rescheduling failed.", job.getName().getValue(), e );
                }
            } ) );
    }

    private Context adminContext()
    {
        return ContextBuilder.from( ContextAccessor.current() ).
            authInfo( AuthenticationInfo.
                copyOf( ContextAccessor.current().getAuthInfo() ).
                principals( RoleKeys.ADMIN ).
                build() ).build();
    }
}
