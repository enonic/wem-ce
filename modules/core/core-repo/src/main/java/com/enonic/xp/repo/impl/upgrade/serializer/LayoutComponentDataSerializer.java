package com.enonic.xp.repo.impl.upgrade.serializer;


import java.util.ArrayList;
import java.util.List;

import com.enonic.xp.data.PropertySet;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.region.LayoutComponent;
import com.enonic.xp.region.LayoutComponentType;
import com.enonic.xp.region.LayoutDescriptor;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.LayoutRegions;
import com.enonic.xp.region.Region;

final class LayoutComponentDataSerializer
    extends DescriptorBasedComponentDataSerializer<LayoutComponent>
{
    private final LayoutDescriptorService layoutDescriptorService;

    private final RegionDataSerializer regionDataSerializer;

    public LayoutComponentDataSerializer( final LayoutDescriptorService layoutDescriptorService,
                                          final RegionDataSerializer regionDataSerializer )
    {
        this.layoutDescriptorService = layoutDescriptorService;
        this.regionDataSerializer = regionDataSerializer;
    }

    @Override
    public void toData( final LayoutComponent component, final PropertySet parent )
    {
        super.toData( component, parent );

        if ( component.hasRegions() )
        {
            for ( final Region region : component.getRegions() )
            {
                regionDataSerializer.toData( region, parent );
            }
        }
    }

    @Override
    public LayoutComponent fromData( final PropertySet data )
    {
        return fromData( data, new ArrayList<>() );
    }

    public LayoutComponent fromData( final PropertySet layoutData, final List<PropertySet> componentsAsData )
    {
        final LayoutComponent.Builder layoutComponent = LayoutComponent.create();

        final LayoutRegions.Builder layoutRegionsBuilder = LayoutRegions.create();

        final PropertySet specialBlockSet = layoutData.getSet( LayoutComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null && specialBlockSet.isNotNull( DESCRIPTOR ) )
        {
            final DescriptorKey descriptorKey = DescriptorKey.from( specialBlockSet.getString( DESCRIPTOR ) );

            layoutComponent.descriptor( descriptorKey );
            layoutComponent.config( getConfigFromData( specialBlockSet, descriptorKey ) );

            final LayoutDescriptor layoutDescriptor = layoutDescriptorService.getByKey( descriptorKey );

            final String layoutPath = layoutData.getString( PATH );

            if ( layoutDescriptor.getRegions() != null && layoutDescriptor.getRegions().numberOfRegions() > 0 )
            {
                layoutDescriptor.getRegions().forEach( regionDescriptor -> {
                    layoutRegionsBuilder.add( regionDataSerializer.fromData( regionDescriptor, layoutPath, componentsAsData ) );
                } );
            }
        }

        layoutComponent.regions( layoutRegionsBuilder.build() );

        return layoutComponent.build();
    }
}
