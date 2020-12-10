package com.enonic.xp.repo.impl.upgrade.serializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.base.Preconditions;

import com.enonic.xp.attachment.Attachment;
import com.enonic.xp.attachment.AttachmentNames;
import com.enonic.xp.attachment.AttachmentSerializer;
import com.enonic.xp.attachment.Attachments;
import com.enonic.xp.attachment.CreateAttachment;
import com.enonic.xp.attachment.CreateAttachments;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentIds;
import com.enonic.xp.content.ContentInheritType;
import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.ContentPublishInfo;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.content.CreateContentTranslatorParams;
import com.enonic.xp.content.ExtraDatas;
import com.enonic.xp.content.UpdateContentTranslatorParams;
import com.enonic.xp.content.WorkflowInfo;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.icon.Thumbnail;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.page.Page;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.project.ProjectName;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.util.BinaryReference;
import com.enonic.xp.util.BinaryReferences;
import com.enonic.xp.util.Exceptions;
import com.enonic.xp.util.Reference;

import static com.enonic.xp.content.ContentPropertyNames.ATTACHMENT;
import static com.enonic.xp.content.ContentPropertyNames.CREATED_TIME;
import static com.enonic.xp.content.ContentPropertyNames.CREATOR;
import static com.enonic.xp.content.ContentPropertyNames.DATA;
import static com.enonic.xp.content.ContentPropertyNames.DISPLAY_NAME;
import static com.enonic.xp.content.ContentPropertyNames.EXTRA_DATA;
import static com.enonic.xp.content.ContentPropertyNames.INHERIT;
import static com.enonic.xp.content.ContentPropertyNames.LANGUAGE;
import static com.enonic.xp.content.ContentPropertyNames.MODIFIED_TIME;
import static com.enonic.xp.content.ContentPropertyNames.MODIFIER;
import static com.enonic.xp.content.ContentPropertyNames.ORIGIN_PROJECT;
import static com.enonic.xp.content.ContentPropertyNames.OWNER;
import static com.enonic.xp.content.ContentPropertyNames.PROCESSED_REFERENCES;
import static com.enonic.xp.content.ContentPropertyNames.PUBLISH_FIRST;
import static com.enonic.xp.content.ContentPropertyNames.PUBLISH_FROM;
import static com.enonic.xp.content.ContentPropertyNames.PUBLISH_INFO;
import static com.enonic.xp.content.ContentPropertyNames.PUBLISH_TO;
import static com.enonic.xp.content.ContentPropertyNames.TYPE;
import static com.enonic.xp.content.ContentPropertyNames.VALID;
import static com.enonic.xp.content.ContentPropertyNames.WORKFLOW_INFO;
import static com.enonic.xp.content.ContentPropertyNames.WORKFLOW_INFO_CHECKS;
import static com.enonic.xp.content.ContentPropertyNames.WORKFLOW_INFO_STATE;
import static com.enonic.xp.repo.impl.upgrade.serializer.ComponentDataSerializer.COMPONENTS;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

public class ContentDataSerializer
{
    private PageDataSerializer pageDataSerializer;

    private ExtraDataSerializer extraDataSerializer;

    private WorkflowInfoSerializer workflowInfoSerializer;

    private PublishInfoSerializer publishInfoSerializer;

