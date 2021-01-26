package com.enonic.xp.scheduler;


import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SchedulerNameTest
{
    @Test
    public void testCannotBeNull()
    {
        assertThrows( NullPointerException.class, () -> SchedulerName.from( null ) );
        assertThrows( NullPointerException.class, () -> SchedulerName.create().value( null ).build() );
    }

    @Test
    public void testCannotBeBlank()
    {
        assertThrows( IllegalArgumentException.class, () -> SchedulerName.from( "" ) );
        assertThrows( IllegalArgumentException.class, () -> SchedulerName.create().value( "" ).build() );
    }

    @Test
    public void testValue()
    {
        assertEquals( "schedulerName", SchedulerName.from( "schedulerName" ).getValue() );
        assertEquals( "schedulerName", SchedulerName.create().value( "schedulerName" ).build().getValue() );
    }

    @Test
    public void testToString()
    {
        assertEquals( "schedulerName", SchedulerName.from( "schedulerName" ).toString() );
        assertEquals( "schedulerName", SchedulerName.create().value( "schedulerName" ).build().toString() );
    }

    @Test
    public void equalsContract()
    {
        EqualsVerifier.forClass( SchedulerName.class ).withNonnullFields( "value" ).verify();
    }
}

