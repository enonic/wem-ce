package com.enonic.xp.admin.impl.json.content;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.enonic.xp.admin.impl.json.ChangeTraceableJson;
import com.enonic.xp.admin.impl.json.ItemJson;
import com.enonic.xp.admin.impl.json.thumb.ThumbnailJson;
import com.enonic.xp.admin.impl.rest.resource.content.ContentIconUrlResolver;
import com.enonic.xp.admin.impl.rest.resource.content.json.ChildOrderJson;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentInheritType;

@SuppressWarnings("UnusedDeclaration")
public class ContentSummaryJson
    extends ContentIdJson
    implements ChangeTraceableJson, ItemJson
{
    private final Content content;

    private final String iconUrl;

    private final ThumbnailJson thumbnailJson;

    private final boolean isSite;

    private final boolean isPage;

    private final List<ContentInheritType> inherit;

    private final ChildOrderJson childOrderJson;

    private final ContentPublishInfoJson publish;

    private final String contentState;

    private final ContentWorkflowInfoJson workflow;

    private final String originProject;

    public ContentSummaryJson( final Content content, final ContentIconUrlResolver iconUrlResolver )
    {
        super( content.getId() );
        this.content = content;
        this.iconUrl = iconUrlResolver.resolve( content );
        this.thumbnailJson = content.hasThumbnail() ? new ThumbnailJson( content.getThumbnail() ) : null;
        this.isSite = content.isSite();
        this.isPage = content.hasPage();
        this.inherit = content.getInherit().
            stream().
            sorted().
            collect( Collectors.toList() );
        this.childOrderJson = content.getChildOrder() != null ? new ChildOrderJson( content.getChildOrder() ) : null;
        this.contentState = content.getContentState().toString();
        this.publish = content.getPublishInfo() != null ? new ContentPublishInfoJson( content.getPublishInfo() ) : null;
        this.workflow = content.getWorkflowInfo() != null ? new ContentWorkflowInfoJson( content.getWorkflowInfo() ) : null;
        this.originProject = content.getOriginProject() != null ? content.getOriginProject().toString() : null;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public ThumbnailJson getThumbnail()
    {
        return this.thumbnailJson;
    }

    public String getPath()
    {
        return content.getPath().toString();
    }

    public String getName()
    {
        return content.getName().toString();
    }

    public String getType()
    {
        return content.getType() != null ? content.getType().toString() : null;
    }

    public String getDisplayName()
    {
        return content.getDisplayName();
    }

    public String getOwner()
    {
        return content.getOwner() != null ? content.getOwner().toString() : null;
    }

    public String getLanguage()
    {
        return content.getLanguage() != null ? content.getLanguage().toLanguageTag() : null;
    }

    public boolean getIsRoot()
    {
        return content.isRoot();
    }

    public ContentPublishInfoJson getPublish()
    {
        return publish;
    }

    public ContentWorkflowInfoJson getWorkflow()
    {
        return workflow;
    }

    @Override
    public Instant getCreatedTime()
    {
        return content.getCreatedTime();
    }

    @Override
    public String getCreator()
    {
        return content.getCreator() != null ? content.getCreator().toString() : null;
    }

    @Override
    public Instant getModifiedTime()
    {
        return content.getModifiedTime();
    }

    @Override
    public String getModifier()
    {
        return content.getModifier() != null ? content.getModifier().toString() : null;
    }

    public boolean getHasChildren()
    {
        return content.hasChildren();
    }

    public boolean getIsValid()
    {
        return content.isValid();
    }

    public ChildOrderJson getChildOrder()
    {
        return this.childOrderJson;
    }

    public boolean getIsPage()
    {
        return isPage;
    }

    public List<ContentInheritType> getInherit()
    {
        return inherit;
    }

    public String getContentState()
    {
        return this.contentState;
    }

    public String getOriginProject()
    {
        return this.originProject;
    }

    @Override
    public boolean getEditable()
    {
        return true;
    }

    @Override
    public boolean getDeletable()
    {
        return true;
    }

}
