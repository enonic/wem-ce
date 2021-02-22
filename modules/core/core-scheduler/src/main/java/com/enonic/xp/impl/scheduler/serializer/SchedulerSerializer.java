package com.enonic.xp.impl.scheduler.serializer;

import java.util.Optional;
import java.util.TimeZone;

import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.scheduler.ScheduledJobPropertyNames;
import com.enonic.xp.node.Node;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.scheduler.CreateScheduledJobParams;
import com.enonic.xp.scheduler.CronCalendar;
import com.enonic.xp.scheduler.EditableScheduledJob;
import com.enonic.xp.scheduler.ModifyScheduledJobParams;
import com.enonic.xp.scheduler.ScheduleCalendar;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.ScheduledJobEditor;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.security.PrincipalKey;

import static com.google.common.base.Strings.nullToEmpty;

public class SchedulerSerializer
{

    public static PropertyTree toCreateNodeData( final CreateScheduledJobParams params )
    {
        final PropertyTree tree = new PropertyTree();
        final PropertySet data = tree.getRoot();

        data.ifNotNull().addString( ScheduledJobPropertyNames.DESCRIPTION, params.getDescription() );
        data.addBoolean( ScheduledJobPropertyNames.ENABLED, params.isEnabled() );

        addCalendar( params, data );

        if ( params.getDescriptor() != null )
        {
            data.addString( ScheduledJobPropertyNames.DESCRIPTOR, params.getDescriptor().toString() );
        }
        if ( params.getPayload() != null )
        {
            data.addSet( ScheduledJobPropertyNames.PAYLOAD, params.getPayload().getRoot().copy( data.getTree() ) );
        }

        if ( params.getAuthor() != null )
        {
            data.addString( ScheduledJobPropertyNames.AUTHOR, params.getAuthor().toString() );
        }
        if ( params.getUser() != null )
        {
            data.addString( ScheduledJobPropertyNames.USER, params.getUser().toString() );
        }

        return tree;
    }

    public static PropertyTree toUpdateNodeData( final ModifyScheduledJobParams params, final ScheduledJob original )
    {
        final ScheduledJob modifiedJob = editScheduledJob( params.getEditor(), original );

        final PropertyTree tree = new PropertyTree();
        final PropertySet data = tree.getRoot();

        data.ifNotNull().addString( ScheduledJobPropertyNames.DESCRIPTION, modifiedJob.getDescription() );
        data.addBoolean( ScheduledJobPropertyNames.ENABLED, modifiedJob.isEnabled() );

        addCalendar( modifiedJob, data );

        if ( modifiedJob.getDescriptor() != null )
        {
            data.addString( ScheduledJobPropertyNames.DESCRIPTOR, modifiedJob.getDescriptor().toString() );
        }
        if ( modifiedJob.getPayload() != null )
        {
            data.addSet( ScheduledJobPropertyNames.PAYLOAD, modifiedJob.getPayload().getRoot().copy( data.getTree() ) );
        }

        if ( modifiedJob.getAuthor() != null )
        {
            data.addString( ScheduledJobPropertyNames.AUTHOR, modifiedJob.getAuthor().toString() );
        }
        if ( modifiedJob.getUser() != null )
        {
            data.addString( ScheduledJobPropertyNames.USER, modifiedJob.getUser().toString() );
        }

        return tree;
    }

    public static ScheduledJob fromNode( final Node node )
    {
        final PropertySet data = node.data().getRoot();

        return ScheduledJob.create().
            name( SchedulerName.from( node.name().toString() ) ).
            description( data.getString( ScheduledJobPropertyNames.DESCRIPTION ) ).
            enabled( data.getBoolean( ScheduledJobPropertyNames.ENABLED ) ).
            calendar( Optional.ofNullable( data.getSet( ScheduledJobPropertyNames.CALENDAR ) ).
                map( SchedulerSerializer::createCalendar ).
                orElse( null ) ).
            descriptor( Optional.ofNullable( data.getString( ScheduledJobPropertyNames.DESCRIPTOR ) ).
                map( DescriptorKey::from ).
                orElse( null ) ).
            payload( Optional.ofNullable( data.getSet( ScheduledJobPropertyNames.PAYLOAD ) ).
                map( PropertySet::toTree ).
                orElse( null ) ).
            author( Optional.ofNullable( data.getString( ScheduledJobPropertyNames.AUTHOR ) ).
                map( PrincipalKey::from ).
                orElse( null ) ).
            user( Optional.ofNullable( data.getString( ScheduledJobPropertyNames.USER ) ).
                map( PrincipalKey::from ).
                orElse( null ) ).
            build();
    }

    private static ScheduledJob editScheduledJob( final ScheduledJobEditor editor, final ScheduledJob original )
    {
        final EditableScheduledJob editableJob = new EditableScheduledJob( original );
        if ( editor != null )
        {
            editor.edit( editableJob );
        }
        return editableJob.build();
    }

    private static void addCalendar( final ScheduledJob modifiedJob, final PropertySet data )
    {
        if ( modifiedJob.getCalendar() != null )
        {
            final PropertySet calendarSet = new PropertySet();
            calendarSet.addString( ScheduledJobPropertyNames.CALENDAR_VALUE, modifiedJob.getCalendar().getStringValue() );
            if ( modifiedJob.getCalendar() instanceof CronCalendar )
            {

                calendarSet.addString( ScheduledJobPropertyNames.CALENDAR_TIMEZONE,
                                       ( (CronCalendar) modifiedJob.getCalendar() ).getTimeZone().getID() );
            }
            data.addSet( ScheduledJobPropertyNames.CALENDAR, calendarSet );
        }
    }

    private static void addCalendar( final CreateScheduledJobParams params, final PropertySet data )
    {
        final PropertySet calendarSet = new PropertySet();
        if ( params.getCalendar() instanceof CronCalendar )
        {
            final CronCalendar cronCalendar = (CronCalendar) params.getCalendar();

            calendarSet.addString( ScheduledJobPropertyNames.CALENDAR_VALUE, cronCalendar.getStringValue() );
            calendarSet.addString( ScheduledJobPropertyNames.CALENDAR_TIMEZONE, cronCalendar.getTimeZone().getID() );
        }
        data.addSet( ScheduledJobPropertyNames.CALENDAR, calendarSet );
    }

    private static ScheduleCalendar createCalendar( final PropertySet data )
    {

        if ( data != null )
        {
            final String value = data.getString( ScheduledJobPropertyNames.CALENDAR_VALUE );
            final String timeZone = data.getString( ScheduledJobPropertyNames.CALENDAR_TIMEZONE );

            if ( CronCalendar.isCronValue( value ) )
            {

                final CronCalendar.Builder calendar = CronCalendar.create().value( value );

                if ( !nullToEmpty( timeZone ).isBlank() )
                {
                    calendar.timeZone( TimeZone.getTimeZone( timeZone ) );
                }

                return calendar.build();
            }

            return null; // TODO: return OneTimeCalendar
        }

        return null;
    }
}
