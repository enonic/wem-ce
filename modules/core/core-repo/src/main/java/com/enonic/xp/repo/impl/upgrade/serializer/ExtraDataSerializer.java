package com.enonic.xp.repo.impl.upgrade.serializer;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.content.ExtraData;
import com.enonic.xp.content.ExtraDatas;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.schema.xdata.XDataName;

import static com.enonic.xp.content.ContentPropertyNames.EXTRA_DATA;

final class ExtraDataSerializer
    extends AbstractDataSetSerializer<ExtraDatas>
{
    @Override
    public void toData( final ExtraDatas extraDatas, final PropertySet parent )
    {
        final PropertySet metaSet = parent.addSet( EXTRA_DATA );
        for ( final ExtraData extraData : extraDatas )
        {

            final String xDataApplicationPrefix = extraData.getApplicationPrefix();
            PropertySet xDataApplication = metaSet.getSet( xDataApplicationPrefix );
            if ( xDataApplication == null )
            {
                xDataApplication = metaSet.addSet( xDataApplicationPrefix );
            }
            xDataApplication.addSet( extraData.getName().getLocalName(), extraData.getData().getRoot().copy( metaSet.getTree() ) );
        }
    }

    @Override
    public ExtraDatas fromData( final PropertySet metadataSet )
    {
        if ( metadataSet != null )
        {
            final ExtraDatas.Builder extradatasBuilder = ExtraDatas.create();
            for ( final String metadataApplicationPrefix : metadataSet.getPropertyNames() )
            {
                final PropertySet xDataApplication = metadataSet.getSet( metadataApplicationPrefix );
                for ( final String metadataLocalName : xDataApplication.getPropertyNames() )
                {

                    final ApplicationKey applicationKey = ExtraData.fromApplicationPrefix( metadataApplicationPrefix );

                    final XDataName metadataName = XDataName.from( applicationKey, metadataLocalName );
                    extradatasBuilder.add( new ExtraData( metadataName, xDataApplication.getSet( metadataLocalName ).toTree() ) );
                }
            }

            return extradatasBuilder.build();
        }
        return null;
    }

}
