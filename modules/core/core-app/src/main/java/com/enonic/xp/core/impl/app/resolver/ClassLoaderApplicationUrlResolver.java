package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath;

import com.enonic.xp.util.Exceptions;

public final class ClassLoaderApplicationUrlResolver
    implements ApplicationUrlResolver
{
    private final ClassLoader loader;

    public ClassLoaderApplicationUrlResolver( final URLClassLoader loader )
    {
        this.loader = loader;
    }

    @Override
    public Set<String> findFiles()
    {
        try
        {
            return doFindFiles();
        }
        catch ( final Exception e )
        {
            throw Exceptions.unchecked( e );
        }
    }

    private Set<String> doFindFiles()
        throws Exception
    {
        final ClassPath cp = ClassPath.from( this.loader );
        return cp.getResources().stream().map( ClassPath.ResourceInfo::getResourceName ).collect( Collectors.toSet() );
    }

    @Override
    public URL findUrl( final String path )
    {
        final String normalized = ApplicationUrlResolver.normalizePath( path );
        return this.loader.getResource( normalized );
    }

    public static ClassLoaderApplicationUrlResolver create( final URL... urls )
    {
        return new ClassLoaderApplicationUrlResolver( new URLClassLoader( urls, null ) );
    }
}
