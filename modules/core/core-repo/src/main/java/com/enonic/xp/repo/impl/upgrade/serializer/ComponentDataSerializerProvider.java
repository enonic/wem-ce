package com.enonic.xp.repo.impl.upgrade.serializer;

import com.google.common.base.Preconditions;

import com.enonic.xp.content.ContentService;
import com.enonic.xp.region.ComponentType;
import com.enonic.xp.region.FragmentComponentType;
import com.enonic.xp.region.ImageComponentType;
import com.enonic.xp.region.LayoutComponentType;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartComponentType;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.region.TextComponentType;

public final class ComponentDataSerializerProvider
{
    private final PartComponentDataSerializer partDataSerializer;

    private final TextComponentDataSerializer textDataSerializer;

    private final LayoutComponentDataSerializer layoutDataSerializer;

    private final ImageComponentDataSerializer imageDataSerializer;

    private final FragmentComponentDataSerializer fragmentDataSerializer;

    private final RegionDataSerializer regionDataSerializer;

    private ComponentDataSerializerProvider( final Builder builder )
    {
        this.regionDataSerializer = new RegionDataSerializer( this );
        this.partDataSerializer = new PartComponentDataSerializer();
        this.textDataSerializer = new TextComponentDataSerializer();
        this.layoutDataSerializer = new LayoutComponentDataSerializer( builder.layoutDescriptorService, this.regionDataSerializer );
        this.imageDataSerializer = new ImageComponentDataSerializer();
        this.fragmentDataSerializer = new FragmentComponentDataSerializer();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public ComponentDataSerializer getDataSerializer( final ComponentType componentType )
    {
        if ( componentType instanceof PartComponentType )
        {
            return partDataSerializer;
        }
        if ( componentType instanceof LayoutComponentType )
        {
            return layoutDataSerializer;
        }
        if ( componentType instanceof TextComponentType )
        {
            return textDataSerializer;
        }
        if ( componentType instanceof ImageComponentType )
        {
            return imageDataSerializer;
        }
        if ( componentType instanceof FragmentComponentType )
        {
            return fragmentDataSerializer;
        }
        else
        {
            return null;
        }
    }

    public RegionDataSerializer getRegionDataSerializer()
    {
        return regionDataSerializer;
    }

    public static class Builder
    {
        private PartDescriptorService partDescriptorService;

        private LayoutDescriptorService layoutDescriptorService;

        private ContentService contentService;

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
            Preconditions.checkNotNull( partDescriptorService );
            Preconditions.checkNotNull( layoutDescriptorService );
            Preconditions.checkNotNull( contentService );
        }

        public ComponentDataSerializerProvider build()
        {
            validate();
            return new ComponentDataSerializerProvider( this );
        }
    }

}
