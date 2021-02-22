package com.enonic.xp.core.scheduler;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.scheduler.SchedulerServiceImpl;
import com.enonic.xp.node.NodeAccessException;
import com.enonic.xp.node.NodeAlreadyExistAtPathException;
import com.enonic.xp.node.NodeNotFoundException;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.repo.impl.node.AbstractNodeTest;
import com.enonic.xp.scheduler.CreateScheduledJobParams;
import com.enonic.xp.scheduler.CronCalendar;
import com.enonic.xp.scheduler.ModifyScheduledJobParams;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerConstants;
import com.enonic.xp.scheduler.SchedulerName;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedulerServiceImplTest
    extends AbstractNodeTest
{
    private static final User REPO_TEST_DEFAULT_USER =
        User.create().key( PrincipalKey.ofUser( IdProviderKey.system(), "repo-test-user" ) ).login( "repo-test-user" ).build();

    private static final AuthenticationInfo REPO_TEST_ADMIN_USER_AUTHINFO = AuthenticationInfo.create().
        principals( RoleKeys.AUTHENTICATED ).
        principals( RoleKeys.ADMIN ).
        user( REPO_TEST_DEFAULT_USER ).
        build();

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock
    private IScheduledExecutorService scheduledExecutorService;

    private SchedulerServiceImpl schedulerService;

    private static Context adminContext()
    {
        return ContextBuilder.create().
            branch( "master" ).
            repositoryId( SchedulerConstants.SCHEDULER_REPO_ID ).
            authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
            build();
    }

    @BeforeEach
    protected void setUpNode()
        throws Exception
    {
        super.setUpNode();

        Mockito.when( hazelcastInstance.getScheduledExecutorService( Mockito.anyString() ) ).thenReturn( scheduledExecutorService );

        schedulerService = new SchedulerServiceImpl( indexService, repositoryService, nodeService, hazelcastInstance );

        adminContext().runWith( () -> schedulerService.initialize() );
    }

    @Test
    void create()
    {
        final SchedulerName name = SchedulerName.from( "test" );
        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();
        final PropertyTree payload = new PropertyTree();
        payload.addString( "string", "value" );
        final PrincipalKey author = PrincipalKey.from( "user:system:author" );
        final PrincipalKey user = PrincipalKey.from( "user:system:user" );

        final CreateScheduledJobParams params = CreateScheduledJobParams.create().
            name( name ).
            descriptor( descriptor ).
            calendar( calendar ).
            payload( payload ).
            description( "description" ).
            enabled( true ).
            user( user ).
            author( author ).
            build();

        final ScheduledJob scheduledJob = adminContext().callWith( () -> schedulerService.create( params ) );

        assertEquals( name, scheduledJob.getName() );
        assertEquals( descriptor, scheduledJob.getDescriptor() );
        assertEquals( calendar.getStringValue(), scheduledJob.getCalendar().getStringValue() );
        assertEquals( calendar.getTimeZone(), ( (CronCalendar) scheduledJob.getCalendar() ).getTimeZone() );
        assertEquals( payload, scheduledJob.getPayload() );
        assertEquals( "description", scheduledJob.getDescription() );
        assertEquals( user, scheduledJob.getUser() );
        assertEquals( author, scheduledJob.getAuthor() );
        assertTrue( scheduledJob.isEnabled() );
    }

    @Test
    void createWithoutAccess()
    {
        final SchedulerName name = SchedulerName.from( "test" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();

        final CreateScheduledJobParams params = CreateScheduledJobParams.create().
            name( name ).
            calendar( calendar ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" ) ).
            payload( new PropertyTree() ).
            build();

        assertThrows( NodeAccessException.class, () -> schedulerService.create( params ) );
    }

    @Test
    void createExisted()
    {
        final SchedulerName name = SchedulerName.from( "test" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();

        final CreateScheduledJobParams params = CreateScheduledJobParams.create().
            name( name ).
            calendar( calendar ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" ) ).
            payload( new PropertyTree() ).
            build();

        adminContext().runWith( () -> schedulerService.create( params ) );

        assertThrows( NodeAlreadyExistAtPathException.class, () -> adminContext().runWith( () -> schedulerService.create( params ) ) );
    }

    @Test
    void modifyNotCreated()
    {
        assertThrows( NodeNotFoundException.class,
                      () -> adminContext().runWith( () -> schedulerService.modify( ModifyScheduledJobParams.create().
                          name( SchedulerName.from( "nonExist" ) ).
                          editor( edit -> edit.enabled = false ).
                          build() ) ) );

    }

    @Test
    void modifyWithoutAccess()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            enabled( true ).
            build() ) );

        adminContext().callWith( () -> schedulerService.modify( ModifyScheduledJobParams.create().
            name( name ).
            editor( edit -> edit.enabled = false ).
            build() ) );
    }

    @Test
    void modifyWithDispose()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            enabled( true ).
            build() ) );

        final IScheduledFuture<?> future = mockFuture( name );

        adminContext().callWith( () -> schedulerService.modify( ModifyScheduledJobParams.create().
            name( name ).
            editor( edit -> edit.enabled = false ).
            build() ) );

        Mockito.verify( future, Mockito.times( 1 ) ).dispose();
    }

    @Test
    void modify()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            build() ) );

        final ScheduledJob modifiedJob = adminContext().callWith( () -> schedulerService.modify( ModifyScheduledJobParams.create().
            name( name ).
            editor( edit -> {
                edit.enabled = true;
                edit.description = "new description";
                edit.descriptor = DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task2" );
                edit.payload.addString( "string", "value" );
                edit.author = PrincipalKey.from( "user:provider:author" );
                edit.user = PrincipalKey.from( "user:provider:user" );
                edit.calendar = CronCalendar.create().
                    value( "* * * * 5" ).
                    timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                    build();
            } ).
            build() ) );

        assertEquals( name, modifiedJob.getName() );
        assertEquals( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task2" ), modifiedJob.getDescriptor() );
        assertEquals( "new description", modifiedJob.getDescription() );
        assertEquals( "* * * * 5", modifiedJob.getCalendar().getStringValue() );
        assertEquals( "GMT+05:30", ( (CronCalendar) modifiedJob.getCalendar() ).getTimeZone().getID() );
        assertEquals( "value", modifiedJob.getPayload().getString( "string" ) );
        assertEquals( PrincipalKey.from( "user:provider:author" ), modifiedJob.getAuthor() );
        assertEquals( PrincipalKey.from( "user:provider:user" ), modifiedJob.getUser() );
    }

    @Test
    void deleteNotCreated()
    {
        final SchedulerName name = SchedulerName.from( "test" );
        final boolean deleted = schedulerService.delete( name );

        assertFalse( deleted );
    }

    @Test
    void deleteWithoutAccess()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            build() ) );

        final boolean deleted = schedulerService.delete( name );
        assertFalse( deleted );
    }

    @Test
    void deleteWithDispose()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            build() ) );

        final IScheduledFuture<?> future = mockFuture( name );

        adminContext().callWith( () -> schedulerService.delete( name ) );

        Mockito.verify( future, Mockito.times( 1 ) ).dispose();
    }

    @Test
    void delete()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            build() ) );

        final boolean deleted = adminContext().callWith( () -> schedulerService.delete( name ) );

        assertTrue( deleted );
    }

    @Test
    void getNotCreated()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        assertNull( schedulerService.get( name ) );
    }

    @Test
    void getWithoutAccess()
    {
        final SchedulerName name = SchedulerName.from( "test" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( DescriptorKey.from( ApplicationKey.from( "com.enonic.app.test" ), "task1" ) ).
            calendar( CronCalendar.create().
                value( "* * * * *" ).
                timeZone( TimeZone.getTimeZone( "GMT+5:30" ) ).
                build() ).
            payload( new PropertyTree() ).
            build() ) );

        assertNull( schedulerService.get( name ) );
    }

    @Test
    void get()
    {
        final SchedulerName name = SchedulerName.from( "test" );
        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();
        final PropertyTree payload = new PropertyTree();
        payload.addString( "string", "value" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( descriptor ).
            calendar( calendar ).
            payload( payload ).
            description( "description" ).
            build() ) );

        final ScheduledJob scheduledJob = adminContext().callWith( () -> schedulerService.get( name ) );

        assertEquals( name, scheduledJob.getName() );
        assertEquals( descriptor, scheduledJob.getDescriptor() );
        assertEquals( calendar.getStringValue(), scheduledJob.getCalendar().getStringValue() );
        assertEquals( calendar.getTimeZone(), ( (CronCalendar) scheduledJob.getCalendar() ).getTimeZone() );
        assertEquals( payload, scheduledJob.getPayload() );
        assertEquals( "description", scheduledJob.getDescription() );
    }

    @Test
    void listWithoutAccess()
    {
        assertEquals( 0, schedulerService.list().size() );

        final SchedulerName name = SchedulerName.from( "test" );
        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();
        final PropertyTree payload = new PropertyTree();
        payload.addString( "string", "value" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( name ).
            descriptor( descriptor ).
            calendar( calendar ).
            payload( payload ).
            description( "description" ).
            build() ) );

        assertEquals( 0, schedulerService.list().size() );
    }

    @Test
    void list()
    {
        assertEquals( 0, adminContext().callWith( () -> schedulerService.list() ).size() );

        final DescriptorKey descriptor = DescriptorKey.from( ApplicationKey.from( "com.enonic.app.features" ), "landing" );
        final CronCalendar calendar = CronCalendar.create().
            value( "* * * * *" ).
            timeZone( TimeZone.getDefault() ).
            build();
        final PropertyTree payload = new PropertyTree();
        payload.addString( "string", "value" );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( SchedulerName.from( "test1" ) ).
            descriptor( descriptor ).
            calendar( calendar ).
            payload( payload ).
            description( "description" ).
            build() ) );

        assertEquals( 1, adminContext().callWith( () -> schedulerService.list() ).size() );

        adminContext().callWith( () -> schedulerService.create( CreateScheduledJobParams.create().
            name( SchedulerName.from( "test2" ) ).
            descriptor( descriptor ).
            calendar( calendar ).
            payload( payload ).
            description( "description" ).
            build() ) );

        assertEquals( 2, adminContext().callWith( () -> schedulerService.list() ).size() );
    }

    private IScheduledFuture<?> mockFuture( final SchedulerName name )
    {
        final ScheduledTaskHandler handler = Mockito.mock( ScheduledTaskHandler.class );
        final IScheduledFuture<?> future = Mockito.mock( IScheduledFuture.class );

        Mockito.when( future.getHandler() ).thenReturn( handler );
        Mockito.when( handler.getTaskName() ).thenReturn( name.getValue() );

        final Map futures = Map.of( Mockito.mock( Member.class ), List.of( future ) );
        Mockito.when( scheduledExecutorService.getAllScheduledFutures() ).
            thenReturn( futures );

        return future;
    }
}
