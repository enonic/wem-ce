package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.util.Set;

public interface ApplicationUrlResolver
{
    Set<String> findFiles();

    URL findUrl( final String path );

    public static String normalizePath( final String path )
    {
        return stripLeasdingSlashes( path );
    }

    private static String stripLeasdingSlashes( String str )
    {
        int length = str.length();
        int pos = 0;
        while ( pos != length && str.charAt( pos ) == '/' )
        {
            pos++;
        }
        return str.substring( pos );
    }
}
