package com.enonic.xp.repo.impl.upgrade.serializer;

import com.enonic.xp.data.PropertySet;

public abstract class AbstractDataSetSerializer<DATA>
{
    public abstract void toData( final DATA in, final PropertySet parent );

    public abstract DATA fromData( final PropertySet data );

}
