package com.enonic.xp.impl.task;

import java.util.UUID;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.internal.osgi.OsgiSupport;
import com.enonic.xp.impl.task.distributed.DescribedTask;
import com.enonic.xp.impl.task.distributed.TaskContext;
import com.enonic.xp.impl.task.script.NamedTask;
import com.enonic.xp.task.ProgressReporter;
import com.enonic.xp.task.RunnableTask;
import com.enonic.xp.task.TaskDescriptor;
import com.enonic.xp.task.TaskId;

public class DescribedTaskImpl
    implements DescribedTask
{
    private final RunnableTask runnableTask;

    private final TaskId taskId;

    private final String description;

    private final TaskContext context;

    private final ApplicationKey applicationKey;

    private final String name;

    public DescribedTaskImpl( final RunnableTask runnableTask, final String description, final TaskContext context )
    {
        this.runnableTask = runnableTask;
        this.taskId = TaskId.from( UUID.randomUUID().toString() );
        this.context = context;

        this.description = description;
        this.applicationKey = ApplicationKey.from( OsgiSupport.getBundle( runnableTask.getClass() ) );
        this.name = "";
    }

    public DescribedTaskImpl( final NamedTask namedTask, final TaskContext context )
    {
        this.runnableTask = namedTask;
        this.taskId = TaskId.from( UUID.randomUUID().toString() );
        this.context = context;

        final TaskDescriptor taskDescriptor = namedTask.getTaskDescriptor();
        this.description = taskDescriptor.getDescription();
        this.applicationKey = taskDescriptor.getApplicationKey();
        this.name = taskDescriptor.getKey().toString();
    }

    @Override
    public void run( final ProgressReporter progressReporter )
    {
        runnableTask.run( this.taskId, progressReporter );
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public TaskId getTaskId()
    {
        return taskId;
    }

    @Override
    public TaskContext getTaskContext()
    {
        return context;
    }

    @Override
    public ApplicationKey getApplicationKey()
    {
        return applicationKey;
    }
}
