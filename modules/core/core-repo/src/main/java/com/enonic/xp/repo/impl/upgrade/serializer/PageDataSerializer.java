package com.enonic.xp.repo.impl.upgrade.serializer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import com.enonic.xp.content.ContentService;
import com.enonic.xp.data.Property;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.page.Page;
import com.enonic.xp.page.PageDescriptor;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.page.PageRegions;
import com.enonic.xp.page.PageTemplateKey;
import com.enonic.xp.region.Component;
import com.enonic.xp.region.ComponentPath;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.region.Region;
import com.enonic.xp.region.RegionDescriptors;
import com.enonic.xp.util.Reference;

import static com.enonic.xp.content.ContentPropertyNames.PAGE;
import static com.enonic.xp.repo.impl.upgrade.serializer.ComponentDataSerializer.COMPONENTS;
import static com.enonic.xp.repo.impl.upgrade.serializer.ComponentDataSerializer.PATH;
import static com.enonic.xp.repo.impl.upgrade.serializer.ComponentDataSerializer.TYPE;
import static com.enonic.xp.repo.impl.upgrade.serializer.DescriptorBasedComponentDataSerializer.CONFIG;
import static com.enonic.xp.repo.impl.upgrade.serializer.DescriptorBasedComponentDataSerializer.DESCRIPTOR;
import static com.enonic.xp.repo.impl.upgrade.serializer.DescriptorBasedComponentDataSerializer.appNameToConfigPropertyName;
import static com.enonic.xp.repo.impl.upgrade.serializer.DescriptorBasedComponentDataSerializer.getConfigFromData;

