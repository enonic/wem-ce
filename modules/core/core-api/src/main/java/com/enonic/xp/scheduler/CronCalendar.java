package com.enonic.xp.scheduler;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Preconditions;

public class CronCalendar
    extends ScheduleCalendar
    implements Serializable
{
    private static final long serialVersionUID = 0;

    private final static CronDefinition DEFINITION = CronDefinitionBuilder.instanceDefinitionFor( CronType.UNIX );

    private final static CronParser PARSER = new CronParser( DEFINITION );

    private final Cron cron;

    private final TimeZone timeZone;

    private final ExecutionTime executionTime;

    public CronCalendar( final Builder builder )
    {
        this.timeZone = builder.timeZone;
        this.cron = PARSER.parse( builder.value );
        this.executionTime = ExecutionTime.forCron( this.cron );
    }

    public static boolean isCronValue( final String value )
    {
        try
        {
            PARSER.parse( value );
            return true;
        }
        catch ( IllegalArgumentException e )
        {
            return false;
        }
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public Optional<Duration> nextExecution()
    {
        return this.executionTime.timeToNextExecution( ZonedDateTime.now() );
    }

    @Override
    public String getStringValue()
    {
        return cron.asString();
    }

    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    private Object writeReplace()
    {
        return new SerializedForm( this );
    }

    private static class SerializedForm
        implements Serializable
    {
        private static final long serialVersionUID = 0;

        private final String value;

        private final String timezone;

        public SerializedForm( final CronCalendar calendar )
        {
            this.value = calendar.getStringValue();
            this.timezone = calendar.getTimeZone().getID();
        }

        private Object readResolve()
        {
            return CronCalendar.create().
                value( value ).
                timeZone( TimeZone.getTimeZone( timezone ) ).
                build();
        }
    }

    public static class Builder
        extends ScheduleCalendar.Builder<Builder>
    {
        private TimeZone timeZone;

        public Builder timeZone( final TimeZone timeZone )
        {
            if ( timeZone != null )
            {
                this.timeZone = timeZone;
            }
            return this;
        }

        @Override
        protected void validate()
        {
            super.validate();
            Preconditions.checkNotNull( timeZone, "timeZone must be set." );
        }

        @Override
        public CronCalendar build()
        {
            validate();
            return new CronCalendar( this );
        }
    }
}
