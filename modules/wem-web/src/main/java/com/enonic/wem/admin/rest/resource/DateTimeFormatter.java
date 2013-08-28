package com.enonic.wem.admin.rest.resource;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeFormatter
{
    private static final org.joda.time.format.DateTimeFormatter isoDateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();

    public static String format( DateTime dateTime )
    {
        return isoDateTimeFormatter.print( dateTime );
    }

    public static DateTime parse( String dateTimeString )
    {
        if ( dateTimeString == null || dateTimeString.isEmpty() )
        {
            return null;
        }

        try
        {
            return isoDateTimeFormatter.parseDateTime( dateTimeString );
        }
        catch ( Exception e )
        {
            return null;
        }
    }
}
