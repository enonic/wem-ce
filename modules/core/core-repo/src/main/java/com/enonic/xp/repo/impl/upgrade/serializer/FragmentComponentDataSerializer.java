package com.enonic.xp.repo.impl.upgrade.serializer;

import com.enonic.xp.content.ContentId;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.region.FragmentComponent;
import com.enonic.xp.region.FragmentComponentType;
import com.enonic.xp.util.Reference;

final class FragmentComponentDataSerializer
    extends ComponentDataSerializer<FragmentComponent>
{
    private static final String ID = "id";

    @Override
    public void applyComponentToData( final FragmentComponent component, final PropertySet asData )
    {
        final PropertySet specBlock = asData.addSet( component.getType().toString() );

        if ( component.getFragment() != null )
        {
            specBlock.addReference( ID, Reference.from( component.getFragment().toString() ) );
        }
    }

    @Override
    public FragmentComponent fromData( final PropertySet data )
    {
        final FragmentComponent.Builder component = FragmentComponent.create();

        final PropertySet specialBlockSet = data.getSet( FragmentComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null && specialBlockSet.isNotNull( ID ) )
        {
            final ContentId contentId = ContentId.from( specialBlockSet.getString( ID ) );
            component.fragment( contentId );
        }

        return component.build();
    }
}
