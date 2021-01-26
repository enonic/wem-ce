package com.enonic.xp.scheduler;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.security.PrincipalKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EditableScheduledJobTest
{
    @Test
    public void testNull()
    {
        assertThrows( NullPointerException.class, () -> new EditableScheduledJob( null ) );
    }

    @Test
    public void testEmpty()
    {
        final SchedulerName name = SchedulerName.from( "schedulerName" );
        final ScheduledJob source = ScheduledJob.create().name( name ).build();

        final ScheduledJob target = new EditableScheduledJob( source ).build();

        assertEquals( name, target.getName() );
        assertNull( target.getAuthor() );
        assertNull( target.getPayload() );
        assertNull( target.getUser() );
        assertNull( target.getDescriptor() );
        assertNull( target.getDescription() );
        assertNull( target.getTimeZone() );
        assertNull( target.getFrequency() );
        assertFalse( source.isEnabled() );
    }

    @Test
    public void testNotChanged()
    {
        final PrincipalKey author = PrincipalKey.ofUser( IdProviderKey.createDefault(), "author" );
        final PrincipalKey user = PrincipalKey.ofUser( IdProviderKey.createDefault(), "user" );

        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.BASE, "descriptor" );

        final PropertyTree payload = new PropertyTree();
        payload.addString( "property", "value" );

        final String description = "description";

        final TimeZone timeZone = TimeZone.getDefault();

        final Frequency frequency = Frequency.create().value( "value" ).build();

        final ScheduledJob source = ScheduledJob.create().
            name( SchedulerName.from( "name" ) ).
            author( author ).
            payload( payload ).
            user( user ).
            descriptor( descriptor ).
            description( description ).
            timeZone( timeZone ).
            frequency( frequency ).
            enabled( true ).
            build();

        final ScheduledJob target = new EditableScheduledJob( source ).build();

        assertEquals( source.getName(), target.getName() );
        assertEquals( source.getAuthor(), target.getAuthor() );
        assertEquals( source.getPayload(), target.getPayload() );
        assertEquals( source.getUser(), target.getUser() );
        assertEquals( source.getDescriptor(), target.getDescriptor() );
        assertEquals( source.getDescription(), target.getDescription() );
        assertEquals( source.getTimeZone(), target.getTimeZone() );
        assertEquals( source.getFrequency(), target.getFrequency() );
        assertEquals( source.isEnabled(), target.isEnabled() );
    }

    @Test
    public void testChanged()
    {
        final ScheduledJob source = ScheduledJob.create().
            name( SchedulerName.from( "name" ) ).
            build();

        final PrincipalKey author = PrincipalKey.ofUser( IdProviderKey.createDefault(), "author" );
        final PrincipalKey user = PrincipalKey.ofUser( IdProviderKey.createDefault(), "user" );

        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.BASE, "descriptor" );

        final PropertyTree payload = new PropertyTree();
        payload.addString( "property", "value" );

        final String description = "description";

        final TimeZone timeZone = TimeZone.getDefault();

        final Frequency frequency = Frequency.create().value( "value" ).build();

        final EditableScheduledJob editable = new EditableScheduledJob( source );
        editable.author = author;
        editable.payload = payload;
        editable.user = user;
        editable.descriptor = descriptor;
        editable.description = description;
        editable.timeZone = timeZone;
        editable.frequency = frequency;
        editable.enabled = true;

        final ScheduledJob target = editable.build();

        assertEquals( source.getName(), target.getName() );
        assertEquals( author, target.getAuthor() );
        assertEquals( payload, target.getPayload() );
        assertEquals( user, target.getUser() );
        assertEquals( descriptor, target.getDescriptor() );
        assertEquals( description, target.getDescription() );
        assertEquals( timeZone, target.getTimeZone() );
        assertEquals( frequency, target.getFrequency() );
        assertTrue( target.isEnabled() );
    }
}

