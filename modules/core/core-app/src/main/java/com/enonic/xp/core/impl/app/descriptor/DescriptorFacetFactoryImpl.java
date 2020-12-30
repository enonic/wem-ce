package com.enonic.xp.core.impl.app.descriptor;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.core.impl.app.ApplicationRegistry;
import com.enonic.xp.descriptor.Descriptor;
import com.enonic.xp.descriptor.DescriptorLoader;
import com.enonic.xp.resource.ResourceService;

@Component
public final class DescriptorFacetFactoryImpl
    implements DescriptorFacetFactory
{
    private final ApplicationRegistry applicationRegistry;

    private final ResourceService resourceService;

    @Activate
    public DescriptorFacetFactoryImpl( @Reference final ApplicationRegistry applicationRegistry,
                                       @Reference final ResourceService resourceService )
    {
        this.applicationRegistry = applicationRegistry;
        this.resourceService = resourceService;
    }

    @Override
    public <T extends Descriptor> DescriptorFacet<T> create( final DescriptorLoader<T> loader )
    {
        final DescriptorFacetImpl<T> facet = new DescriptorFacetImpl<>( loader );
        facet.applicationRegistry = this.applicationRegistry;
        facet.resourceService = this.resourceService;
        return facet;
    }
}
