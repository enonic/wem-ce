package com.enonic.xp.core.impl.content;

import java.time.Instant;

import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.NodeDataProcessor;
import com.enonic.xp.security.User;

public class DuplicateContentProcessor
    implements NodeDataProcessor
{
    @Override
    public PropertyTree process( final PropertyTree originalData )
    {
        final User user = ContextAccessor.current().getAuthInfo().getUser();

        final PropertyTree data = originalData.copy();

        data.setInstant( ContentPropertyNames.CREATED_TIME, Instant.now() );
        data.setInstant( ContentPropertyNames.MODIFIED_TIME, Instant.now() );
        data.setString( ContentPropertyNames.OWNER, user.getKey().toString() );
        data.setString( ContentPropertyNames.CREATOR, user.getKey().toString() );
        data.setString( ContentPropertyNames.MODIFIER, user.getKey().toString() );
        if ( data.hasProperty( ContentPropertyNames.PUBLISH_INFO ) )
        {
            data.removeProperty( ContentPropertyNames.PUBLISH_INFO );
        }

        if ( data.hasProperty( ContentPropertyNames.INHERIT ) )
        {
            data.removeProperties( ContentPropertyNames.INHERIT );
        }

        if ( data.hasProperty( ContentPropertyNames.ORIGIN_PROJECT ) )
        {
            data.removeProperty( ContentPropertyNames.ORIGIN_PROJECT );
        }

        return data;
    }
}
