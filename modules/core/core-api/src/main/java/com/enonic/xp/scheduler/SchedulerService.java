package com.enonic.xp.scheduler;

import java.util.List;

public interface SchedulerService
{
    ScheduledJob create( CreateScheduledJobParams params );

    ScheduledJob modify( ModifyScheduledJobParams params );

    List<ScheduledJob> list();

    ScheduledJob get( SchedulerName name );

    boolean delete( SchedulerName name );
}
