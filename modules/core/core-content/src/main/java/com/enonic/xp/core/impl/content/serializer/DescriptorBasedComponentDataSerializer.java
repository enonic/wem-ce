package com.enonic.xp.core.impl.content.serializer;


import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.region.DescriptorBasedComponent;

abstract class DescriptorBasedComponentDataSerializer<DATA extends DescriptorBasedComponent>
    extends ComponentDataSerializer<DATA>
{
    public static final String DESCRIPTOR = "descriptor";

    public static final String CONFIG = "config";

    protected void applyComponentToData( final DescriptorBasedComponent component, final PropertySet asData )
    {
        if ( !component.hasDescriptor() )
        {
            return;
        }

        final PropertySet specBlock = asData.addSet( component.getType().toString() );

        specBlock.setString( DESCRIPTOR, component.getDescriptor().toString() );

        if ( component.hasConfig() )
        {
            final String appKeyAsString = appNameToConfigPropertyName( component.getDescriptor().getApplicationKey().toString() );
            final PropertySet configSet = specBlock.addSet( CONFIG ).addSet( appKeyAsString );
            final String componentName = appNameToConfigPropertyName( component.getDescriptor().getName() );
            configSet.addSet( componentName, component.getConfig().getRoot().copy( asData.getTree() ) );
        }
    }

    public static PropertyTree getConfigFromData( final PropertySet specialBlockSet, final DescriptorKey descriptorKey )
    {
        if ( specialBlockSet.hasProperty( CONFIG ) )
        {

            final String appKeyAsString = appNameToConfigPropertyName( descriptorKey.getApplicationKey().toString() );
            final String componentName = appNameToConfigPropertyName( descriptorKey.getName() );

            final PropertySet configSet = specialBlockSet.getSet( CONFIG ).getSet( appKeyAsString );

            if ( configSet.hasProperty( componentName ) )
            {
                return configSet.getSet( componentName ).toTree();
            }
        }

        return null;
    }

    public static String appNameToConfigPropertyName( final String appKey )
    {
        return appKey.replace( ".", "-" );
    }
}
