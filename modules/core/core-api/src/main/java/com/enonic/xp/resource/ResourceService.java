package com.enonic.xp.resource;

import java.util.stream.Stream;

import com.enonic.xp.app.ApplicationKey;

public interface ResourceService
{
    Resource getResource( ResourceKey resourceKey );

    @Deprecated
    ResourceKeys findFiles( ApplicationKey key, String pattern );

    Stream<ResourceKey> findFiles( ApplicationKey key );

    <K, V> V processResource( ResourceProcessor<K, V> processor );
}
