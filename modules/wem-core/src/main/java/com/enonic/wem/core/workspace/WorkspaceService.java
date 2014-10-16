package com.enonic.wem.core.workspace;

import com.enonic.wem.core.entity.NodeId;
import com.enonic.wem.core.entity.NodeIds;
import com.enonic.wem.core.entity.NodePath;
import com.enonic.wem.core.entity.NodePaths;
import com.enonic.wem.core.entity.NodeVersionId;
import com.enonic.wem.core.entity.NodeVersionIds;
import com.enonic.wem.core.workspace.compare.query.CompareWorkspacesQuery;

public interface WorkspaceService
{
    public void store( final StoreWorkspaceDocument storeWorkspaceDocument, final WorkspaceContext context );

    public void delete( final NodeId nodeId, final WorkspaceContext context );

    public NodeVersionId getCurrentVersion( final NodeId nodeId, final WorkspaceContext context );

    public NodeVersionIds getByVersionIds( final NodeIds nodeIds, final WorkspaceContext context );

    public NodeVersionId getByPath( final NodePath nodePath, final WorkspaceContext context );

    public NodeVersionIds getByPaths( final NodePaths nodePaths, final WorkspaceContext context );

    public NodeVersionIds findByParent( final NodePath parentPath, final WorkspaceContext context );

    public NodeIds findNodesWithDifferences( final CompareWorkspacesQuery query, final WorkspaceContext context );

    public boolean hasChildren( final NodePath parentPath, final WorkspaceContext context );

}
