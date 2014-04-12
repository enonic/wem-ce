package com.enonic.wem.core.content.page.part;

import javax.inject.Inject;

import com.enonic.wem.api.content.page.part.PartDescriptor;
import com.enonic.wem.api.content.page.part.PartDescriptorKey;
import com.enonic.wem.api.content.page.part.PartDescriptorService;
import com.enonic.wem.api.content.page.part.PartDescriptors;
import com.enonic.wem.api.module.ModuleKeys;
import com.enonic.wem.api.module.ModuleService;
import com.enonic.wem.api.resource.ResourceService;

public final class PartDescriptorServiceImpl
    implements PartDescriptorService
{
    @Inject
    protected ModuleService moduleService;

    @Inject
    protected ResourceService resourceService;

    @Inject
    protected PartDescriptorService partDescriptorService;

    public PartDescriptor getByKey( final PartDescriptorKey key )
    {
        return new GetPartDescriptorCommand().moduleService( this.moduleService ).resourceService( this.resourceService ).key(
            key ).execute();
    }

    public PartDescriptors getByModules( final ModuleKeys moduleKeys )
    {
        return new GetPartDescriptorsByModulesCommand().moduleService( this.moduleService ).resourceService(
            this.resourceService ).moduleKeys( moduleKeys ).execute();
    }
}
