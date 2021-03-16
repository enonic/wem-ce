package com.enonic.xp.site.components;

import java.util.ArrayList;
import java.util.List;

public final class SiteComponentDescriptor
{
    private final List<DescriptorPattern> patterns;

    private final List<DescriptorPattern> contentTypePermits;

    private SiteComponentDescriptor( final Builder builder )
    {
        patterns = builder.patterns;
        contentTypePermits = builder.contentTypePermits;
    }

    public List<DescriptorPattern> getPatterns()
    {
        return patterns;
    }

    public List<DescriptorPattern> getContentTypePermits()
    {
        return contentTypePermits;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final List<DescriptorPattern> patterns = new ArrayList<>();

        private final List<DescriptorPattern> contentTypePermits = new ArrayList<>();

        public Builder addPattern( DescriptorPattern descriptorPattern )
        {
            patterns.add( descriptorPattern );
            return this;
        }

        public Builder permitContentType( DescriptorPattern descriptorPattern )
        {
            contentTypePermits.add( descriptorPattern );
            return this;
        }

        public SiteComponentDescriptor build()
        {
            return new SiteComponentDescriptor( this );
        }
    }
}
