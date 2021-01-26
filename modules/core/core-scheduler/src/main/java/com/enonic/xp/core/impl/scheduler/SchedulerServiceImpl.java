package com.enonic.xp.core.impl.scheduler;

import java.util.List;

import com.enonic.xp.scheduler.CreateScheduledJobParams;
import com.enonic.xp.scheduler.ModifyScheduledJobParams;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.scheduler.SchedulerService;

public class SchedulerServiceImpl
    implements SchedulerService
{
    @Override
    public ScheduledJob create( final CreateScheduledJobParams params )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledJob modify( final ModifyScheduledJobParams params )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ScheduledJob> list()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledJob get( final SchedulerName name )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete( final SchedulerName name )
    {
        throw new UnsupportedOperationException();
    }
}
