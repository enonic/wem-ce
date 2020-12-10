package com.enonic.xp.repo.impl.upgrade.serializer;

import com.enonic.xp.data.PropertySet;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.region.PartComponent;
import com.enonic.xp.region.PartComponentType;

final class PartComponentDataSerializer
    extends DescriptorBasedComponentDataSerializer<PartComponent>
{
    @Override
    public PartComponent fromData( final PropertySet data )
    {
        final PartComponent.Builder component = PartComponent.create();

        final PropertySet specialBlockSet = data.getSet( PartComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null && specialBlockSet.isNotNull( DESCRIPTOR ) )
        {
            final DescriptorKey descriptorKey = DescriptorKey.from( specialBlockSet.getString( DESCRIPTOR ) );

            component.descriptor( descriptorKey );
            component.config( getConfigFromData( specialBlockSet, descriptorKey ) );
        }

        return component.build();
    }
}
