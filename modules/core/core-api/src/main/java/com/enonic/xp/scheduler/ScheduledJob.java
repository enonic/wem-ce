package com.enonic.xp.scheduler;

import java.util.TimeZone;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.security.PrincipalKey;

@PublicApi
public final class ScheduledJob
{
    private final SchedulerName name;

    private final String description;

    private final Frequency frequency;

    private final boolean enabled;

    private final TimeZone timeZone;

    private final DescriptorKey descriptor;

    private final PropertyTree payload;

    private final PrincipalKey user;

    private final PrincipalKey author;

    private ScheduledJob( final Builder builder )
    {
        this.name = builder.name;
        this.description = builder.description;
        this.frequency = builder.frequency;
        this.enabled = builder.enabled;
        this.timeZone = builder.timeZone;
        this.descriptor = builder.descriptor;
        this.payload = builder.payload;
        this.user = builder.user;
        this.author = builder.author;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public SchedulerName getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Frequency getFrequency()
    {
        return frequency;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    public DescriptorKey getDescriptor()
    {
        return descriptor;
    }

    public PropertyTree getPayload()
    {
        return payload;
    }

    public PrincipalKey getUser()
    {
        return user;
    }

    public PrincipalKey getAuthor()
    {
        return author;
    }

    public static class Builder
    {
        private SchedulerName name;

        private String description;

        private Frequency frequency;

        private boolean enabled;

        private TimeZone timeZone;

        private DescriptorKey descriptor;

        private PropertyTree payload;

        private PrincipalKey user;

        private PrincipalKey author;

        public Builder()
        {
            super();
        }

        public Builder name( final SchedulerName name )
        {
            this.name = name;
            return this;
        }

        public Builder description( final String description )
        {
            this.description = description;
            return this;
        }

        public Builder frequency( final Frequency frequency )
        {
            this.frequency = frequency;
            return this;
        }

        public Builder enabled( final boolean enabled )
        {
            this.enabled = enabled;
            return this;
        }

        public Builder timeZone( final TimeZone timeZone )
        {
            this.timeZone = timeZone;
            return this;
        }

        public Builder descriptor( final DescriptorKey descriptor )
        {
            this.descriptor = descriptor;
            return this;
        }

        public Builder payload( final PropertyTree payload )
        {
            this.payload = payload;
            return this;
        }

        public Builder user( final PrincipalKey user )
        {
            this.user = user;
            return this;
        }

        public Builder author( final PrincipalKey author )
        {
            this.author = author;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( name, "name must be set." );
        }

        public ScheduledJob build()
        {
            validate();
            return new ScheduledJob( this );
        }
    }
}
