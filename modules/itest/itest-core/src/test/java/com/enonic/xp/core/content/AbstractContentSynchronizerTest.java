package com.enonic.xp.core.content;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;

import com.enonic.xp.audit.AuditLogService;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentInheritType;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.CreateContentParams;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.core.impl.content.ContentAuditLogExecutorImpl;
import com.enonic.xp.core.impl.content.ContentAuditLogSupportImpl;
import com.enonic.xp.core.impl.content.ContentConfig;
import com.enonic.xp.core.impl.content.ContentServiceImpl;
import com.enonic.xp.core.impl.media.MediaInfoServiceImpl;
import com.enonic.xp.core.impl.project.ProjectPermissionsContextManagerImpl;
import com.enonic.xp.core.impl.project.ProjectServiceImpl;
import com.enonic.xp.core.impl.schema.content.ContentTypeServiceImpl;
import com.enonic.xp.core.impl.security.SecurityServiceImpl;
import com.enonic.xp.core.impl.site.SiteServiceImpl;
import com.enonic.xp.core.internal.concurrent.RecurringJob;
import com.enonic.xp.core.internal.osgi.OsgiSupportMock;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.extractor.BinaryExtractor;
import com.enonic.xp.extractor.ExtractedData;
import com.enonic.xp.form.Form;
import com.enonic.xp.impl.task.LocalTaskManagerImpl;
import com.enonic.xp.impl.task.TaskManagerCleanupScheduler;
import com.enonic.xp.impl.task.TaskServiceImpl;
import com.enonic.xp.impl.task.script.NamedTaskFactory;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.CreateProjectParams;
import com.enonic.xp.project.Project;
import com.enonic.xp.project.ProjectName;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.repo.impl.node.AbstractNodeTest;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.schema.mixin.MixinService;
import com.enonic.xp.schema.xdata.XDataService;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.SystemConstants;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public abstract class AbstractContentSynchronizerTest
    extends AbstractNodeTest
{
    protected static final User REPO_TEST_DEFAULT_USER =
        User.create().key( PrincipalKey.ofUser( IdProviderKey.system(), "repo-test-user" ) ).login( "repo-test-user" ).build();

    protected static final AuthenticationInfo REPO_TEST_ADMIN_USER_AUTHINFO = AuthenticationInfo.create().
        principals( RoleKeys.AUTHENTICATED ).
        principals( RoleKeys.ADMIN ).
        user( REPO_TEST_DEFAULT_USER ).
        build();

    protected ProjectServiceImpl projectService;

    protected ContentServiceImpl contentService;

    protected MediaInfoServiceImpl mediaInfoService;

    protected PageDescriptorService pageDescriptorService;

    protected PartDescriptorService partDescriptorService;

    protected LayoutDescriptorService layoutDescriptorService;

    protected ContentTypeServiceImpl contentTypeService;

    protected TaskServiceImpl taskService;

    protected Context sourceContext;

    protected Context targetContext;

    protected Project sourceProject;

    protected Project targetProject;

    protected static Context adminContext()
    {
        return ContextBuilder.create().
            branch( "master" ).
            repositoryId( SystemConstants.SYSTEM_REPO_ID ).
            authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
            build();
    }

    protected void setUpNode()
        throws Exception
    {
        super.setUpNode();

        setUpProjectService();
        setUpContentService();
        setupTaskService();
    }

    private void setUpProjectService()
    {
        adminContext().runWith( () -> {
            SecurityServiceImpl securityService = new SecurityServiceImpl( this.nodeService, indexService );

            securityService.initialize();

            projectService = new ProjectServiceImpl( repositoryService, indexService, nodeService, securityService,
                                                     new ProjectPermissionsContextManagerImpl(), eventPublisher );

            sourceProject = projectService.create( CreateProjectParams.create().
                name( ProjectName.from( "source_project" ) ).
                displayName( "Source Project" ).
                build() );

            targetProject = projectService.create( CreateProjectParams.create().
                name( ProjectName.from( "target_project" ) ).
                displayName( "Target Project" ).
                parent( sourceProject.getName() ).
                build() );

            this.targetContext = ContextBuilder.from( ContextAccessor.current() ).
                repositoryId( targetProject.getName().getRepoId() ).
                branch( ContentConstants.BRANCH_DRAFT ).
                authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
                build();

            this.sourceContext = ContextBuilder.from( ContextAccessor.current() ).
                repositoryId( sourceProject.getName().getRepoId() ).
                branch( ContentConstants.BRANCH_DRAFT ).
                authInfo( REPO_TEST_ADMIN_USER_AUTHINFO ).
                build();
        } );
    }

    private void setUpContentService()
    {
        final Map<String, List<String>> metadata = new HashMap<>();
        metadata.put( HttpHeaders.CONTENT_TYPE, List.of( "image/jpg" ) );

        final ExtractedData extractedData = ExtractedData.create().
            metadata( metadata ).
            build();

        final BinaryExtractor extractor = Mockito.mock( BinaryExtractor.class );
        Mockito.when( extractor.extract( Mockito.isA( ByteSource.class ) ) ).
            thenReturn( extractedData );

        mediaInfoService = new MediaInfoServiceImpl();
        mediaInfoService.setBinaryExtractor( extractor );

        XDataService xDataService = Mockito.mock( XDataService.class );

        MixinService mixinService = Mockito.mock( MixinService.class );
        Mockito.when( mixinService.inlineFormItems( Mockito.isA( Form.class ) ) ).then( AdditionalAnswers.returnsFirstArg() );

        pageDescriptorService = Mockito.mock( PageDescriptorService.class );
        partDescriptorService = Mockito.mock( PartDescriptorService.class );
        layoutDescriptorService = Mockito.mock( LayoutDescriptorService.class );

        contentTypeService = new ContentTypeServiceImpl();
        contentTypeService.setMixinService( mixinService );

        final ResourceService resourceService = Mockito.mock( ResourceService.class );
        final SiteServiceImpl siteService = new SiteServiceImpl();
        siteService.setResourceService( resourceService );
        siteService.setMixinService( mixinService );

        AuditLogService auditLogService = Mockito.mock( AuditLogService.class );
        final ContentConfig contentConfig = Mockito.mock( ContentConfig.class );

        final ContentAuditLogSupportImpl contentAuditLogSupport =
            new ContentAuditLogSupportImpl( contentConfig, new ContentAuditLogExecutorImpl(), auditLogService );

        contentService = new ContentServiceImpl();
        contentService.setNodeService( nodeService );
        contentService.setEventPublisher( eventPublisher );
        contentService.setMediaInfoService( mediaInfoService );
        contentService.setSiteService( siteService );
        contentService.setContentTypeService( contentTypeService );
        contentService.setxDataService( xDataService );
        contentService.setPageDescriptorService( pageDescriptorService );
        contentService.setPartDescriptorService( partDescriptorService );
        contentService.setLayoutDescriptorService( layoutDescriptorService );
        contentService.setFormDefaultValuesProcessor( ( form, data ) -> {
        } );
        contentService.setContentAuditLogSupport( contentAuditLogSupport );
        contentService.initialize();
    }

    private void setupTaskService()
    {
        final Bundle bundle = OsgiSupportMock.mockBundle();
        Mockito.when( bundle.getSymbolicName() ).thenReturn( "testBundle" );

        final LocalTaskManagerImpl taskMan = new LocalTaskManagerImpl( Runnable::run, new TaskManagerCleanupSchedulerMock(), event -> {
        } );
        taskMan.activate();

        NamedTaskFactory namedTaskFactory = mock( NamedTaskFactory.class );

        this.taskService = new TaskServiceImpl( taskMan, namedTaskFactory );
    }

    private static class TaskManagerCleanupSchedulerMock
        implements TaskManagerCleanupScheduler
    {

        @Override
        public RecurringJob scheduleWithFixedDelay( final Runnable command )
        {
            command.run();
            return mock( RecurringJob.class );
        }
    }

    protected Content createContent( final ContentPath parent )
    {
        return createContent( parent, "name" );
    }

    protected Content createContent( final ContentPath parent, final String name )
    {
        final PropertyTree data = new PropertyTree();
        data.addStrings( "stringField", "stringValue" );

        final CreateContentParams createParent = CreateContentParams.create().
            contentData( data ).
            name( name ).
            displayName( name ).
            parent( parent ).
            type( ContentTypeName.folder() ).
            build();

        return this.contentService.create( createParent );
    }

    protected void compareSynched( final Content sourceContent, final Content targetContent )
    {
        assertEquals( sourceContent.getId(), targetContent.getId() );
        assertEquals( sourceContent.getName(), targetContent.getName() );
        assertEquals( sourceContent.getDisplayName(), targetContent.getDisplayName() );
        assertEquals( sourceContent.getData(), targetContent.getData() );
        assertEquals( sourceContent.getPath(), targetContent.getPath() );
        assertEquals( sourceContent.getAllExtraData(), targetContent.getAllExtraData() );
        assertEquals( sourceContent.getAttachments(), targetContent.getAttachments() );
        assertEquals( sourceContent.getOwner(), targetContent.getOwner() );
        assertEquals( sourceContent.getLanguage(), targetContent.getLanguage() );
        assertEquals( sourceContent.getWorkflowInfo(), targetContent.getWorkflowInfo() );
        assertEquals( sourceContent.getPage(), targetContent.getPage() );
        assertEquals( sourceContent.isValid(), targetContent.isValid() );
        assertEquals( sourceContent.inheritsPermissions(), targetContent.inheritsPermissions() );
        assertEquals( sourceContent.getCreatedTime(), targetContent.getCreatedTime() );

        assertNotEquals( sourceContent.getPermissions(), targetContent.getPermissions() );

        assertTrue( targetContent.getInherit().containsAll( EnumSet.allOf( ContentInheritType.class ) ) );
    }
}
