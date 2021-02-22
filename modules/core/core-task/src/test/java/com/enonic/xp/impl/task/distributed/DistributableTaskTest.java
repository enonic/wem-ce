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

import com.enonic.xp.branch.Branch;
import com.enonic.xp.core.internal.osgi.OsgiSupportMock;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.script.NamedTask;
import com.enonic.xp.impl.task.script.NamedTaskFactory;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.auth.AuthenticationInfo;
import com.enonic.xp.task.ProgressReporter;
import com.enonic.xp.task.TaskDescriptor;
import com.enonic.xp.task.TaskId;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributableTaskTest
{
    private static final TaskContext TEST_TASK_CONTEXT =
        new TaskContext( Branch.from( "master" ), RepositoryId.from( "test" ), AuthenticationInfo.unAuthenticated() );

    @Mock(stubOnly = true)
    BundleContext bundleContext;

    @Mock(stubOnly = true)
    ServiceReference<NamedTaskFactory> serviceReference;

    @Mock(stubOnly = true)
    NamedTaskFactory namedTaskFactory;

    Bundle bundle;

    @BeforeEach
    void setUp()
        throws Exception
    {
        bundle = OsgiSupportMock.mockBundle();
        when( bundle.getBundleContext() ).thenReturn( bundleContext );
        when( bundleContext.getServiceReferences( NamedTaskFactory.class, null ) ).thenReturn( List.of( serviceReference ) );
        when( bundleContext.getService( serviceReference ) ).thenReturn( namedTaskFactory );
    }

    @AfterEach
    void tearDown()
    {
        OsgiSupportMock.reset();
    }

    @Test
    void fields()
    {
        final NamedTask namedTask = mock( NamedTask.class );

        final DescriptorKey descriptorKey = DescriptorKey.from( "app:a" );
        final PropertyTree config = new PropertyTree();
        when( namedTaskFactory.create( descriptorKey, config ) ).thenReturn( namedTask );
        when( namedTask.getTaskDescriptor() ).
            thenReturn( TaskDescriptor.create().key( descriptorKey ).description( "task description" ).build() );
        final DistributableTask describedTask = new DistributableTask( descriptorKey, config, TEST_TASK_CONTEXT );
        assertAll( () -> assertEquals( "app:a", describedTask.getName() ), () -> assertNotNull( describedTask.getTaskId() ),
                   () -> assertNotNull( describedTask.getTaskContext() ),
                   () -> assertEquals( "task description", describedTask.getDescription() ),
                   () -> assertEquals( descriptorKey.getApplicationKey(), describedTask.getApplicationKey() ) );
    }

    @Test
    void run()
    {
        final NamedTask namedTask = mock( NamedTask.class );

        final DescriptorKey descriptorKey = DescriptorKey.from( "app:a" );
        final PropertyTree config = new PropertyTree();
        when( namedTaskFactory.create( descriptorKey, config ) ).thenReturn( namedTask );

        final DistributableTask describedTask = new DistributableTask( descriptorKey, config, TEST_TASK_CONTEXT );
        describedTask.run( mock( ProgressReporter.class ) );
        verify( namedTask ).run( any( TaskId.class ), any( ProgressReporter.class ) );
    }
}
