package com.enonic.xp.repo.impl.upgrade;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;

import com.enonic.xp.blob.BlobKey;
import com.enonic.xp.blob.BlobStore;
import com.enonic.xp.blob.NodeVersionKey;
import com.enonic.xp.blob.Segment;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.content.FindContentByParentParams;
import com.enonic.xp.content.FindContentByParentResult;
import com.enonic.xp.content.FindContentVersionsParams;
import com.enonic.xp.content.FindContentVersionsResult;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.index.IndexService;
import com.enonic.xp.index.ReindexParams;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodeVersion;
import com.enonic.xp.node.NodeVersionId;
import com.enonic.xp.node.NodeVersionMetadata;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.Project;
import com.enonic.xp.project.ProjectService;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.branch.BranchService;
import com.enonic.xp.repo.impl.node.NodeConstants;
import com.enonic.xp.repo.impl.node.dao.NodeVersionService;
import com.enonic.xp.repo.impl.node.json.NodeVersionJsonSerializer;
import com.enonic.xp.repo.impl.version.VersionService;
import com.enonic.xp.repository.RepositorySegmentUtils;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

final class SyncTask
    implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SyncTask.class );

    private final ProjectService projectService;

    private final ContentService contentService;

    private final BlobStore blobStore;

    private final VersionService versionService;

    private final BranchService branchService;

    private final NodeVersionService nodeVersionService;

    private final IndexService indexService;


    public SyncTask( final Builder builder )
    {
        this.projectService = builder.projectService;
        this.contentService = builder.contentService;
        this.indexService = builder.indexService;

        this.blobStore = builder.blobStore;
        this.nodeVersionService = builder.nodeVersionService;
        this.versionService = builder.versionService;
        this.branchService = builder.branchService;
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public void run()
    {
        createAdminContext().runWith( () -> this.projectService.list().
            stream().
            filter( project -> project.getParent() != null ).
            sorted( ( o1, o2 ) -> {

                if ( o2.getName().equals( o1.getParent() ) )
                {
                    return 1;
                }

                if ( o1.getName().equals( o2.getParent() ) )
                {
                    return -1;
                }

                return 0;
            } ).
            forEach( project -> {
                Project parentProject = this.projectService.get( project.getParent() );
                doSync( parentProject, project );
            } ) );

    }

    private void doSync( final Project sourceProject, final Project targetProject )
    {
        LOGGER.info( "-- [{}] -> [{}] origin project sync started.", sourceProject.getName().toString(),
                     targetProject.getName().toString() );

        Context sourceContext = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( sourceProject.getName().getRepoId() ).
            branch( ContentConstants.BRANCH_DRAFT ).
            authInfo( createAdminAuthInfo() ).
            build();

        Context targetContext = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( targetProject.getName().getRepoId() ).
            branch( ContentConstants.BRANCH_DRAFT ).
            authInfo( createAdminAuthInfo() ).
            build();

        final Queue<Content> queue = new ArrayDeque<>();

        targetContext.runWith( () -> {

            final InternalContext draftInternalContext = InternalContext.
                create( ContextAccessor.current() ).
                branch( ContentConstants.BRANCH_DRAFT ).
                build();

            final InternalContext masterInternalContext = InternalContext.
                create( ContextAccessor.current() ).
                branch( ContentConstants.BRANCH_MASTER ).
                build();

            queue.add( contentService.getByPath( ContentPath.ROOT ) );

            while ( queue.size() > 0 )
            {
                final Content currentContent = queue.poll();

                final FindContentByParentResult result = contentService.findByParent( FindContentByParentParams.create().
                    parentId( currentContent.getId() ).
                    recursive( false ).
                    childOrder( currentContent.getChildOrder() ).
                    size( -1 ).
                    build() );

                for ( final Content content : result.getContents() )
                {

                    final FindContentVersionsResult versions = contentService.getVersions( FindContentVersionsParams.create().
                        contentId( content.getId() ).
                        from( 0 ).
                        size( -1 ).
                        build() );

                    final NodeBranchEntry draftBranchEntry = this.branchService.get( NodeId.from( content.getId() ), draftInternalContext );
                    final NodeBranchEntry masterBranchEntry =
                        this.branchService.get( NodeId.from( content.getId() ), masterInternalContext );

                    versions.getContentVersions().forEach( contentVersion -> {

                        final NodeVersionMetadata metadata =
                            this.versionService.getVersion( NodeId.from( content.getId() ), NodeVersionId.from( contentVersion.getId() ),
                                                            draftInternalContext );

                        final NodeVersion nodeVersion = this.nodeVersionService.get( metadata.getNodeVersionKey(), draftInternalContext );
                        final PropertyTree versionData = nodeVersion.getData();

                        final List<String> inherit = (List<String>) versionData.getStrings( ContentPropertyNames.INHERIT );

                        //remove originProject for duplicates
                        if ( !sourceContext.callWith( () -> contentService.contentExists( content.getId() ) ) && inherit.isEmpty() )
                        {
                            if ( versionData.getProperty( ContentPropertyNames.ORIGIN_PROJECT ) != null )
                            {
                                versionData.removeProperty( ContentPropertyNames.ORIGIN_PROJECT );

                                writeChanges( nodeVersion, metadata, draftInternalContext, masterInternalContext, draftBranchEntry,
                                              masterBranchEntry );
                            }
                        }
                        else //add originProject to version
                        {
                            if ( versionData.getProperty( ContentPropertyNames.ORIGIN_PROJECT ) == null ||
                                !sourceProject.getName().toString().equals( versionData.getString( ContentPropertyNames.ORIGIN_PROJECT ) ) )
                            {
                                versionData.setString( ContentPropertyNames.ORIGIN_PROJECT, sourceProject.getName().toString() );

                                writeChanges( nodeVersion, metadata, draftInternalContext, masterInternalContext, draftBranchEntry,
                                              masterBranchEntry );
                            }
                        }

                    } );

                    if ( content.hasChildren() )
                    {
                        queue.offer( content );
                    }
                }
            }
        } );

        this.indexService.reindex( ReindexParams.create().
            setBranches( Branches.from( ContentConstants.BRANCH_MASTER, ContentConstants.BRANCH_DRAFT ) ).
            initialize( false ).
            repositoryId( targetProject.getName().getRepoId() ).
            build() );

        LOGGER.info( "-- [{}] -> [{}] origin project sync finished.", sourceProject.getName().toString(),
                     targetProject.getName().toString() );

    }

    private void writeChanges( final NodeVersion nodeVersion, NodeVersionMetadata metadata, InternalContext draftInternalContext,
                               InternalContext masterInternalContext, NodeBranchEntry draftBranchEntry, NodeBranchEntry masterBranchEntry )
    {
        final byte[] nodeJsonString = new NodeVersionJsonSerializer().
            toNodeString( nodeVersion );

        final Segment nodeSegment =
            RepositorySegmentUtils.toSegment( draftInternalContext.getRepositoryId(), NodeConstants.NODE_SEGMENT_LEVEL );
        try
        {

            this.versionService.delete( Set.of( metadata.getNodeVersionId() ), draftInternalContext );

            final BlobKey newNodeKey = blobStore.addRecord( nodeSegment, ByteSource.wrap( nodeJsonString ) ).getKey();

            final NodeVersionKey newNodeVersionKey = NodeVersionKey.from( newNodeKey, metadata.getNodeVersionKey().getIndexConfigBlobKey(),
                                                                          metadata.getNodeVersionKey().getAccessControlBlobKey() );

            this.versionService.store( NodeVersionMetadata.create().
                nodeVersionId( metadata.getNodeVersionId() ).
                nodeVersionKey( newNodeVersionKey ).
                nodeCommitId( metadata.getNodeCommitId() ).
                nodeId( metadata.getNodeId() ).
                nodePath( metadata.getNodePath() ).
                binaryBlobKeys( metadata.getBinaryBlobKeys() ).
                timestamp( metadata.getTimestamp() ).
                build(), draftInternalContext );

            if ( draftBranchEntry != null && draftBranchEntry.getVersionId().equals( metadata.getNodeVersionId() ) )
            {
                updateBranchEntry( draftBranchEntry, newNodeVersionKey, draftInternalContext );
            }

            if ( masterBranchEntry != null && masterBranchEntry.getVersionId().equals( metadata.getNodeVersionId() ) )
            {
                updateBranchEntry( masterBranchEntry, newNodeVersionKey, masterInternalContext );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "originProject value sync failed for [{}] content in [{}] repo", metadata.getNodeId(),
                          ContextAccessor.current().getRepositoryId() );
        }
    }

    private void updateBranchEntry( final NodeBranchEntry branchEntry, final NodeVersionKey newNodeVersionKey,
                                    final InternalContext internalContext )
    {
        this.branchService.delete( NodeIds.from( branchEntry.getNodeId() ), internalContext );

        this.branchService.store( NodeBranchEntry.create().
            nodeId( branchEntry.getNodeId() ).
            nodePath( branchEntry.getNodePath() ).
            nodeState( branchEntry.getNodeState() ).
            nodeVersionId( branchEntry.getVersionId() ).
            nodeVersionKey( newNodeVersionKey ).
            timestamp( branchEntry.getTimestamp() ).
            build(), internalContext );
    }

    private Context createAdminContext()
    {
        final AuthenticationInfo authInfo = createAdminAuthInfo();
        return ContextBuilder.from( ContentConstants.CONTEXT_MASTER ).
            authInfo( authInfo ).
            build();
    }

    private AuthenticationInfo createAdminAuthInfo()
    {
        return AuthenticationInfo.create().
            principals( RoleKeys.ADMIN ).
            user( User.create().
                key( PrincipalKey.ofSuperUser() ).
                login( PrincipalKey.ofSuperUser().getId() ).
                build() ).
            build();
    }

    public static class Builder
    {
        private ProjectService projectService;

        private ContentService contentService;

        private IndexService indexService;

        private PageDescriptorService pageDescriptorService;

        private PartDescriptorService partDescriptorService;

        private LayoutDescriptorService layoutDescriptorService;

        private BlobStore blobStore;

        private VersionService versionService;

        private BranchService branchService;

        private NodeVersionService nodeVersionService;


        private Builder()
        {
        }

        public Builder projectService( final ProjectService projectService )
        {
            this.projectService = projectService;
            return this;
        }

        public Builder contentService( final ContentService contentService )
        {
            this.contentService = contentService;
            return this;
        }

        public Builder indexService( final IndexService indexService )
        {
            this.indexService = indexService;
            return this;
        }

        public Builder pageDescriptorService( final PageDescriptorService value )
        {
            this.pageDescriptorService = value;
            return this;
        }

        public Builder partDescriptorService( final PartDescriptorService value )
        {
            this.partDescriptorService = value;
            return this;
        }

        public Builder layoutDescriptorService( final LayoutDescriptorService value )
        {
            this.layoutDescriptorService = value;
            return this;
        }

        public Builder blobStore( final BlobStore value )
        {
            this.blobStore = value;
            return this;
        }

        public Builder versionService( final VersionService value )
        {
            this.versionService = value;
            return this;
        }

        public Builder branchService( final BranchService value )
        {
            this.branchService = value;
            return this;
        }

        public Builder nodeVersionService( final NodeVersionService value )
        {
            this.nodeVersionService = value;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( this.projectService, "projectService must be set." );
            Preconditions.checkNotNull( this.contentService, "contentService must be set." );
            Preconditions.checkNotNull( this.indexService, "nodeService must be set." );
            Preconditions.checkNotNull( this.partDescriptorService, "partDescriptorService must be set." );
            Preconditions.checkNotNull( this.pageDescriptorService, "pageDescriptorService must be set." );
            Preconditions.checkNotNull( this.layoutDescriptorService, "layoutDescriptorService must be set." );
            Preconditions.checkNotNull( this.blobStore, "blobStore must be set." );
            Preconditions.checkNotNull( this.versionService, "versionService must be set." );
            Preconditions.checkNotNull( this.branchService, "branchService must be set." );
            Preconditions.checkNotNull( this.nodeVersionService, "nodeVersionService must be set." );
        }

        public SyncTask build()
        {
            validate();
            return new SyncTask( this );
        }
    }
}
