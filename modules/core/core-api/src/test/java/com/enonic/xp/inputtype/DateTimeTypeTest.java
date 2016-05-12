package com.enonic.xp.inputtype;

import org.junit.Test;

import com.enonic.xp.data.Value;
import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.data.ValueTypes;

import static org.junit.Assert.*;

public class DateTimeTypeTest
    extends BaseInputTypeTest
{
    public DateTimeTypeTest()
    {
        super( DateTimeType.INSTANCE );
    }

    @Test
    public void testName()
    {
        assertEquals( "DateTime", this.type.getName().toString() );
    }

    @Test
    public void testToString()
    {
        assertEquals( "DateTime", this.type.toString() );
    }

    @Test
    public void testCreateProperty()
    {
        final InputTypeConfig config = newEmptyConfig();
        final Value value = this.type.createValue( ValueFactory.newString( "2015-01-02T22:11:00" ), config );

        assertNotNull( value );
        assertSame( ValueTypes.LOCAL_DATE_TIME, value.getType() );
    }

    @Test
    public void testCreateProperty_withTimezone()
    {
        final InputTypeConfig config = newFullConfig();
        final Value value = this.type.createValue( ValueFactory.newString( "2015-01-02T22:11:00Z" ), config );

        assertNotNull( value );
        assertSame( ValueTypes.DATE_TIME, value.getType() );
    }

    @Test
    public void testCreateDefaultValue()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-08-16T05:03:45" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.LOCAL_DATE_TIME, value.getType() );
        assertEquals( value.toString(), "2014-08-16T05:03:45" );

    }

    @Test
    public void testCreateDefaultValue_withTimezone_format1()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-08-16T10:03:45Z" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.DATE_TIME, value.getType() );
        assertEquals( value.toString(), "2014-08-16T10:03:45Z" );

    }

    @Test
    public void testCreateDefaultValue_withTimezone_format2_plus()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-08-16T10:03:45+03:00" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.DATE_TIME, value.getType() );
        assertEquals( value.toString(), "2014-08-16T07:03:45Z" );

    }

    @Test
    public void testCreateDefaultValue_withTimezone_format2_minus()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-08-16T10:03:45-03:00" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.DATE_TIME, value.getType() );
        assertEquals( value.toString(), "2014-08-16T13:03:45Z" );
    }

    @Test
    public void testCreateDefaultValue_withTimezone_format2_day_change()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-08-16T22:03:45-03:00" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.DATE_TIME, value.getType() );
        assertEquals( value.toString(), "2014-08-17T01:03:45Z" );
    }




    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultValue_invalid()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "2014-18-16T05:03:45" ).
                build() ).
            build();

        this.type.createDefaultValue( config );
    }

    @Test
    public void testRelativeDefaultValue_only_relative_date_exists()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "+1year -5months -36d" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.LOCAL_DATE_TIME, value.getType() );
    }

    @Test
    public void testRelativeDefaultValue_only_relative_time_exists()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "+1hour -5minutes -36s" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.LOCAL_DATE_TIME, value.getType() );
    }

    @Test
    public void testRelativeDefaultValue_date_time()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "+1year -5months -36d +2minutes -1h" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );

        assertNotNull( value );
        assertSame( ValueTypes.LOCAL_DATE_TIME, value.getType() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativeDefaultValue_date_time_invalid()
    {
        final InputTypeDefault config = InputTypeDefault.create().
            property( InputTypeProperty.create( "default", "+1year -5months -36d +2minutes -1haaur" ).
                build() ).
            build();

        final Value value = this.type.createDefaultValue( config );
    }

    @Test
    public void testValidate_dateTime()
    {
        final InputTypeConfig config = newEmptyConfig();
        this.type.validate( dateTimeProperty(), config );
    }

    @Test
    public void testValidate_localDateTime()
    {
        final InputTypeConfig config = newEmptyConfig();
        this.type.validate( localDateTimeProperty(), config );
    }

    @Test(expected = InputTypeValidationException.class)
    public void testValidate_invalidType()
    {
        final InputTypeConfig config = newEmptyConfig();
        this.type.validate( booleanProperty( true ), config );
    }

    private InputTypeConfig newEmptyConfig()
    {
        return InputTypeConfig.create().build();
    }

    private InputTypeConfig newFullConfig()
    {
        return InputTypeConfig.create().
            property( InputTypeProperty.create( "timezone", "true" ).build() ).
            build();
    }
}