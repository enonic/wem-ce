package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.util.Set;

public interface ApplicationUrlResolver
{
    Set<String> findFiles();

    URL findUrl( final String path );

    public static String normalizePath( final String path )
    {
        if ( path.startsWith( "/" ) )
        {
            return normalizePath( path.substring( 1 ) );
        }

        if ( path.endsWith( "/" ) )
        {
            return path.substring( 0, path.length() - 1 );
        }

        return path;
    }
}
