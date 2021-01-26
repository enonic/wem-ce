package com.enonic.xp.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModifyScheduledJobParamsTest
{
    @Test
    public void testInvalid()
    {
        assertThrows( NullPointerException.class, () -> ModifyScheduledJobParams.create().build() );
        assertThrows( NullPointerException.class,
                      () -> ModifyScheduledJobParams.create().name( SchedulerName.from( "schedulerName" ) ).build() );
        assertThrows( NullPointerException.class, () -> ModifyScheduledJobParams.create().editor( ( edit ) -> {
        } ).build() );
    }


    @Test
    public void testBuilder()
    {
        final ScheduledJobEditor editor = edit -> {
        };
        final SchedulerName name = SchedulerName.from( "name" );

        final ModifyScheduledJobParams params = ModifyScheduledJobParams.create().
            name( name ).
            editor( editor ).
            build();

        assertEquals( name, params.getName() );
        assertSame( editor, params.getEditor() );
    }
}

