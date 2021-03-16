package com.enonic.xp.convert;

import java.util.regex.Pattern;

public class PatternConverter
    implements Converter<Pattern>
{
    @Override
    public Class<Pattern> getType()
    {
        return Pattern.class;
    }

    @Override
    public Pattern convert( final Object value )
    {
        if ( value instanceof Pattern )
        {
            return (Pattern) value;
        }

        return Pattern.compile( value.toString() );
    }
}
