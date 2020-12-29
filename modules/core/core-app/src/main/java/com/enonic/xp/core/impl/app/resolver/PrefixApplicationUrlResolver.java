package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

public final class PrefixApplicationUrlResolver
    implements ApplicationUrlResolver
{
    private final ApplicationUrlResolver resolver;

    private final String prefix;

    public PrefixApplicationUrlResolver( final ApplicationUrlResolver resolver, final String prefix )
    {
        this.resolver = resolver;
        this.prefix = ( prefix.startsWith( "/" ) ? prefix.substring( 1 ) : prefix ) + "/";
    }

    @Override
    public Set<String> findFiles()
    {
        return this.resolver.findFiles().stream().
            filter( name -> name.startsWith( this.prefix ) ).
            map( name -> name.substring( this.prefix.length() ) ).
            collect( Collectors.toSet() );
    }

    @Override
    public URL findUrl( final String path )
    {
        final String normalized = this.prefix + ApplicationUrlResolver.normalizePath( path );
        return this.resolver.findUrl( normalized );
    }
}
