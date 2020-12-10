package com.enonic.xp.repo.impl.upgrade.serializer;


import com.enonic.xp.content.ContentId;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.region.ImageComponent;
import com.enonic.xp.region.ImageComponentType;
import com.enonic.xp.util.Reference;

final class ImageComponentDataSerializer
    extends ComponentDataSerializer<ImageComponent>
{
    private static final String ID = "id";

    private static final String CAPTION = "caption";

    @Override
    public void applyComponentToData( final ImageComponent component, final PropertySet asData )
    {
        if ( !component.hasImage() )
        {
            return;
        }

        final PropertySet specBlock = asData.addSet( component.getType().toString() );

        specBlock.addReference( ID, Reference.from( component.getImage().toString() ) );

        if ( component.hasCaption() )
        {
            specBlock.addString( CAPTION, component.getCaption() );
        }
    }

    @Override
    public ImageComponent fromData( final PropertySet data )
    {
        final ImageComponent.Builder component = ImageComponent.create();

        final PropertySet specialBlockSet = data.getSet( ImageComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null )
        {
            if ( specialBlockSet.isNotNull( ID ) )
            {
                final ContentId contentId = ContentId.from( specialBlockSet.getString( ID ) );
                component.image( contentId );
            }

            if ( specialBlockSet.hasProperty( CAPTION ) )
            {
                final PropertyTree config = new PropertyTree();
                config.addString( CAPTION, specialBlockSet.getString( CAPTION ) );
                component.config( config );
            }
        }

        return component.build();
    }
}
