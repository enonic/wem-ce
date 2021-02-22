package com.enonic.xp.impl.scheduler;

import com.google.common.base.Preconditions;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;

import com.enonic.xp.scheduler.SchedulerName;

final class UnscheduleJobCommand
{
    private final IScheduledExecutorService schedulerService;

    private final SchedulerName name;

    private UnscheduleJobCommand( final Builder builder )
    {
        this.schedulerService = builder.schedulerService;
        this.name = builder.name;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public void execute()
    {
        schedulerService.getAllScheduledFutures().
            values().stream().
            flatMap( features -> features.stream().
                filter( feature -> name.getValue().equals( feature.getHandler().getTaskName() ) ) ).
            findAny().
            ifPresent( IScheduledFuture::dispose );
    }

    public static class Builder
    {
        private IScheduledExecutorService schedulerService;

        private SchedulerName name;

        public Builder schedulerService( final IScheduledExecutorService schedulerService )
        {
            this.schedulerService = schedulerService;
            return this;
        }

        public Builder name( final SchedulerName name )
        {
            this.name = name;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( schedulerService, "schedulerService must be set." );
            Preconditions.checkNotNull( name, "name must be set." );
        }

        public UnscheduleJobCommand build()
        {
            validate();
            return new UnscheduleJobCommand( this );
        }
    }
}
