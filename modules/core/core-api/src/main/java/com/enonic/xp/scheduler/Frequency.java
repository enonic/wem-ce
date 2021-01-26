package com.enonic.xp.scheduler;

import java.util.Objects;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class Frequency
{
    private final String value;

    private Frequency( final Builder builder )
    {
        this.value = builder.value;
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
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final Frequency frequency = (Frequency) o;
        return Objects.equals( value, frequency.value );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( value );
    }

    @Override
    public String toString()
    {
        return value;
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
            Preconditions.checkNotNull( value, "value must be set." );
        }

        public Frequency build()
        {
            validate();
            return new Frequency( this );
        }
    }
}
