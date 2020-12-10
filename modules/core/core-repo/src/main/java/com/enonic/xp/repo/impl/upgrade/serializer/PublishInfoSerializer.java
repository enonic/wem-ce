package com.enonic.xp.repo.impl.upgrade.serializer;

import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.ContentPublishInfo;
import com.enonic.xp.data.PropertySet;

public class PublishInfoSerializer
{

    public ContentPublishInfo serialize( final PropertySet contentAsSet )
    {
        final PropertySet publishInfo = contentAsSet.getSet( ContentPropertyNames.PUBLISH_INFO );

        if ( publishInfo == null )
        {
            return null;
        }

        return ContentPublishInfo.create().
            first( publishInfo.getInstant( ContentPropertyNames.PUBLISH_FIRST ) ).
            from( publishInfo.getInstant( ContentPropertyNames.PUBLISH_FROM ) ).
            to( publishInfo.getInstant( ContentPropertyNames.PUBLISH_TO ) ).
            build();
    }

}
