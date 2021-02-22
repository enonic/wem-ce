package com.enonic.xp.impl.scheduler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;

import com.enonic.xp.impl.scheduler.distributed.SchedulableTask;
import com.enonic.xp.scheduler.ScheduledJob;

public final class ScheduleJobCommand //TODO: Do we really need it with reschedule task?
{
    private final static Logger LOG = LoggerFactory.getLogger( ScheduleJobCommand.class );

    private final IScheduledExecutorService schedulerService;

    private final ScheduledJob job;

    private ScheduleJobCommand( final Builder builder )
    {
        this.schedulerService = builder.schedulerService;
        this.job = builder.job;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public void execute()
    {
        try
        {
            this.doExecute();

        }
        catch ( Exception e )
        {
            LOG.warn( "Error while running job [{}]", this.job.getName(), e );
            doExecute();
        }
        catch ( Throwable t )
        {
            LOG.error( "Error while running job [{}], no further attempts will be made", this.job.getName(), t );
            throw t;
        }
    }

    private void doExecute()
    {
        final SchedulableTask task = SchedulableTask.create().
            job( job ).
            build();

        job.getCalendar().
            nextExecution().
            map( duration -> schedulerService.schedule( task, duration.toMillis(), TimeUnit.MILLISECONDS ) );
    }


    public static class Builder
    {
        private IScheduledExecutorService schedulerService;

        private ScheduledJob job;

        public Builder schedulerService( final IScheduledExecutorService schedulerService )
        {
            this.schedulerService = schedulerService;
            return this;
        }

        public Builder job( final ScheduledJob job )
        {
            this.job = job;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( schedulerService, "schedulerService must be set." );
            Preconditions.checkNotNull( job, "job must be set." );
        }

        public ScheduleJobCommand build()
        {
            validate();
            return new ScheduleJobCommand( this );
        }
    }
}
