package com.enonic.xp.repo.impl.upgrade.serializer;


import com.enonic.xp.data.PropertySet;
import com.enonic.xp.region.TextComponent;
import com.enonic.xp.region.TextComponentType;

final class TextComponentDataSerializer
    extends ComponentDataSerializer<TextComponent>
{
    private static final String VALUE = "value";

    @Override
    public void applyComponentToData( final TextComponent component, final PropertySet asData )
    {
        final PropertySet specBlock = asData.addSet( component.getType().toString() );

        if ( component.getText() != null )
        {
            specBlock.addString( VALUE, component.getText() );
        }
    }

    @Override
    public TextComponent fromData( final PropertySet data )
    {
        final TextComponent.Builder component = TextComponent.create();

        final PropertySet specialBlockSet = data.getSet( TextComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null && specialBlockSet.isNotNull( VALUE ) )
        {
            component.text( specialBlockSet.getString( VALUE ) );
        }

        return component.build();
    }
}
