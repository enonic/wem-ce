package com.enonic.xp.scheduler;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public interface ScheduledJobEditor
{
    void edit( EditableScheduledJob edit );
}
