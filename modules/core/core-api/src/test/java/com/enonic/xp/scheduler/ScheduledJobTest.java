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

public class ScheduledJobTest
{
    @Test
    public void testName()
    {
        assertThrows( NullPointerException.class, () -> ScheduledJob.create().build() );

        final SchedulerName name = SchedulerName.from( "name" );
        assertEquals( name, ScheduledJob.create().name( name ).build().getName() );
    }

    @Test
    public void testEmptyBuilder()
    {
        final ScheduledJob job = ScheduledJob.create().
            name( SchedulerName.from( "name" ) ).build();

        assertNull( job.getAuthor() );
        assertNull( job.getPayload() );
        assertNull( job.getUser() );
        assertNull( job.getDescriptor() );
        assertNull( job.getDescription() );
        assertNull( job.getTimeZone() );
        assertNull( job.getFrequency() );
        assertFalse( job.isEnabled() );
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

        final ScheduledJob job = ScheduledJob.create().
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

        assertEquals( author, job.getAuthor() );
        assertEquals( payload, job.getPayload() );
        assertEquals( user, job.getUser() );
        assertEquals( descriptor, job.getDescriptor() );
        assertEquals( description, job.getDescription() );
        assertEquals( timeZone, job.getTimeZone() );
        assertEquals( frequency, job.getFrequency() );
        assertTrue( job.isEnabled() );


    }

}

