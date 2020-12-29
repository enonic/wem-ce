package com.enonic.xp.testing.resource;

import java.net.URL;
import java.util.stream.Stream;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceKeys;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;

public final class ClassLoaderResourceService
    implements ResourceService
{
    private final ClassLoader loader;

    public ClassLoaderResourceService( final ClassLoader loader )
    {
        this.loader = loader;
    }

    @Override
    public Resource getResource( final ResourceKey key )
    {
        final URL url = this.loader.getResource( key.getPath().substring( 1 ) );
        return new UrlResource( key, url );
    }

    @Override
    public ResourceKeys findFiles( final ApplicationKey key, final String pattern )
    {
        throw new IllegalStateException( "Not implemented" );
    }

    @Override
    public Stream<ResourceKey> findFiles( final ApplicationKey key )
    {
        throw new IllegalStateException( "Not implemented" );
    }

    @Override
    public <K, V> V processResource( final ResourceProcessor<K, V> processor )
    {
        throw new IllegalStateException( "Not implemented" );
    }
}
