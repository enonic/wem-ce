package com.enonic.xp.scheduler;


import java.util.TimeZone;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.security.PrincipalKey;

@PublicApi
public final class EditableScheduledJob
{
    public final ScheduledJob source;

    public String description;

    public Frequency frequency;

    public boolean enabled;

    public TimeZone timeZone;

    public DescriptorKey descriptor;

    public PropertyTree payload;

    public PrincipalKey user;

    public PrincipalKey author;

    public EditableScheduledJob( final ScheduledJob source )
    {
        this.source = source;
        this.description = source.getDescription();
        this.frequency = source.getFrequency();
        this.enabled = source.isEnabled();
        this.timeZone = source.getTimeZone();
        this.descriptor = source.getDescriptor();
        this.payload = source.getPayload() != null ? source.getPayload().copy() : null;
        this.user = source.getUser();
        this.author = source.getAuthor();
    }

    public ScheduledJob build()
    {
        return ScheduledJob.create().
            name( source.getName() ).
            description( description ).
            frequency( frequency ).
            enabled( enabled ).
            timeZone( timeZone ).
            descriptor( descriptor ).
            payload( payload ).
            user( user ).
            author( author ).
            build();
    }
}
