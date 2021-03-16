package com.enonic.xp.site;


import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.form.Form;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.site.components.SiteComponentDescriptors;
import com.enonic.xp.site.mapping.ControllerMappingDescriptors;
import com.enonic.xp.site.processor.ResponseProcessorDescriptors;

@PublicApi
public final class SiteDescriptor
{
    private static final String SITE_DESCRIPTOR_PATH = "site/site.xml";

    private final Form form;

    private final XDataMappings xDataMappings;

    private final ResponseProcessorDescriptors responseProcessors;

    private final ControllerMappingDescriptors mappingDescriptors;

    private final SiteComponentDescriptors siteComponentDescriptors;

    private SiteDescriptor( final Builder builder )
    {
        this.form = builder.form;
        this.xDataMappings = builder.xDataMappings;
        this.responseProcessors = builder.responseProcessors != null ? builder.responseProcessors : ResponseProcessorDescriptors.empty();
        this.mappingDescriptors = builder.mappingDescriptors != null ? builder.mappingDescriptors : ControllerMappingDescriptors.empty();
        this.siteComponentDescriptors = builder.components;
    }

    public Form getForm()
    {
        return form;
    }

    public XDataMappings getXDataMappings()
    {
        return xDataMappings;
    }

    public ResponseProcessorDescriptors getResponseProcessors()
    {
        return responseProcessors;
    }

    public ControllerMappingDescriptors getMappingDescriptors()
    {
        return mappingDescriptors;
    }

    public SiteComponentDescriptors getSiteComponents()
    {
        return siteComponentDescriptors;
    }

    public static ResourceKey toResourceKey( final ApplicationKey applicationKey )
    {
        return ResourceKey.from( applicationKey, SITE_DESCRIPTOR_PATH );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static SiteDescriptor.Builder copyOf( final SiteDescriptor siteDescriptor )
    {
        return new Builder( siteDescriptor );
    }

    public static class Builder
    {
        private Form form;

        private XDataMappings xDataMappings;

        private ResponseProcessorDescriptors responseProcessors;

        private ControllerMappingDescriptors mappingDescriptors;

        private SiteComponentDescriptors components;

        private Builder()
        {
        }

        private Builder( final SiteDescriptor siteDescriptor )
        {
            this.form = siteDescriptor.form != null ? siteDescriptor.form.copy() : null;
            this.xDataMappings = siteDescriptor.xDataMappings;
            this.responseProcessors = siteDescriptor.responseProcessors;
            this.mappingDescriptors = siteDescriptor.mappingDescriptors;
        }

        public Builder form( final Form form )
        {
            this.form = form;
            return this;
        }

        public Builder xDataMappings( final XDataMappings xDataMappings )
        {
            this.xDataMappings = xDataMappings;
            return this;
        }

        public Builder responseProcessors( final ResponseProcessorDescriptors responseProcessors )
        {
            this.responseProcessors = responseProcessors;
            return this;
        }

        public Builder mappingDescriptors( final ControllerMappingDescriptors mappingDescriptors )
        {
            this.mappingDescriptors = mappingDescriptors;
            return this;
        }

        public Builder components( final SiteComponentDescriptors components )
        {
            this.components = components;
            return this;
        }

        public SiteDescriptor build()
        {
            return new SiteDescriptor( this );
        }
    }
}
