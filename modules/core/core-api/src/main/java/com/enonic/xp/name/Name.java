package com.enonic.xp.name;

import java.util.Arrays;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public abstract class Name
{
    protected final String value;

    protected Name( final String name )
    {
        this( name, true );
    }

    protected Name( final String name, final boolean validate )
    {
        if ( validate )
        {
            validateName( name );
        }

        this.value = name;
    }

    private static void validateName( final String name )
    {
        Preconditions.checkNotNull( name, "name cannot be null" );
        Preconditions.checkArgument( !name.isBlank(), "name cannot be empty" );
        Preconditions.checkArgument( NameCharacterHelper.hasNoExplicitIllegal( name ), "Invalid name: '" + name + "'. Cannot contain " +
            Arrays.toString( NameCharacterHelper.getExplicitlyIllegalCharacters() ) );
        checkValidName( name );
    }

    private static void checkValidName( final String value )
    {
        for ( final char c : value.toCharArray() )
        {
            if ( !NameCharacterHelper.isValidCharacter( c ) )
            {
                final String unicodeChar = c > 255 ? " (" + NameCharacterHelper.getUnicodeString( c ) + ")" : "";
                throw new IllegalArgumentException( "Invalid character in name: '" + c + "'" + unicodeChar );
            }
        }
    }

    @Override
    public final boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final Name name = (Name) o;

        return value != null ? value.equals( name.value ) : name.value == null;
    }

    @Override
    public final int hashCode()
    {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public final String toString()
    {
        return value;
    }
}
