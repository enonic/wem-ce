package com.enonic.xp.impl.task;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.core.internal.concurrent.RecurringJob;
import com.enonic.xp.event.EventPublisher;
import com.enonic.xp.impl.task.distributed.DescribedTask;
import com.enonic.xp.impl.task.distributed.TaskManager;
import com.enonic.xp.impl.task.event.TaskEvents;
import com.enonic.xp.security.User;
import com.enonic.xp.task.TaskId;
import com.enonic.xp.task.TaskInfo;
import com.enonic.xp.task.TaskProgress;
import com.enonic.xp.task.TaskState;
import com.enonic.xp.trace.Trace;
import com.enonic.xp.trace.Tracer;

@Component(immediate = true)
@Local
public final class LocalTaskManagerImpl
    implements TaskManager
{
    static final long KEEP_COMPLETED_MAX_TIME_SEC = 60;

    private final ConcurrentMap<TaskId, TaskInfoHolder> tasks = new ConcurrentHashMap<>();

    private final EventPublisher eventPublisher;

    private final TaskManagerCleanupScheduler cleanupScheduler;

    private final Executor executor;

    private Clock clock;

    private RecurringJob recurringJob;

    @Activate
    public LocalTaskManagerImpl( @Reference(service = TaskManagerExecutor.class) final Executor executor,
                                 @Reference TaskManagerCleanupScheduler cleanupScheduler, @Reference final EventPublisher eventPublisher )
    {
        this.executor = executor;
        this.clock = Clock.systemUTC();
        this.cleanupScheduler = cleanupScheduler;
        this.eventPublisher = eventPublisher;
    }

    @Activate
    public void activate()
    {
        recurringJob = cleanupScheduler.scheduleWithFixedDelay( this::removeExpiredTasks );
    }

    @Deactivate
    public void deactivate()
    {
        recurringJob.cancel();
    }

    @Override
    public TaskInfo getTaskInfo( final TaskId taskId )
    {
        final TaskInfoHolder ctx = tasks.get( taskId );
        return ctx != null ? ctx.getTaskInfo() : null;
    }

    @Override
    public List<TaskInfo> getAllTasks()
    {
        return tasks.values().stream().map( TaskInfoHolder::getTaskInfo ).collect( Collectors.toUnmodifiableList() );
    }

    @Override
    public List<TaskInfo> getRunningTasks()
    {
        return tasks.values().stream().map( TaskInfoHolder::getTaskInfo ).filter( TaskInfo::isRunning ).collect(
            Collectors.toUnmodifiableList() );
    }

    private void updateProgress( final TaskId taskId, final int current, final int total )
    {
        final TaskInfoHolder ctx = tasks.get( taskId );
        if ( ctx == null )
        {
            return;
        }
        final TaskInfo taskInfo = ctx.getTaskInfo();
        final TaskProgress updatedProgress = taskInfo.getProgress().copy().current( current ).total( total ).build();

        final TaskInfo updatedInfo = taskInfo.copy().progress( updatedProgress ).build();
        final TaskInfoHolder updatedCtx = ctx.copy().taskInfo( updatedInfo ).build();
        tasks.put( taskId, updatedCtx );

        eventPublisher.publish( TaskEvents.updated( updatedInfo ) );
    }

    private void updateProgress( final TaskId taskId, final String message )
    {
        final TaskInfoHolder ctx = tasks.get( taskId );
        if ( ctx == null )
        {
            return;
        }
        final TaskInfo taskInfo = ctx.getTaskInfo();
        final TaskProgress updatedProgress = taskInfo.getProgress().copy().info( message ).build();

        final TaskInfo updatedInfo = taskInfo.copy().progress( updatedProgress ).build();
        final TaskInfoHolder updatedCtx = ctx.copy().taskInfo( updatedInfo ).build();
        tasks.put( taskId, updatedCtx );

        eventPublisher.publish( TaskEvents.updated( updatedInfo ) );
    }

    private void updateState( final TaskId taskId, final TaskState newState )
    {
        final TaskInfoHolder ctx = tasks.get( taskId );
        if ( ctx == null )
        {
            return;
        }
        final TaskInfo taskInfo = ctx.getTaskInfo();
        final TaskInfo updatedInfo = taskInfo.copy().state( newState ).build();
        final Instant doneTime = newState == TaskState.FAILED || newState == TaskState.FINISHED ? Instant.now( clock ) : null;
        final TaskInfoHolder updatedCtx = ctx.copy().taskInfo( updatedInfo ).doneTime( doneTime ).build();
        tasks.put( taskId, updatedCtx );

        switch ( newState )
        {
            case FINISHED:
                eventPublisher.publish( TaskEvents.finished( updatedInfo ) );
                break;
            case FAILED:
                eventPublisher.publish( TaskEvents.failed( updatedInfo ) );
                break;
            default:
                eventPublisher.publish( TaskEvents.updated( updatedInfo ) );
                break;
        }
    }

    @Override
    public void submitTask( final DescribedTask runnableTask )
    {
        final Trace trace = Tracer.newTrace( "task.submit" );
        if ( trace == null )
        {
            doSubmitTask( runnableTask );
        }
        else
        {
            Tracer.trace( trace, () -> doSubmitTask( runnableTask ) );
            trace.put( "taskId", runnableTask.getTaskId() );
            trace.put( "name", runnableTask.getName() );
        }
    }

    private void doSubmitTask( final DescribedTask runnableTask )
    {
        executor.execute( prepareRunnable( runnableTask ) );
    }

    private Runnable prepareRunnable( final DescribedTask runnableTask )
    {
        final TaskId id = runnableTask.getTaskId();

        final User user = Objects.requireNonNullElse( runnableTask.getTaskContext().getAuthInfo().getUser(), User.ANONYMOUS );
        final TaskInfo info = TaskInfo.create().
            id( id ).
            description( runnableTask.getDescription() ).
            name( runnableTask.getName() ).
            state( TaskState.WAITING ).
            startTime( Instant.now( clock ) ).
            application( runnableTask.getApplicationKey() ).
            user( user.getKey() ).
            build();

        final TaskInfoHolder taskInfoHolder = TaskInfoHolder.create().
            taskInfo( info ).
            build();

        tasks.put( id, taskInfoHolder );

        eventPublisher.publish( TaskEvents.submitted( info ) );
        return new TaskRunnable( runnableTask, new ProgressReporterAdapter( id ) );
    }

    private void removeExpiredTasks()
    {
        final Instant now = Instant.now( clock );
        for ( TaskInfoHolder taskCtx : tasks.values() )
        {
            final TaskInfo taskInfo = taskCtx.getTaskInfo();
            if ( taskInfo.isDone() && taskCtx.getDoneTime() != null &&
                taskCtx.getDoneTime().until( now, ChronoUnit.SECONDS ) > KEEP_COMPLETED_MAX_TIME_SEC )
            {
                tasks.remove( taskInfo.getId() );
                eventPublisher.publish( TaskEvents.removed( taskInfo ) );
            }
        }
    }

    void setClock( final Clock clock )
    {
        this.clock = clock;
    }

    private class ProgressReporterAdapter
        implements InternalProgressReporter
    {
        private final TaskId taskId;

        public ProgressReporterAdapter( final TaskId taskId )
        {
            this.taskId = taskId;
        }

        public void running()
        {
            updateState( taskId, TaskState.RUNNING );
        }

        public void finished()
        {
            updateState( taskId, TaskState.FINISHED );
        }

        public void failed( final String message )
        {
            updateProgress( taskId, message );
            updateState( taskId, TaskState.FAILED );
        }

        @Override
        public void progress( final int current, final int total )
        {
            updateProgress( taskId, current, total );
        }

        @Override
        public void info( final String message )
        {
            updateProgress( taskId, message );
        }
    }
}
