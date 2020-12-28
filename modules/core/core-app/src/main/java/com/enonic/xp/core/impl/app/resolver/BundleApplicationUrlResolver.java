package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableSet;

public final class BundleApplicationUrlResolver
    implements ApplicationUrlResolver
{
    private final Bundle bundle;

    private ImmutableSet<String> files;

    public BundleApplicationUrlResolver( final Bundle bundle )
    {
        this.bundle = bundle;
    }

    @Override
    public Set<String> findFiles()
    {
        if ( this.files == null )
        {
            this.files = doFindFiles();
        }

        return this.files;
    }

    private ImmutableSet<String> doFindFiles()
    {
        final Iterator<URL> urls = this.bundle.findEntries( "/", "*", true ).asIterator();
        return StreamSupport.stream( Spliterators.spliteratorUnknownSize( urls, Spliterator.ORDERED ), false ).
            map( url -> url.getFile() ).
            filter( name -> !name.endsWith( "/" ) ).
            map( name -> name.substring( 1 ) ).
            collect( ImmutableSet.toImmutableSet() );
    }

    @Override
    public URL findUrl( final String path )
    {
        final String normalized = ApplicationUrlResolver.normalizePath( path );
        return this.bundle.getResource( normalized );
    }
}
