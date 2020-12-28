package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class MultiApplicationUrlResolver
    implements ApplicationUrlResolver
{
    private final ApplicationUrlResolver[] list;

    public MultiApplicationUrlResolver( final ApplicationUrlResolver... list )
    {
        this.list = list;
    }

    @Override
    public Set<String> findFiles()
    {
        return Arrays.stream( this.list ).flatMap( resolver -> resolver.findFiles().stream() ).collect( Collectors.toSet() );
    }

    @Override
    public URL findUrl( final String path )
    {
        for ( final ApplicationUrlResolver resolver : this.list )
        {
            final URL url = resolver.findUrl( path );
            if ( url != null )
            {
                return url;
            }
        }

        return null;
    }
}
