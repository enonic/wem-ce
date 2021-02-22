package com.enonic.xp.impl.task.distributed;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.enonic.xp.core.internal.osgi.OsgiSupportMock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffloadedTaskCallableTest
{
    @Mock(stubOnly = true)
    BundleContext bundleContext;

    @Mock(stubOnly = true)
    ServiceReference<TaskManager> serviceReference;

    @Mock
    TaskManager taskManager;

    @BeforeEach
    void setUp()
        throws Exception
    {
        final Bundle bundle = OsgiSupportMock.mockBundle();
        when( bundle.getBundleContext() ).thenReturn( bundleContext );
        when( bundleContext.getServiceReferences( TaskManager.class, "(local=true)" ) ).thenReturn( List.of( serviceReference ) );
        when( bundleContext.getService( serviceReference ) ).thenReturn( taskManager );

    }

    @AfterEach
    void tearDown()
    {
        OsgiSupportMock.reset();
    }

    @Test
    void call()
    {
        final DescribedTask describedTask = mock( DescribedTask.class );
        new OffloadedTaskCallable( describedTask ).call();
        verify( taskManager ).submitTask( describedTask );
    }
}
