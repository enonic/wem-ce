package com.enonic.xp.impl.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;

import com.enonic.xp.index.IndexService;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.scheduler.SchedulerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceActivatorTest
{
    @Mock
    private BundleContext bundleContext;

    @Mock
    private ServiceRegistration<SchedulerService> service;

    @Mock(stubOnly = true)
    private NodeService nodeService;

    @Mock(stubOnly = true)
    private IndexService indexService;

    @Mock(stubOnly = true)
    private RepositoryService repositoryService;

    @Mock(stubOnly = true)
    private HazelcastInstance hazelcastInstance;

    @Mock(stubOnly = true)
    private IScheduledExecutorService ScheduledExecutorService;

    @BeforeEach
    void setUp()
    {
        when( indexService.isMaster() ).thenReturn( true );
        when( indexService.waitForYellowStatus() ).thenReturn( true );
    }

    @Test
    void lifecycle()
    {
        final SchedulerServiceActivator activator =
            new SchedulerServiceActivator( repositoryService, indexService, nodeService, hazelcastInstance );

        when( hazelcastInstance.getScheduledExecutorService( isA( String.class ) ) ).thenReturn( ScheduledExecutorService );

        when( bundleContext.registerService( same( SchedulerService.class ), any( SchedulerService.class ), isNull() ) ).
            thenReturn( service );

        activator.activate( bundleContext );

        activator.deactivate();
        verify( service ).unregister();
    }
}
