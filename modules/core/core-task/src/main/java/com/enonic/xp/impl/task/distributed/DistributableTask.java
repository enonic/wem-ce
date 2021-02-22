package com.enonic.xp.impl.task.distributed;

import java.io.Serializable;
import java.util.UUID;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.internal.osgi.OsgiSupport;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.script.NamedTask;
import com.enonic.xp.impl.task.script.NamedTaskFactory;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.ProgressReporter;
import com.enonic.xp.task.TaskId;

public final class DistributableTask
    implements DescribedTask, Serializable
{
    private static final long serialVersionUID = 0;

    private final TaskId taskId;

    private final String name;

    private final DescriptorKey key;

    private final PropertyTree data;

    private final TaskContext context;

    private transient NamedTask namedTask;

    public DistributableTask( final DescriptorKey key, final PropertyTree data, final TaskContext context )
    {
        this.taskId = TaskId.from( UUID.randomUUID().toString() );
        this.name = key.toString();
        this.key = key;
        this.data = data;
        this.context = context;
    }

    @Override
    public TaskId getTaskId()
    {
        return taskId;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public TaskContext getTaskContext()
    {
        return context;
    }

    @Override
    public ApplicationKey getApplicationKey()
    {
        return key.getApplicationKey();
    }

    @Override
    public String getDescription()
    {
        initNamedTask();
        return namedTask.getTaskDescriptor().getDescription();
    }

    @Override
    public void run( final ProgressReporter progressReporter )
    {
        initNamedTask();
        namedTask.run( taskId, progressReporter );
    }

    private void initNamedTask()
    {
        if ( namedTask == null )
        {
            final DescriptorKey descriptorKey = DescriptorKey.from( name );
            namedTask = OsgiSupport.withService( NamedTaskFactory.class, ntsf -> ntsf.create( descriptorKey, data ) );
        }
    }
}
