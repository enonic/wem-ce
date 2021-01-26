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

public class CreateScheduledJobParamsTest
{
    @Test
    public void testName()
    {
        assertThrows( NullPointerException.class, () -> CreateScheduledJobParams.create().build() );

        final SchedulerName name = SchedulerName.from( "schedulerName" );
        assertEquals( name, CreateScheduledJobParams.create().name( name ).build().getName() );
    }

    @Test
    public void testEmptyBuilder()
    {
        final CreateScheduledJobParams params = CreateScheduledJobParams.create().
            name( SchedulerName.from( "name" ) ).build();

        assertNull( params.getAuthor() );
        assertNull( params.getPayload() );
        assertNull( params.getUser() );
        assertNull( params.getDescriptor() );
        assertNull( params.getDescription() );
        assertNull( params.getTimeZone() );
        assertNull( params.getFrequency() );
        assertFalse( params.isEnabled() );
    }

    @Test
    public void testBuilder()
    {
        final PrincipalKey author = PrincipalKey.ofUser( IdProviderKey.createDefault(), "author" );
        final PrincipalKey user = PrincipalKey.ofUser( IdProviderKey.createDefault(), "user" );

        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.BASE, "descriptor" );

        final PropertyTree payload = new PropertyTree();
        payload.addString( "property", "value" );

        final String description = "description";

        final TimeZone timeZone = TimeZone.getDefault();

        final Frequency frequency = Frequency.create().value( "value" ).build();

        final CreateScheduledJobParams params = CreateScheduledJobParams.create().
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

        assertEquals( author, params.getAuthor() );
        assertEquals( payload, params.getPayload() );
        assertEquals( user, params.getUser() );
        assertEquals( descriptor, params.getDescriptor() );
        assertEquals( description, params.getDescription() );
        assertEquals( timeZone, params.getTimeZone() );
        assertEquals( frequency, params.getFrequency() );
        assertTrue( params.isEnabled() );
    }
}

