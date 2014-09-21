package com.enonic.wem.core.entity;

import java.time.Instant;

import com.enonic.wem.api.account.UserKey;
import com.enonic.wem.api.context.Context;
import com.enonic.wem.api.entity.EntityId;
import com.enonic.wem.api.entity.Node;
import com.enonic.wem.api.entity.NodeAlreadyExistException;
import com.enonic.wem.api.entity.NodeName;
import com.enonic.wem.api.entity.NodePath;
import com.enonic.wem.api.entity.NodeVersionId;
import com.enonic.wem.api.entity.NodeVersionIds;
import com.enonic.wem.api.entity.Nodes;
import com.enonic.wem.api.entity.RenameNodeParams;
import com.enonic.wem.core.entity.dao.NodeDao;
import com.enonic.wem.core.index.IndexContext;
import com.enonic.wem.core.version.EntityVersionDocument;
import com.enonic.wem.core.workspace.StoreWorkspaceDocument;
import com.enonic.wem.core.workspace.WorkspaceContext;

final class RenameNodeCommand
    extends AbstractNodeCommand
{
    private final RenameNodeParams params;

    private final NodeDao nodeDao;

    private RenameNodeCommand( Builder builder )
    {
        super( builder );

        this.params = builder.params;
        this.nodeDao = builder.nodeDao;
    }

    Node execute()
    {
        final EntityId entityId = params.getEntityId();

        final NodeVersionId currentVersion = this.workspaceService.getCurrentVersion( entityId, WorkspaceContext.from( context ) );

        final Node nodeToBeRenamed = nodeDao.getByVersionId( currentVersion );

        final NodePath parentPath = nodeToBeRenamed.parent().asAbsolute();
        final NodePath targetPath = new NodePath( parentPath, params.getNewNodeName() );
        final EntityId existingNodeAtTargetPath = getExistingNode( targetPath );

        if ( ( existingNodeAtTargetPath != null ) && !nodeToBeRenamed.id().equals( existingNodeAtTargetPath ) )
        {
            throw new NodeAlreadyExistException( targetPath );
        }

        final Nodes children = getChildNodes( nodeToBeRenamed );

        final Node renamedNode = doMoveNode( parentPath, params.getNewNodeName(), params.getEntityId() );

        if ( !children.isEmpty() )
        {
            moveNodesToNewParentPath( children, renamedNode.path() );
        }

        return NodeHasChildResolver.create().
            workspaceService( this.workspaceService ).
            context( this.context ).
            build().
            resolve( renamedNode );
    }

    private Nodes getChildNodes( final Node parentNode )
    {
        final NodeVersionIds childrenVersions = workspaceService.findByParent( parentNode.path(), WorkspaceContext.from( context ) );

        if ( childrenVersions.isEmpty() )
        {
            return Nodes.empty();
        }

        return nodeDao.getByVersionIds( childrenVersions );
    }

    private EntityId getExistingNode( final NodePath path )
    {
        final NodeVersionId existingVersion = workspaceService.getByPath( path, WorkspaceContext.from( context ) );

        if ( existingVersion == null )
        {
            return null;
        }

        final Node existingNode = nodeDao.getByVersionId( existingVersion );

        return existingNode == null ? null : existingNode.id();
    }

    private void moveNodesToNewParentPath( final Nodes nodes, final NodePath newParentPath )
    {
        for ( final Node node : nodes )
        {
            final Node movedNode = doMoveNode( newParentPath, node.name(), node.id() );

            final Nodes children = getChildNodes( movedNode );

            if ( children != null && children.isNotEmpty() )
            {
                moveNodesToNewParentPath( children, movedNode.path().asAbsolute() );
            }
        }
    }

    private Node doMoveNode( final NodePath newParentPath, final NodeName newNodeName, final EntityId id )
    {
        final NodeVersionId currentVersion = this.workspaceService.getCurrentVersion( id, WorkspaceContext.from( context ) );

        final Node persistedNode = nodeDao.getByVersionId( currentVersion );

        if ( persistedNode.path().equals( new NodePath( newParentPath, newNodeName ) ) )
        {
            return persistedNode;
        }

        final Instant now = Instant.now();

        final Node movedNode = Node.newNode( persistedNode ).
            name( newNodeName ).
            parent( newParentPath ).
            modifiedTime( now ).
            modifier( UserKey.superUser() ).
            entityIndexConfig( persistedNode.getEntityIndexConfig() ).
            build();

        final NodeVersionId newVersion = nodeDao.store( movedNode );

        workspaceService.store( StoreWorkspaceDocument.create().
            id( movedNode.id() ).
            parentPath( movedNode.parent() ).
            path( movedNode.path() ).
            nodeVersionId( newVersion ).
            build(), WorkspaceContext.from( context ) );

        versionService.store( EntityVersionDocument.create().
            entityId( movedNode.id() ).
            nodeVersionId( newVersion ).
            build(), this.context.getRepository() );

        indexService.store( movedNode, IndexContext.from( this.context.getWorkspace(), this.context.getRepository() ) );

        return movedNode;
    }

    public static Builder create( final Context context )
    {
        return new Builder( context );
    }

    public static class Builder
        extends AbstractNodeCommand.Builder<Builder>
    {
        Builder( final Context context )
        {
            super( context );
        }

        private RenameNodeParams params;

        private NodeDao nodeDao;

        public Builder params( RenameNodeParams params )
        {
            this.params = params;
            return this;
        }

        public Builder nodeDao( final NodeDao nodeDao )
        {
            this.nodeDao = nodeDao;
            return this;
        }

        public RenameNodeCommand build()
        {
            return new RenameNodeCommand( this );
        }

    }
}
