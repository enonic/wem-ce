package com.enonic.xp.scheduler;

import java.util.Objects;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class SchedulerName
{
    private final String value;

    private SchedulerName( final Builder builder )
    {
        this.value = builder.value;
    }

    public static SchedulerName from( final String schedulerName )
    {
        return create().value( schedulerName ).build();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals( final Object o )
    {
        return ( o instanceof SchedulerName ) && Objects.equals( this.value, ( (SchedulerName) o ).value );
    }

    @Override
    public int hashCode()
    {
        return this.value.hashCode();
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    public static class Builder
    {
        private String value;

        public Builder value( final String value )
        {
            this.value = value;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( value, "Scheduler name cannot be null." );
            Preconditions.checkArgument( !value.isBlank(), "Scheduler name cannot be blank." );
        }

        public SchedulerName build()
        {
            validate();
            return new SchedulerName( this );
        }
    }
}
