package com.enonic.wem.core.content;

import com.enonic.wem.api.blob.BlobService;
import com.enonic.wem.api.content.CompareContentParams;
import com.enonic.wem.api.content.CompareContentResult;
import com.enonic.wem.api.content.CompareContentResults;
import com.enonic.wem.api.content.CompareContentsParams;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.ContentPaths;
import com.enonic.wem.api.content.ContentService;
import com.enonic.wem.api.content.Contents;
import com.enonic.wem.api.content.CreateContentParams;
import com.enonic.wem.api.content.DeleteContentParams;
import com.enonic.wem.api.content.DeleteContentResult;
import com.enonic.wem.api.content.FindContentByParentParams;
import com.enonic.wem.api.content.FindContentByParentResult;
import com.enonic.wem.api.content.FindContentByQueryParams;
import com.enonic.wem.api.content.FindContentByQueryResult;
import com.enonic.wem.api.content.FindContentVersionsParams;
import com.enonic.wem.api.content.FindContentVersionsResult;
import com.enonic.wem.api.content.GetActiveContentVersionsParams;
import com.enonic.wem.api.content.GetActiveContentVersionsResult;
import com.enonic.wem.api.content.GetContentByIdsParams;
import com.enonic.wem.api.content.PushContentParams;
import com.enonic.wem.api.content.RenameContentParams;
import com.enonic.wem.api.content.UpdateContentParams;
import com.enonic.wem.api.content.ValidateContentData;
import com.enonic.wem.api.content.attachment.AttachmentService;
import com.enonic.wem.api.content.data.ContentData;
import com.enonic.wem.api.content.site.CreateSiteParams;
import com.enonic.wem.api.content.site.ModuleConfig;
import com.enonic.wem.api.content.site.ModuleConfigDataSerializer;
import com.enonic.wem.api.content.site.Site;
import com.enonic.wem.api.context.Context;
import com.enonic.wem.api.data.DataId;
import com.enonic.wem.api.data.Value;
import com.enonic.wem.api.schema.content.ContentTypeForms;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.content.ContentTypeService;
import com.enonic.wem.api.schema.content.validator.DataValidationErrors;
import com.enonic.wem.core.entity.NodeService;
import com.enonic.wem.core.index.query.QueryService;

