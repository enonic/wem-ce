package com.enonic.xp.site.components;

import java.util.ArrayList;
import java.util.List;

public final class SiteComponentDescriptors
{
    private final List<SiteComponentDescriptor> sitePartDescriptors;

    private final List<SiteComponentDescriptor> siteLayoutDescriptors;

    private SiteComponentDescriptors( final Builder builder )
    {
        this.sitePartDescriptors = builder.partSiteDescriptors;
        this.siteLayoutDescriptors = builder.layoutSiteDescriptors;
    }

    public List<SiteComponentDescriptor> getPartSiteDescriptors()
    {
        return sitePartDescriptors;
    }

    public List<SiteComponentDescriptor> getLayoutSiteDescriptors()
    {
        return siteLayoutDescriptors;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final List<SiteComponentDescriptor> partSiteDescriptors = new ArrayList<>();

        private final List<SiteComponentDescriptor> layoutSiteDescriptors = new ArrayList<>();

        public Builder addPartComponentDescriptor( final SiteComponentDescriptor partSiteDescriptor )
        {
            this.partSiteDescriptors.add( partSiteDescriptor );
            return this;
        }

        public Builder addLayoutComponentDescriptor( final SiteComponentDescriptor layoutSiteDescriptor )
        {
            this.layoutSiteDescriptors.add( layoutSiteDescriptor );
            return this;
        }

        public SiteComponentDescriptors build()
        {
            return new SiteComponentDescriptors( this );
        }
    }
}
