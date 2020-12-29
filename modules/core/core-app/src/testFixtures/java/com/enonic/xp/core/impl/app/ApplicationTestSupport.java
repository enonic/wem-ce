package com.enonic.xp.core.impl.app;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.app.Applications;
import com.enonic.xp.core.impl.app.resource.ResourceServiceImpl;
import com.enonic.xp.resource.ResourceService;

public abstract class ApplicationTestSupport
{
    protected ResourceService resourceService;

    protected ApplicationService applicationService;

    private Map<ApplicationKey, Application> apps;

    private URL rootTestUrl;

    @BeforeEach
    public final void setup()
        throws Exception
    {
        this.rootTestUrl = Path.of( "src/test/resources" ).toUri().toURL();

        this.apps = new HashMap<>();

        this.applicationService = Mockito.mock( ApplicationService.class );
        Mockito.when( this.applicationService.getInstalledApplication( Mockito.any() ) ).then(
            invocationOnMock -> apps.get( invocationOnMock.getArguments()[0] ) );
        Mockito.when( this.applicationService.getInstalledApplications() ).then( invocationOnMock -> Applications.from( apps.values() ) );
        Mockito.when( this.applicationService.getInstalledApplicationKeys() ).then(
            invocationOnMock -> ApplicationKeys.from( apps.keySet() ) );

        ResourceServiceImpl rService = new ResourceServiceImpl();
        rService.setApplicationService( this.applicationService );
        this.resourceService = rService;

        initialize();
    }

    protected abstract void initialize()
        throws Exception;

    protected final MockApplication addApplication( final String key, final String prefix )
    {
        final MockApplication app = new MockApplication();
        app.setKey( ApplicationKey.from( key ) );
        app.setStarted( true );

        app.setUrlResolver( this.rootTestUrl, prefix );

        this.apps.put( app.getKey(), app );
        return app;
    }
}
