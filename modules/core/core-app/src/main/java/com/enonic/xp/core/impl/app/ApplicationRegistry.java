package com.enonic.xp.core.impl.app;

import org.osgi.framework.Bundle;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.app.Applications;
import com.enonic.xp.config.Configuration;

public interface ApplicationRegistry
{
    ApplicationKeys getKeys();

    ApplicationImpl get( ApplicationKey key );

    Applications getAll();

    Application installApplication( Bundle bundle );

    void uninstallApplication( ApplicationKey applicationKey );

    void stopApplication( ApplicationKey applicationKey );

    boolean startApplication( ApplicationKey applicationKey, boolean throwOnInvalidVersion );

    void configureApplication( Bundle bundle, Configuration configuration );
}
