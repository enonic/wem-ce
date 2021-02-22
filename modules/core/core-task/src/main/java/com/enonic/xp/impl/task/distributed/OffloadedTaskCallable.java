package com.enonic.xp.impl.task.distributed;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.enonic.xp.core.internal.osgi.OsgiSupport;

public final class OffloadedTaskCallable
    implements Callable<Void>, Serializable
{
    private static final long serialVersionUID = 0;

    private final DescribedTask task;

    public OffloadedTaskCallable( final DescribedTask task )
    {
        this.task = task;
    }

    @Override
    public Void call()
    {
        return OsgiSupport.withService( TaskManager.class, "(local=true)", taskExecutor -> {
            taskExecutor.submitTask( task );
            return null;
        } );
    }
}