    private ContentDataSerializer( final Builder builder )
    {
        this.pageDataSerializer = PageDataSerializer.create().
            pageDescriptorService( builder.pageDescriptorService ).
            partDescriptorService( builder.partDescriptorService ).
            layoutDescriptorService( builder.layoutDescriptorService ).
            contentService( builder.contentService ).
            build();

        this.extraDataSerializer = new ExtraDataSerializer();
        this.workflowInfoSerializer = new WorkflowInfoSerializer();
        this.publishInfoSerializer = new PublishInfoSerializer();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public PropertyTree toCreateNodeData( final CreateContentTranslatorParams params )
    {
        final PropertyTree propertyTree = new PropertyTree();

        final PropertySet contentAsData = propertyTree.getRoot();

        contentAsData.addBoolean( VALID, params.isValid() );
        contentAsData.ifNotNull().addString( DISPLAY_NAME, params.getDisplayName() );
        contentAsData.ifNotNull().addString( TYPE, params.getType() != null ? params.getType().toString() : null );
        contentAsData.ifNotNull().addInstant( CREATED_TIME, params.getCreatedTime() );
        contentAsData.ifNotNull().addString( CREATOR, params.getCreator().toString() );
        contentAsData.ifNotNull().addString( MODIFIER, params.getModifier().toString() );
        contentAsData.ifNotNull().addInstant( MODIFIED_TIME, params.getModifiedTime() );
        contentAsData.ifNotNull().addString( OWNER, PrincipalKey.ofAnonymous().equals( params.getOwner() ) || params.getOwner() == null
            ? null
            : params.getOwner().toString() );
        contentAsData.ifNotNull().addString( LANGUAGE, params.getLanguage() != null ? params.getLanguage().toLanguageTag() : null );
        contentAsData.ifNotNull().addString( ORIGIN_PROJECT,
                                             params.getOriginProject() != null ? params.getOriginProject().toString() : null );
        contentAsData.addSet( DATA, params.getData().getRoot().copy( contentAsData.getTree() ) );

        addPublishInfo( contentAsData, params.getContentPublishInfo() );
        addWorkflowInfo( contentAsData, params.getWorkflowInfo() );
        addInherit( contentAsData, params.getInherit() );

        final ExtraDatas extraData = params.getExtraDatas();

        if ( extraData != null && !extraData.isEmpty() )
        {
            extraDataSerializer.toData( extraData, contentAsData );
        }

        if ( params.getPage() != null )
        {
            pageDataSerializer.toData( params.getPage(), contentAsData );
        }

        if ( params.getCreateAttachments() != null )
        {
            addAttachmentInfoToDataset( params.getCreateAttachments(), contentAsData );
        }

        addProcessedReferences( contentAsData, params.getProcessedIds() );

        return propertyTree;
    }

    public PropertyTree toUpdateNodeData( final UpdateContentTranslatorParams params )
    {
        final PropertyTree newPropertyTree = new PropertyTree();
        final PropertySet contentAsData = newPropertyTree.getRoot();

        final Content content = params.getEditedContent();

        addMetadata( contentAsData, content, params.getModifier() );
        contentAsData.addSet( DATA, content.getData().getRoot().copy( contentAsData.getTree() ) );

        if ( content.hasExtraData() )
        {
            extraDataSerializer.toData( content.getAllExtraData(), contentAsData );
        }

        final Attachments attachments = mergeExistingAndUpdatedAttachments( content.getAttachments(), params );

        applyAttachmentsAsData( attachments, contentAsData );

        if ( content.hasPage() )
        {
            pageDataSerializer.toData( content.getPage(), contentAsData );
        }

        addProcessedReferences( contentAsData, content.getProcessedReferences() );

        return newPropertyTree;
    }

    public PropertyTree toNodeData( final Content content )
    {
        final PropertyTree propertyTree = new PropertyTree();

        final PropertySet contentAsData = propertyTree.getRoot();

        addMetadata( contentAsData, content, content.getModifier() );

        contentAsData.addSet( DATA, content.getData().getRoot().copy( contentAsData.getTree() ) );
        final ExtraDatas extraData = content.getAllExtraData();

        if ( extraData != null && !extraData.isEmpty() )
        {
            extraDataSerializer.toData( extraData, contentAsData );
        }

        if ( content.getPage() != null )
        {
            pageDataSerializer.toData( content.getPage(), contentAsData );
        }

        if ( content.getAttachments() != null )
        {
            applyAttachmentsAsData( content.getAttachments(), contentAsData );
        }

        addProcessedReferences( contentAsData, content.getProcessedReferences() );

        return propertyTree;
    }

    public void toPageData( final Page page, final PropertySet parent )
    {
        pageDataSerializer.toData( page, parent );
    }

    public Page fromPageData( final PropertySet asSet )
    {
        return pageDataSerializer.fromData( asSet );
    }

    public ExtraDatas fromExtraData( final PropertySet asSet )
    {
        return extraDataSerializer.fromData( asSet );
    }

    public Content.Builder fromData( final PropertySet contentAsSet )
    {
        final ContentTypeName contentTypeName = ContentTypeName.from( contentAsSet.getString( ContentPropertyNames.TYPE ) );
        final Content.Builder builder = Content.create( contentTypeName );

        builder.displayName( contentAsSet.getString( DISPLAY_NAME ) );
        builder.valid( contentAsSet.getBoolean( VALID ) != null ? contentAsSet.getBoolean( ContentPropertyNames.VALID ) : false );
        builder.data( contentAsSet.getSet( DATA ).toTree() );

        extractUserInfo( contentAsSet, builder );
        extractOwner( contentAsSet, builder );
        extractLanguage( contentAsSet, builder );
        extractExtradata( contentAsSet, builder );
        extractPage( contentAsSet, builder );
        extractAttachments( contentAsSet, builder );
        extractPublishInfo( contentAsSet, builder );
        extractProcessedReferences( contentAsSet, builder );
        extractWorkflowInfo( contentAsSet, builder );
        extractInherit( contentAsSet, builder );
        extractOriginProject( contentAsSet, builder );

        return builder;
    }

    private void addMetadata( final PropertySet contentAsData, final Content content, final PrincipalKey modifier )
    {
        contentAsData.setBoolean( ContentPropertyNames.VALID, content.isValid() );
        contentAsData.ifNotNull().addString( DISPLAY_NAME, content.getDisplayName() );
        contentAsData.ifNotNull().addString( TYPE, content.getType().toString() );
        contentAsData.ifNotNull().addString( OWNER, content.getOwner() != null ? content.getOwner().toString() : null );
        contentAsData.ifNotNull().addString( LANGUAGE, content.getLanguage() != null ? content.getLanguage().toLanguageTag() : null );
        contentAsData.ifNotNull().addInstant( MODIFIED_TIME, content.getModifiedTime() );
        contentAsData.ifNotNull().addString( MODIFIER, modifier.toString() );
        contentAsData.ifNotNull().addString( CREATOR, content.getCreator().toString() );
        contentAsData.ifNotNull().addInstant( CREATED_TIME, content.getCreatedTime() );
        contentAsData.ifNotNull().addString( ORIGIN_PROJECT,
                                             content.getOriginProject() != null ? content.getOriginProject().toString() : null );
        addPublishInfo( contentAsData, content.getPublishInfo() );
        addWorkflowInfo( contentAsData, content.getWorkflowInfo() );
        addInherit( contentAsData, content.getInherit() );
    }

    private void addProcessedReferences( final PropertySet contentAsData, final ContentIds processedIds )
    {
        if ( processedIds == null )
        {
            return;
        }
        contentAsData.ifNotNull().addReferences( PROCESSED_REFERENCES, processedIds.
            stream().
            map( contentId -> new Reference( NodeId.from( contentId ) ) ).
            toArray( Reference[]::new ) );
    }

    private void addPublishInfo( final PropertySet contentAsData, final ContentPublishInfo data )
    {
        if ( data != null )
        {
            final PropertySet publishInfo = contentAsData.addSet( PUBLISH_INFO );
            publishInfo.addInstant( PUBLISH_FIRST, data.getFirst() );
            publishInfo.addInstant( PUBLISH_FROM, data.getFrom() );
            publishInfo.addInstant( PUBLISH_TO, data.getTo() );
        }
    }

    private void addWorkflowInfo( final PropertySet contentAsData, final WorkflowInfo data )
    {
        if ( data != null )
        {
            final PropertySet workflowInfo = contentAsData.addSet( WORKFLOW_INFO );
            workflowInfo.addString( WORKFLOW_INFO_STATE, data.getState().toString() );

            final PropertySet workflowInfoChecks = workflowInfo.addSet( WORKFLOW_INFO_CHECKS );
            data.getChecks().forEach( ( key, value ) -> workflowInfoChecks.addString( key, value.toString() ) );
        }
    }

    private void addInherit( final PropertySet contentAsData, final Set<ContentInheritType> inherit )
    {
        if ( inherit != null )
        {
            contentAsData.ifNotNull().addStrings( INHERIT, inherit.
                stream().
                map( Enum::name ).
                collect( Collectors.toSet() ) );
        }
    }

    private void extractUserInfo( final PropertySet contentAsSet, final Content.Builder builder )
    {
        builder.creator( PrincipalKey.from( contentAsSet.getString( CREATOR ) ) );
        builder.createdTime( contentAsSet.getInstant( CREATED_TIME ) );
        builder.modifier( contentAsSet.getString( MODIFIER ) != null ? PrincipalKey.from( contentAsSet.getString( MODIFIER ) ) : null );
        builder.modifiedTime( contentAsSet.getInstant( MODIFIED_TIME ) );
    }

    private void extractPublishInfo( final PropertySet contentAsSet, final Content.Builder builder )
    {
        final ContentPublishInfo publishInfo = publishInfoSerializer.serialize( contentAsSet );

        if ( publishInfo != null )
        {
            builder.publishInfo( publishInfo );
        }
    }

    private void extractAttachments( final PropertySet contentAsSet, final Content.Builder builder )
    {
        final Attachments attachments = dataToAttachments( contentAsSet.getSets( ATTACHMENT ) );
        builder.attachments( attachments );

        final Attachment thumbnailAttachment = attachments.byName( AttachmentNames.THUMBNAIL );
        if ( thumbnailAttachment != null )
        {
            final BinaryReference thumbnailBinaryRef = thumbnailAttachment.getBinaryReference();
            final Thumbnail thumbnail =
                Thumbnail.from( thumbnailBinaryRef, thumbnailAttachment.getMimeType(), thumbnailAttachment.getSize() );
            builder.thumbnail( thumbnail );
        }
    }

    private void extractPage( final PropertySet contentAsSet, final Content.Builder builder )
    {
        if ( contentAsSet.hasProperty( COMPONENTS ) )
        {
            builder.page( pageDataSerializer.fromData( contentAsSet ) );
        }
    }

    private void extractExtradata( final PropertySet contentAsSet, final Content.Builder builder )
    {
        final ExtraDatas extraData = extraDataSerializer.fromData( contentAsSet.getSet( EXTRA_DATA ) );

        if ( extraData != null && extraData.isNotEmpty() )
        {
            builder.extraDatas( extraData );
        }
    }

    private void extractLanguage( final PropertySet contentAsSet, final Content.Builder builder )
    {
        String language = contentAsSet.getString( LANGUAGE );
        if ( !isNullOrEmpty( language ) )
        {
            builder.language( Locale.forLanguageTag( language ) );
        }
    }

    private void extractOwner( final PropertySet contentAsSet, final Content.Builder builder )
    {
        String owner = contentAsSet.getString( OWNER );

        if ( !nullToEmpty( owner ).isBlank() )
        {
            builder.owner( PrincipalKey.from( owner ) );
        }
    }

    private void extractProcessedReferences( final PropertySet contentAsSet, final Content.Builder builder )
    {
        Iterable<Reference> references = contentAsSet.getReferences( PROCESSED_REFERENCES );

        references.forEach( reference -> builder.addProcessedReference( ContentId.from( reference ) ) );
    }

    private void extractWorkflowInfo( final PropertySet contentAsSet, final Content.Builder builder )
    {
        final PropertySet workflowInfoSet = contentAsSet.getSet( WORKFLOW_INFO );
        final WorkflowInfo workflowInfo = workflowInfoSerializer.extract( workflowInfoSet );
        builder.workflowInfo( workflowInfo );
    }

    private void extractInherit( final PropertySet contentAsSet, final Content.Builder builder )
    {
        builder.setInherit( StreamSupport.
            stream( contentAsSet.getStrings( INHERIT ).spliterator(), false ).
            map( ContentInheritType::valueOf ).
            collect( Collectors.toSet() ) );
    }

    private void extractOriginProject( final PropertySet contentAsSet, final Content.Builder builder )
    {
        final String originProject = contentAsSet.getString( ORIGIN_PROJECT );
        if ( !isNullOrEmpty( originProject ) )
        {
            builder.originProject( ProjectName.from( originProject ) );
        }
    }

    private Attachments dataToAttachments( final Iterable<PropertySet> attachmentSets )
    {
        final Attachments.Builder attachments = Attachments.create();
        for ( final PropertySet attachmentAsSet : attachmentSets )
        {
            attachments.add( Attachment.create().
                name( attachmentAsSet.getString( ContentPropertyNames.ATTACHMENT_NAME ) ).
                label( attachmentAsSet.getString( ContentPropertyNames.ATTACHMENT_LABEL ) ).
                mimeType( attachmentAsSet.getString( ContentPropertyNames.ATTACHMENT_MIMETYPE ) ).
                size( attachmentAsSet.getLong( ContentPropertyNames.ATTACHMENT_SIZE ) ).
                textContent( attachmentAsSet.getString( ContentPropertyNames.ATTACHMENT_TEXT ) ).
                build() );
        }
        return attachments.build();
    }

    private void addAttachmentInfoToDataset( final CreateAttachments createAttachments, final PropertySet contentAsData )
    {
        AttachmentSerializer.create( contentAsData.getTree(), createAttachments );
    }

    private void applyAttachmentsAsData( final Attachments attachments, final PropertySet contentAsData )
    {
        for ( final Attachment attachment : attachments )
        {
            final PropertySet attachmentSet = contentAsData.addSet( ATTACHMENT );
            attachmentSet.addString( ContentPropertyNames.ATTACHMENT_NAME, attachment.getName() );
            attachmentSet.addString( ContentPropertyNames.ATTACHMENT_LABEL, attachment.getLabel() );
            attachmentSet.addBinaryReference( "binary", attachment.getBinaryReference() );
            attachmentSet.addString( ContentPropertyNames.ATTACHMENT_MIMETYPE, attachment.getMimeType() );
            attachmentSet.addLong( ContentPropertyNames.ATTACHMENT_SIZE, attachment.getSize() );
            attachmentSet.addString( ContentPropertyNames.ATTACHMENT_TEXT, attachment.getTextContent() );
        }
    }

    private Attachments mergeExistingAndUpdatedAttachments( final Attachments existingAttachments,
                                                            final UpdateContentTranslatorParams params )
    {
        CreateAttachments createAttachments = params.getCreateAttachments();
        BinaryReferences removeAttachments = params.getRemoveAttachments();
        if ( createAttachments == null && removeAttachments == null && !params.isClearAttachments() )
        {
            return existingAttachments;
        }

        createAttachments = createAttachments == null ? CreateAttachments.empty() : createAttachments;
        removeAttachments = removeAttachments == null ? BinaryReferences.empty() : removeAttachments;

        final Map<BinaryReference, Attachment> attachments = new LinkedHashMap<>();
        if ( !params.isClearAttachments() )
        {
            existingAttachments.stream().forEach( ( a ) -> attachments.put( a.getBinaryReference(), a ) );
        }
        removeAttachments.stream().forEach( attachments::remove );

        // added attachments with same BinaryReference will replace existing ones
        for ( final CreateAttachment createAttachment : createAttachments )
        {
            final Attachment attachment = Attachment.create().
                name( createAttachment.getName() ).
                label( createAttachment.getLabel() ).
                mimeType( createAttachment.getMimeType() ).
                size( attachmentSize( createAttachment ) ).
                textContent( createAttachment.getTextContent() ).
                build();
            attachments.put( attachment.getBinaryReference(), attachment );
        }
        return Attachments.from( attachments.values() );
    }

    private long attachmentSize( final CreateAttachment createAttachment )
    {
        try
        {
            return createAttachment.getByteSource().size();
        }
        catch ( IOException e )
        {
            throw Exceptions.unchecked( e );
        }
    }

    public static class Builder
    {
        private PageDescriptorService pageDescriptorService;

        private PartDescriptorService partDescriptorService;

        private LayoutDescriptorService layoutDescriptorService;

        private ContentService contentService;

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

        public Builder contentService( final ContentService value )
        {
            this.contentService = value;
            return this;
        }

        void validate()
        {
            Preconditions.checkNotNull( pageDescriptorService );
            Preconditions.checkNotNull( partDescriptorService );
            Preconditions.checkNotNull( layoutDescriptorService );
            Preconditions.checkNotNull( contentService );
        }

        public ContentDataSerializer build()
        {
            validate();
            return new ContentDataSerializer( this );
        }
    }

}
