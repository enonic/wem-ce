package com.enonic.wem.admin.json.content.page.layout;

import java.util.List;

import com.enonic.wem.admin.json.data.DataJson;
import com.enonic.wem.admin.json.data.RootDataSetJson;
import com.enonic.wem.api.content.page.layout.LayoutTemplate;


public class LayoutTemplateJson
    extends LayoutTemplateSummaryJson
{
    private final RootDataSetJson configJson;

    private final LayoutDescriptorJson descriptorJson;

    public LayoutTemplateJson( final LayoutTemplate template )
    {
        this( template, null );
    }

    public List<DataJson> getConfig()
    {
        return configJson.getSet();
    }

    public LayoutTemplateJson( final LayoutTemplate template, final LayoutDescriptorJson descriptorJson )
    {
        super( template );
        this.configJson = new RootDataSetJson( template.getConfig() );
        this.descriptorJson = descriptorJson;
    }

    public LayoutDescriptorJson getDescriptor()
    {
        return descriptorJson;
    }
}
