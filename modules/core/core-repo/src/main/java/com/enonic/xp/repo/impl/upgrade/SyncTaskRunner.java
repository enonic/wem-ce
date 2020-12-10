package com.enonic.xp.repo.impl.upgrade;

import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.blob.BlobStore;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.index.IndexService;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.ProjectService;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.repo.impl.branch.BranchService;
import com.enonic.xp.repo.impl.node.dao.NodeVersionService;
import com.enonic.xp.repo.impl.version.VersionService;

@Component(immediate = true)
public class SyncTaskRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SyncTaskRunner.class );

    private ContentService contentService;

    private NodeService nodeService;

    private ProjectService projectService;

    private IndexService indexService;

    private PageDescriptorService pageDescriptorService;

    private PartDescriptorService partDescriptorService;

    private LayoutDescriptorService layoutDescriptorService;

    private BlobStore blobStore;

    private VersionService versionService;

    private BranchService branchService;

    private NodeVersionService nodeVersionService;

    @Activate
    public void activate()
    {

        Executors.newSingleThreadExecutor().execute( SyncTask.create().
            contentService( contentService ).
            indexService( indexService ).
            projectService( projectService ).
            partDescriptorService( partDescriptorService ).
            layoutDescriptorService( layoutDescriptorService ).
            pageDescriptorService( pageDescriptorService ).
            blobStore( blobStore ).
            versionService( versionService ).
            branchService( branchService ).
            nodeVersionService( nodeVersionService ).
            build() );

    }

    @Reference
    public void setContentService( final ContentService contentService )
    {
        this.contentService = contentService;
    }

    @Reference
    public void setNodeService( final NodeService nodeService )
    {
        this.nodeService = nodeService;
    }

    @Reference
    public void setProjectService( final ProjectService projectService )
    {
        this.projectService = projectService;
    }

    @Reference
    public void setIndexService( final IndexService indexService )
    {
        this.indexService = indexService;
    }

    @Reference
    public void setPageDescriptorService( final PageDescriptorService pageDescriptorService )
    {
        this.pageDescriptorService = pageDescriptorService;
    }

    @Reference
    public void setPartDescriptorService( final PartDescriptorService partDescriptorService )
    {
        this.partDescriptorService = partDescriptorService;
    }

    @Reference
    public void setLayoutDescriptorService( final LayoutDescriptorService layoutDescriptorService )
    {
        this.layoutDescriptorService = layoutDescriptorService;
    }

    @Reference
    public void setBlobStore( final BlobStore blobStore )
    {
        this.blobStore = blobStore;
    }

    @Reference
    public void setVersionService( final VersionService versionService )
    {
        this.versionService = versionService;
    }

    @Reference
    public void setBranchService( final BranchService branchService )
    {
        this.branchService = branchService;
    }

    @Reference
    public void setNodeVersionService( final NodeVersionService nodeVersionService )
    {
        this.nodeVersionService = nodeVersionService;
    }
}
