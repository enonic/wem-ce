package com.enonic.xp.impl.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;

import com.enonic.xp.core.internal.osgi.OsgiSupportMock;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.distributed.DescribedTask;
import com.enonic.xp.impl.task.distributed.TaskManager;
import com.enonic.xp.impl.task.script.NamedTask;
import com.enonic.xp.impl.task.script.NamedTaskFactory;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.RunnableTask;
import com.enonic.xp.task.SubmitTaskParams;
import com.enonic.xp.task.TaskDescriptor;
import com.enonic.xp.task.TaskId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest
{
    @Mock
    TaskManager taskManager;

    @Mock
    NamedTaskFactory namedTaskFactory;

    @Captor
    ArgumentCaptor<DescribedTask> describedTaskCaptor;

    TaskConfig taskConfig;

    TaskServiceImpl taskService;

    Bundle bundle;

    @BeforeEach
    void setUp()
    {
        bundle = OsgiSupportMock.mockBundle();

        taskConfig = mock( TaskConfig.class, invocation -> invocation.getMethod().getDefaultValue() );

        taskService = new TaskServiceImpl( taskManager, namedTaskFactory );
        taskService.activate( taskConfig );
    }

    @AfterEach
    void tearDown()
    {
        OsgiSupportMock.reset();
    }

    @Test
    void submitTask_runnableTask()
    {
        when( bundle.getSymbolicName() ).thenReturn( "some.app" );

        final TaskId taskId = taskService.submitTask( mock( RunnableTask.class ), "someDescription" );
        verify( taskManager ).submitTask( describedTaskCaptor.capture() );
        final DescribedTask argument = describedTaskCaptor.getValue();
        assertEquals( "someDescription", argument.getDescription() );
        assertEquals( taskId, argument.getTaskId() );
    }

    @Test
    void submitTask_DescriptorKey()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "module:my-admin-tool" );
        final PropertyTree config = new PropertyTree();

        final NamedTask namedTask = mock( NamedTask.class );
        when( namedTaskFactory.createLegacy( descriptorKey, config ) ).thenReturn( namedTask );
        when( namedTask.getTaskDescriptor() ).
            thenReturn( TaskDescriptor.create().key( descriptorKey ).description( "task description" ).build() );

        final TaskId taskId = taskService.submitTask( descriptorKey, config );
        verify( taskManager ).submitTask( describedTaskCaptor.capture() );
        describedTaskCaptor.getValue();
        final DescribedTask argument = describedTaskCaptor.getValue();
        assertEquals( taskId, argument.getTaskId() );
    }

    @Test
    void submitTask_DescriptorKey_offload_to_local()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "module:my-admin-tool" );

        final TaskId taskId = taskService.submitTask( SubmitTaskParams.create().descriptorKey( descriptorKey ).build() );
        verify( taskManager ).submitTask( describedTaskCaptor.capture() );
        describedTaskCaptor.getValue();
        final DescribedTask argument = describedTaskCaptor.getValue();
        assertEquals( taskId, argument.getTaskId() );
    }

    @Test
    void submitTask_DescriptorKey_offload_to_clustered()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "module:my-admin-tool" );

        final TaskManager clusteredTaskManager = mock( TaskManager.class );
        taskService.setClusteredTaskManager( clusteredTaskManager );

        final TaskId taskId = taskService.submitTask( SubmitTaskParams.create().descriptorKey( descriptorKey ).build() );

        verify( clusteredTaskManager ).submitTask( describedTaskCaptor.capture() );
        describedTaskCaptor.getValue();
        final DescribedTask argument = describedTaskCaptor.getValue();
        assertEquals( taskId, argument.getTaskId() );
    }

    @Test
    void submitTask_DescriptorKey_offload_to_clustered_wait_success()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "module:my-admin-tool" );

        final TaskManager clusteredTaskManager = mock( TaskManager.class );

        when( taskConfig.distributable_acceptInbound() ).thenReturn( false );
        taskService.activate( taskConfig );

        CompletableFuture.runAsync( () -> taskService.setClusteredTaskManager( clusteredTaskManager ),
                                    CompletableFuture.delayedExecutor( 1, TimeUnit.SECONDS ) );

        final TaskId taskId = taskService.submitTask( SubmitTaskParams.create().descriptorKey( descriptorKey ).build() );

        verify( clusteredTaskManager ).submitTask( describedTaskCaptor.capture() );
        describedTaskCaptor.getValue();
        final DescribedTask argument = describedTaskCaptor.getValue();
        assertEquals( taskId, argument.getTaskId() );
    }

    @Test
    void submitTask_DescriptorKey_offload_to_clustered_wait_fail()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "module:my-admin-tool" );

        when( taskConfig.distributable_acceptInbound() ).thenReturn( false );
        taskService.activate( taskConfig );

        assertThrows( RuntimeException.class, () -> {
            taskService.submitTask( SubmitTaskParams.create().descriptorKey( descriptorKey ).build() );
        } );
    }

    @Test
    void getTasks()
    {
        final TaskId someTaskId1 = TaskId.from( "someTask1" );

        taskService.getTaskInfo( someTaskId1 );

        verify( taskManager ).getTaskInfo( eq( someTaskId1 ) );

        final TaskManager taskManager = mock( TaskManager.class );

        taskService.setClusteredTaskManager( taskManager );

        taskService.getAllTasks();

        verify( taskManager ).getAllTasks();

        taskService.unsetClusteredTaskManager( taskManager );

        taskService.getRunningTasks();

        verifyNoMoreInteractions( taskManager );

        verify( this.taskManager ).getRunningTasks();
    }
}