final class PageDataSerializer
    extends AbstractDataSetSerializer<Page>
{
    private static final String TEMPLATE = "template";

    private static final String CUSTOMIZED = "customized";

    private final ComponentDataSerializerProvider componentDataSerializerProvider;

    private final PageDescriptorService pageDescriptorService;

    private PageDataSerializer( final Builder builder )
    {
        this.pageDescriptorService = builder.pageDescriptorService;

        this.componentDataSerializerProvider = ComponentDataSerializerProvider.create().
            contentService( builder.contentService ).
            layoutDescriptorService( builder.layoutDescriptorService ).
            partDescriptorService( builder.partDescriptorService ).
            build();
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public void toData( final Page page, final PropertySet parent )
    {
        if ( page.isFragment() )
        {
            serializeFragment( page, parent );
        }
        else
        {
            serializePage( page, parent );
        }
    }

    private void serializeFragment( final Page page, final PropertySet parent )
    {
        final Component fragment = page.getFragment();
        componentDataSerializerProvider.getDataSerializer( fragment.getType() ).toData( fragment, parent );
    }

    private void serializePage( final Page page, final PropertySet parent )
    {
        final PropertySet asSet = parent.addSet( COMPONENTS );

        asSet.setString( TYPE, PAGE );
        asSet.setString( PATH, ComponentPath.DIVIDER );

        final PropertySet specialBlockSet = asSet.addSet( PAGE );

        if ( page.hasDescriptor() )
        {
            specialBlockSet.addString( DESCRIPTOR, page.getDescriptor().toString() );
        }

        if ( page.hasTemplate() )
        {
            specialBlockSet.addReference( TEMPLATE, Reference.from( page.getTemplate().toString() ) );
        }

        if ( page.hasRegions() )
        {
            addRegions( page, parent );
        }

        specialBlockSet.addBoolean( CUSTOMIZED, page.isCustomized() );

        if ( page.hasConfig() )
        {
            final String pageName = appNameToConfigPropertyName( page.getDescriptor().getName() );
            final String appKeyAsString = appNameToConfigPropertyName( page.getDescriptor().getApplicationKey().toString() );
            final PropertySet configSet = specialBlockSet.addSet( CONFIG ).addSet( appKeyAsString );
            configSet.addSet( pageName, page.getConfig().getRoot().copy( asSet.getTree() ) );
        }
    }

    private void addRegions( final Page page, final PropertySet asSet )
    {
        if ( !page.getRegions().isEmpty() )
        {
            for ( Region region : page.getRegions() )
            {
                componentDataSerializerProvider.getRegionDataSerializer().toData( region, asSet );
            }
        }
    }

    @Override
    public Page fromData( final PropertySet asSet )
    {
        final List<PropertySet> componentsAsData = asSet.getProperties( COMPONENTS ).stream().filter( Property::hasNotNullValue ).
            map( Property::getSet ).collect( Collectors.toList() );

        if ( componentsAsData.size() == 0 )
        {
            return null;
        }

        if ( !isRootComponent( componentsAsData.get( 0 ) ) )
        {
            componentsAsData.sort( Comparator.comparing( this::getComponentPath ) );
        }

        return fromData( componentsAsData );
    }

    private boolean isRootComponent( final PropertySet componentData )
    {
        return getComponentPath( componentData ).equals( ComponentPath.DIVIDER );
    }

    private String getComponentPath( final PropertySet componentData )
    {
        return componentData.getString( PATH );
    }

    private Page fromData( final List<PropertySet> componentsAsData )
    {
        final PropertySet pageData = componentsAsData.get( 0 );
        componentsAsData.remove( 0 );

        return fromData( pageData, componentsAsData );
    }

    private Page fromData( final PropertySet pageData, final List<PropertySet> componentsAsData )
    {
        final boolean isFragment = !pageData.getString( TYPE ).equals( PAGE );

        if ( isFragment )
        {
            return fromFragmentData( pageData, componentsAsData );
        }

        return fromPageData( pageData, componentsAsData );
    }

    private Page fromFragmentData( final PropertySet fragmentData, final List<PropertySet> componentsAsData )
    {
        final Page.Builder page = Page.create();

        page.fragment( componentDataSerializerProvider.getRegionDataSerializer().getComponent( fragmentData, componentsAsData ) );

        return page.build();
    }

    private Page fromPageData( final PropertySet pageData, final List<PropertySet> componentsAsData )
    {
        final Page.Builder page = Page.create();

        final PropertySet specialBlockSet = pageData.getSet( PAGE );

        if ( specialBlockSet != null )
        {
            if ( specialBlockSet.isNotNull( DESCRIPTOR ) )
            {
                final DescriptorKey descriptorKey = DescriptorKey.from( specialBlockSet.getString( DESCRIPTOR ) );
                page.descriptor( descriptorKey );
                page.config( getConfigFromData( specialBlockSet, descriptorKey ) );
                page.regions( getPageRegions( descriptorKey, componentsAsData ) );
            }

            if ( specialBlockSet.isNotNull( TEMPLATE ) )
            {
                page.template( PageTemplateKey.from( specialBlockSet.getReference( TEMPLATE ).toString() ) );
            }

            if ( specialBlockSet.isNotNull( CUSTOMIZED ) )
            {
                page.customized( specialBlockSet.getBoolean( CUSTOMIZED ) );
            }
        }

        return page.build();
    }

    private PageRegions getPageRegions( final DescriptorKey descriptorKey, final List<PropertySet> componentsAsData )
    {
        final PageDescriptor pageDescriptor = pageDescriptorService.getByKey( descriptorKey );

        final RegionDescriptors regionDescriptors = pageDescriptor.getRegions();

        if ( regionDescriptors.numberOfRegions() == 0 )
        {
            return null;
        }

        final PageRegions.Builder pageRegionsBuilder = PageRegions.create();

        regionDescriptors.forEach( regionDescriptor -> {
            pageRegionsBuilder.add(
                componentDataSerializerProvider.getRegionDataSerializer().fromData( regionDescriptor, ComponentPath.DIVIDER,
                                                                                    componentsAsData ) );
        } );

        return pageRegionsBuilder.build();
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

        public PageDataSerializer build()
        {
            validate();
            return new PageDataSerializer( this );
        }
    }
}
