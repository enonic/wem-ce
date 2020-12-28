package com.enonic.xp.page;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.util.CharacterChecker;

@PublicApi
public final class DescriptorKey
    implements Serializable
{
    private static final long serialVersionUID = 0;

    protected static final String SEPARATOR = ":";

    private final ApplicationKey applicationKey;

    private final String name;

    private final String refString;

    private DescriptorKey( final ApplicationKey applicationKey, final String name )
    {
        this.applicationKey = applicationKey;
        this.name = CharacterChecker.check( name, "Not a valid name for DescriptorKey [" + name + "]" );
        this.refString = applicationKey + SEPARATOR + name;
    }

    public ApplicationKey getApplicationKey()
    {
        return applicationKey;
    }

    public String getName()
    {
        return name;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final DescriptorKey that = (DescriptorKey) o;
        return Objects.equals( this.refString, that.refString );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( this.refString );
    }

    @Override
    public String toString()
    {
        return refString;
    }

    public static DescriptorKey from( final String key )
    {
        Preconditions.checkNotNull( key, "DescriptorKey can't be null" );
        final int index = key.indexOf( SEPARATOR );
        final String applicationKey = index == -1 ? key : key.substring( 0, index );
        final String descriptorName = index == -1 ? "" : key.substring( index + 1 );
        return new DescriptorKey( ApplicationKey.from( applicationKey ), descriptorName );
    }

    public static DescriptorKey from( final ApplicationKey applicationKey, final String descriptorName )
    {
        return new DescriptorKey( applicationKey, descriptorName );
    }
}