public class ContentServiceImpl
    implements ContentService
{
    private ContentTypeService contentTypeService;

    private NodeService nodeService;

    private BlobService blobService;

    private AttachmentService attachmentService;

    private QueryService queryService;

    private ContentNodeTranslator contentNodeTranslator;

    private final static ModuleConfigDataSerializer MODULE_CONFIG_DATA_SERIALIZER = new ModuleConfigDataSerializer();

    @Override
    public Content getById( final ContentId id, final Context context )
    {
        return GetContentByIdCommand.create( id ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Site getNearestSite( final ContentId contentId, final Context context )
    {
        return GetNearestSiteCommand.create().
            contentService( this ).
            contentId( contentId ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Contents getByIds( final GetContentByIdsParams params, final Context context )
    {
        return GetContentByIdsCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            queryService( this.queryService ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Content getByPath( final ContentPath path, final Context context )
    {
        return GetContentByPathCommand.create( path ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Contents getByPaths( final ContentPaths paths, final Context context )
    {
        return GetContentByPathsCommand.create( paths ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public FindContentByParentResult findByParent( final FindContentByParentParams params, final Context context )
    {
        return FindContentByParentCommand.create( params ).
            queryService( this.queryService ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Site create( final CreateSiteParams params, final Context context )
    {
        final ContentData data = new ContentData();
        data.setProperty( DataId.from( "description", 0 ), Value.newString( params.getDescription() ) );
        for ( final ModuleConfig moduleConfig : params.getModuleConfigs() )
        {
            data.addProperty( "modules", Value.newData( MODULE_CONFIG_DATA_SERIALIZER.toData( moduleConfig ) ) );
        }

        final CreateContentParams createContentParams = new CreateContentParams().
            contentType( ContentTypeName.site() ).
            parent( params.getParentContentPath() ).
            name( params.getName() ).
            displayName( params.getDisplayName() ).
            form( ContentTypeForms.SITE ).
            contentData( data ).draft( params.isDraft() );

        final Site site = (Site) CreateContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            params( createContentParams ).
            context( context ).
            build().
            execute();

        this.create( new CreateContentParams().
            owner( site.getOwner() ).
            displayName( "Templates" ).
            name( "templates" ).
            parent( site.getPath() ).
            contentType( ContentTypeName.folder() ).
            draft( false ).
            contentData( new ContentData() ), context );

        return site;
    }

    @Override
    public Content create( final CreateContentParams params, final Context context )
    {
        final Content content = CreateContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            params( params ).
            context( context ).
            build().
            execute();

        if ( content instanceof Site )
        {
            this.create( new CreateContentParams().
                owner( content.getOwner() ).
                displayName( "Templates" ).
                name( "templates" ).
                parent( content.getPath() ).
                contentType( ContentTypeName.folder() ).
                draft( false ).
                contentData( new ContentData() ), context );
        }

        return content;
    }

    @Override
    public Content update( final UpdateContentParams params, final Context context )
    {
        return UpdateContentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            attachmentService( this.attachmentService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public DeleteContentResult delete( final DeleteContentParams params, final Context context )
    {
        return DeleteContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            params( params ).
            context( context ).
            build().
            execute();
    }

    @Override
    public Content push( final PushContentParams params, final Context context )
    {
        params.getContentId();

        return PushContentCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            contentId( params.getContentId() ).
            target( params.getTarget() ).
            context( context ).
            build().
            execute();
    }

    @Override
    public DataValidationErrors validate( final ValidateContentData data, final Context context )
    {
        return new ValidateContentDataCommand().
            contentTypeService( this.contentTypeService ).
            data( data ).
            execute();
    }

    @Override
    public Content rename( final RenameContentParams params, final Context context )
    {
        return RenameContentCommand.create( params ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public FindContentByQueryResult find( final FindContentByQueryParams params, final Context context )
    {
        return FindContentByQueryCommand.create().
            params( params ).
            queryService( this.queryService ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            build().
            execute();
    }

    @Override
    public CompareContentResult compare( final CompareContentParams params, final Context context )
    {
        return CompareContentCommand.create().
            nodeService( this.nodeService ).
            context( context ).
            contentId( params.getContentId() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public CompareContentResults compare( final CompareContentsParams params, final Context context )
    {
        return CompareContentsCommand.create().
            nodeService( this.nodeService ).
            context( context ).
            contentIds( params.getContentIds() ).
            target( params.getTarget() ).
            build().
            execute();
    }

    @Override
    public FindContentVersionsResult getVersions( final FindContentVersionsParams params, final Context context )
    {
        return FindContentVersionsCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            contentId( params.getContentId() ).
            from( params.getFrom() ).
            size( params.getSize() ).
            build().
            execute();
    }


    @Override
    public GetActiveContentVersionsResult getActiveVersions( final GetActiveContentVersionsParams params, final Context context )
    {
        return GetActiveContentVersionsCommand.create().
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            blobService( this.blobService ).
            translator( this.contentNodeTranslator ).
            context( context ).
            contentId( params.getContentId() ).
            workspaces( params.getWorkspaces() ).
            build().
            execute();
    }

    @Override
    public String generateContentName( final String displayName )
    {
        return new ContentPathNameGenerator().generatePathName( displayName );
    }

    public void setContentTypeService( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
    }

    public void setNodeService( final NodeService nodeService )
    {
        this.nodeService = nodeService;
    }

    public void setBlobService( final BlobService blobService )
    {
        this.blobService = blobService;
    }

    public void setAttachmentService( final AttachmentService attachmentService )
    {
        this.attachmentService = attachmentService;
    }

    public void setQueryService( final QueryService queryService )
    {
        this.queryService = queryService;
    }

    public void setContentNodeTranslator( final ContentNodeTranslator contentNodeTranslator )
    {
        this.contentNodeTranslator = contentNodeTranslator;
    }
}
