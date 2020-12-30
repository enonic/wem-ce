package com.enonic.xp.util;

import com.google.common.base.CharMatcher;

public final class CharacterChecker
{
    private static final CharMatcher ILLEGAL_CHAR_MATCHER = CharMatcher.anyOf( "<>\"'/" );

    public static String check( final String value, final String errorMessage )
    {
        if ( ILLEGAL_CHAR_MATCHER.matchesAnyOf( value ) )
        {
            throw new IllegalArgumentException( errorMessage );
        }

        return value;
    }
}
