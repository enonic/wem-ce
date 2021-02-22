package com.enonic.xp.scheduler;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.TimeZone;

import com.google.common.base.Preconditions;

public abstract class ScheduleCalendar
    implements Serializable
{
    public static ScheduleCalendar cron( final String value, final TimeZone timeZone )
    {
        if ( CronCalendar.isCronValue( value ) )
        {
            return CronCalendar.create().
                value( value ).
                timeZone( timeZone ).
                build();
        }
        throw new IllegalArgumentException( String.format( "'value' param is not a cron value: '%s'", value ) );
    }
//
//    public static ScheduleCalendar oneTime(final String value) {
//
//    }

    public abstract String getStringValue();

    public abstract Optional<Duration> nextExecution();

    public abstract static class Builder<B extends Builder<B>>
    {
        public String value;

        public B value( final String value )
        {
            this.value = value;
            return (B) this;
        }

        protected void validate()
        {
            Preconditions.checkNotNull( value, "value must be set." );
        }

        public abstract ScheduleCalendar build();
    }
}
