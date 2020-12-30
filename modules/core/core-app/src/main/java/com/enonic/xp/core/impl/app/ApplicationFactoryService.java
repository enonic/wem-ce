package com.enonic.xp.core.impl.app;

import org.osgi.framework.Bundle;

public interface ApplicationFactoryService
{
    ApplicationImpl getApplication( final Bundle bundle );
}
