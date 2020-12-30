package com.enonic.xp.core.impl.app.descriptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DescriptorLoaderMapTest
{
    @Test
    public void testAddRemove()
    {
        final DescriptorLoaderMap map = new DescriptorLoaderMap();
        map.facetFactory = new DescriptorFacetFactoryImpl( null, null );

        assertTrue( map.facet( MyDescriptor.class ) instanceof NopDescriptorFacet );

        final MyDescriptorLoader loader = new MyDescriptorLoader();
        map.add( loader );

        assertTrue( map.facet( MyDescriptor.class ) instanceof DescriptorFacetImpl );
        map.remove( loader );

        assertTrue( map.facet( MyDescriptor.class ) instanceof NopDescriptorFacet );
    }
}
