package com.enonic.xp.scheduler;


import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FrequencyTest
{
    @Test
    public void testCannotBeNull()
    {
        assertThrows( NullPointerException.class, () -> Frequency.create().value( null ).build() );
    }

    @Test
    public void testValue()
    {
        assertEquals( "value", Frequency.create().value( "value" ).build().getValue() );
    }

    @Test
    public void testToString()
    {
        assertEquals( "value", SchedulerName.create().value( "value" ).build().toString() );
    }

    @Test
    public void equalsContract()
    {
        EqualsVerifier.forClass( SchedulerName.class ).withNonnullFields( "value" ).verify();
    }
}

