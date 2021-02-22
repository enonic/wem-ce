package com.enonic.xp.impl.scheduler.distributed;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.NamedTask;

import com.enonic.xp.core.internal.osgi.OsgiSupport;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.scheduler.ScheduleCalendar;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.task.SubmitTaskParams;
import com.enonic.xp.task.TaskService;

public class SchedulableTask
    implements NamedTask, HazelcastInstanceAware, Runnable, Serializable
{
    private static final long serialVersionUID = 0;

    private static final Logger LOG = LoggerFactory.getLogger( SchedulableTask.class );

    private final ScheduledJob job;

    private IScheduledExecutorService schedulerService;

    private SchedulableTask( final Builder builder )
    {
        this.job = builder.job;
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public String getName()
    {
        return job.getName().getValue();
    }

    @Override
    public void setHazelcastInstance( final HazelcastInstance hazelcastInstance )
    {
        this.schedulerService = hazelcastInstance.getScheduledExecutorService( "scheduler" );
    }

    @Override
    public void run()
    {
        try
        {
            OsgiSupport.withService( TaskService.class, taskService -> taskService.submitTask( SubmitTaskParams.create().
                descriptorKey( job.getDescriptor() ).
                data( job.getPayload() ).
                build() ) );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error while running job [{}]", this.job.getName(), e );
        }
        catch ( Throwable t )
        {
            LOG.error( "Error while running job [{}], no further attempts will be made", this.job.getName(), t );
            throw t;
        }

    }

    private Object writeReplace()
    {
        return new SerializedForm( this.job );
    }

    private static class SerializedForm
        implements Serializable
    {
        private static final long serialVersionUID = 0;

        private final String name;

        private final String description;

        private final ScheduleCalendar calendar;

        private final boolean enabled;

        private final String descriptor;

        private final PropertyTree payload;

        private final String user;

        private final String author;

        public SerializedForm( ScheduledJob job )
        {
            this.name = job.getName().getValue();
            this.description = job.getDescription();
            this.calendar = job.getCalendar();
            this.enabled = job.isEnabled();
            this.descriptor = job.getDescriptor() != null ? job.getDescriptor().toString() : null;
            this.payload = job.getPayload();
            this.user = job.getUser() != null ? job.getUser().toString() : null;
            this.author = job.getAuthor() != null ? job.getAuthor().toString() : null;
        }

        private Object readResolve()
        {
            return SchedulableTask.create().
                job( ScheduledJob.create().
                    name( SchedulerName.from( name ) ).
                    description( description ).
                    calendar( calendar ).
                    enabled( enabled ).
                    descriptor( descriptor != null ? DescriptorKey.from( descriptor ) : null ).
                    payload( payload ).
                    user( user != null ? PrincipalKey.from( user ) : null ).
                    author( author != null ? PrincipalKey.from( author ) : null ).
                    build() ).build();
        }
    }

    public static class Builder
    {
        private ScheduledJob job;

        public Builder job( final ScheduledJob job )
        {
            this.job = job;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( job, "job must be set." );
        }

        public SchedulableTask build()
        {
            validate();
            return new SchedulableTask( this );
        }
    }
}
