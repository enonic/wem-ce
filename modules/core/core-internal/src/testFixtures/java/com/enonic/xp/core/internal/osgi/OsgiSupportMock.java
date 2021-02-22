package com.enonic.xp.core.internal.osgi;

import org.osgi.framework.Bundle;

import static org.mockito.Mockito.mock;

public class OsgiSupportMock
{
    private OsgiSupportMock()
    {
    }

    public static Bundle mockBundle()
    {
        final Bundle mock = mock( Bundle.class );
        OsgiSupport.setBundle( mock );
        return mock;
    }

    public static void reset()
    {
        OsgiSupport.setBundle( null );
    }
}
