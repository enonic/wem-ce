package com.enonic.xp.xml.parser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.enonic.xp.app.ApplicationRelativeResolver;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.site.SiteDescriptor;
import com.enonic.xp.site.XDataMapping;
import com.enonic.xp.site.XDataMappings;
import com.enonic.xp.site.components.DescriptorPattern;
import com.enonic.xp.site.components.SiteComponentDescriptor;
import com.enonic.xp.site.components.SiteComponentDescriptors;
import com.enonic.xp.site.mapping.ControllerMappingDescriptor;
import com.enonic.xp.site.mapping.ControllerMappingDescriptors;
import com.enonic.xp.site.processor.ResponseProcessorDescriptor;
import com.enonic.xp.site.processor.ResponseProcessorDescriptors;
import com.enonic.xp.xml.DomElement;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

public final class XmlSiteParser
    extends XmlModelParser<XmlSiteParser>
{
    private static final String ROOT_TAG_NAME = "site";

    private static final String FORM_TAG_NAME = "form";

    private static final String X_DATA_TAG_NAME = "x-data";

    private static final String PROCESSOR_DESCRIPTORS_PARENT_TAG_NAME = "processors";

    private static final String PROCESSOR_DESCRIPTOR_TAG_NAME = "response-processor";

    private static final String MAPPINGS_DESCRIPTOR_TAG_NAME = "mappings";

    private static final String MAPPING_DESCRIPTOR_TAG_NAME = "mapping";

    private static final String X_DATA_ATTRIBUTE_NAME = "name";

    private static final String X_DATA_CONTENT_TYPE_ATTRIBUTE = "allowContentTypes";

    private static final String X_DATA_OPTIONAL_ATTRIBUTE = "optional";

    private static final String PROCESSOR_DESCRIPTOR_NAME_ATTRIBUTE = "name";

    private static final String PROCESSOR_DESCRIPTOR_ORDER_ATTRIBUTE = "order";

    private static final String MAPPING_DESCRIPTOR_CONTROLLER_ATTRIBUTE = "controller";

    private static final String MAPPING_DESCRIPTOR_FILTER_ATTRIBUTE = "filter";

    private static final String MAPPING_DESCRIPTOR_ORDER_ATTRIBUTE = "order";

    private static final String MAPPING_DESCRIPTOR_MATCH_TAG_NAME = "match";

    private static final String MAPPING_DESCRIPTOR_PATTERN_TAG_NAME = "pattern";

    private static final String MAPPING_DESCRIPTOR_INVERT_ATTRIBUTE = "invert";

    private SiteDescriptor.Builder siteDescriptorBuilder;

    public XmlSiteParser siteDescriptorBuilder( final SiteDescriptor.Builder siteDescriptorBuilder )
    {
        this.siteDescriptorBuilder = siteDescriptorBuilder;
        return this;
    }

    @Override
    protected void doParse( final DomElement root )
        throws Exception
    {
        assertTagName( root, ROOT_TAG_NAME );

        final XmlFormMapper formMapper = new XmlFormMapper( this.currentApplication );
        this.siteDescriptorBuilder.form( formMapper.buildForm( root.getChild( FORM_TAG_NAME ) ) );
        this.siteDescriptorBuilder.xDataMappings( XDataMappings.from( parseXDatas( root ) ) );
        this.siteDescriptorBuilder.responseProcessors(
            ResponseProcessorDescriptors.from( parseProcessorDescriptors( root.getChild( PROCESSOR_DESCRIPTORS_PARENT_TAG_NAME ) ) ) );
        this.siteDescriptorBuilder.mappingDescriptors(
            ControllerMappingDescriptors.from( parseMappingDescriptors( root.getChild( MAPPINGS_DESCRIPTOR_TAG_NAME ) ) ) );
        this.siteDescriptorBuilder.components( toSiteComponents( root.getChild( "components" ) ) );
    }

    private List<XDataMapping> parseXDatas( final DomElement root )
    {
        return root.getChildren( X_DATA_TAG_NAME ).stream().map( this::toXDataMapping ).collect( Collectors.toList() );
    }

    private List<ResponseProcessorDescriptor> parseProcessorDescriptors( final DomElement processorDescriptorsParent )
    {
        if ( processorDescriptorsParent != null )
        {
            return processorDescriptorsParent.getChildren( PROCESSOR_DESCRIPTOR_TAG_NAME ).stream().
                map( this::toProcessorDescriptor ).
                collect( Collectors.toList() );
        }
        return Collections.emptyList();
    }

    private List<ControllerMappingDescriptor> parseMappingDescriptors( final DomElement mappingDescriptorsParent )
    {
        if ( mappingDescriptorsParent != null )
        {
            return mappingDescriptorsParent.getChildren( MAPPING_DESCRIPTOR_TAG_NAME ).stream().
                map( this::toMappingDescriptor ).
                collect( Collectors.toList() );
        }
        return Collections.emptyList();
    }

    private SiteComponentDescriptors toSiteComponents( final DomElement domElement )
    {
        final SiteComponentDescriptors.Builder builder = SiteComponentDescriptors.create();
        if ( domElement != null )
        {
            domElement.getChildren( "part" ).stream().map( this::toSiteComponentDescriptor ).forEach( builder::addPartComponentDescriptor );
            domElement.getChildren( "layout" ).stream().map( this::toSiteComponentDescriptor ).forEach(
                builder::addLayoutComponentDescriptor );
        }
        return builder.build();
    }

    private SiteComponentDescriptor toSiteComponentDescriptor( final DomElement domElement )
    {
        final SiteComponentDescriptor.Builder builder = SiteComponentDescriptor.create();
        domElement.getChildren( "match" ).stream().map( this::toComponentPattern ).forEach( builder::addPattern );
        final DomElement contentTypePermits = domElement.getChild( "allowed-on-types" );
        if ( contentTypePermits != null )
        {
            contentTypePermits.getChildren( "content-type" ).stream().map( this::toComponentPattern ).forEach( builder::permitContentType );
        }
        return builder.build();
    }

    private DescriptorPattern toComponentPattern( final DomElement domElement )
    {
        final String matcherAttribute = domElement.getAttribute( "matcher", "relative" );
        final DescriptorPattern.ComponentMatcher matcher;
        switch ( matcherAttribute )
        {
            case "relative":
                matcher = DescriptorPattern.ComponentMatcher.RELATIVE;
                break;
            case "absolute":
                matcher = DescriptorPattern.ComponentMatcher.ABSOLUTE;
                break;
            case "legacy":
                matcher = DescriptorPattern.ComponentMatcher.LEGACY;
                break;
            default:
                throw new IllegalArgumentException( "Unknown matcher " + matcherAttribute );
        }
        return DescriptorPattern.create().matcher( matcher ).expression( domElement.getValue() ).build();
    }

    private XDataMapping toXDataMapping( final DomElement xDataElement )
    {
        final XDataMapping.Builder builder = XDataMapping.create();

        final ApplicationRelativeResolver resolver = new ApplicationRelativeResolver( this.currentApplication );

        final String name = xDataElement.getAttribute( X_DATA_ATTRIBUTE_NAME );
        builder.xDataName( resolver.toXDataName( name ) );

        final String allowContentTypes = xDataElement.getAttribute( X_DATA_CONTENT_TYPE_ATTRIBUTE );
        builder.allowContentTypes( allowContentTypes );

        final String optional = xDataElement.getAttribute( X_DATA_OPTIONAL_ATTRIBUTE );
        if ( optional != null )
        {
            builder.optional( Boolean.valueOf( optional ) );
        }

        return builder.build();
    }

    private ResponseProcessorDescriptor toProcessorDescriptor( final DomElement processorElement )
    {
        final ResponseProcessorDescriptor.Builder builder = ResponseProcessorDescriptor.create();
        final String orderValue = processorElement.getAttribute( PROCESSOR_DESCRIPTOR_ORDER_ATTRIBUTE );
        if ( !isNullOrEmpty( orderValue ) )
        {
            builder.order( Integer.parseInt( orderValue ) );
        }
        builder.name( processorElement.getAttribute( PROCESSOR_DESCRIPTOR_NAME_ATTRIBUTE ) );
        builder.application( this.currentApplication );
        return builder.build();
    }

    private ControllerMappingDescriptor toMappingDescriptor( final DomElement mappingElement )
    {
        final ControllerMappingDescriptor.Builder builder = ControllerMappingDescriptor.create();
        final String controllerPath = mappingElement.getAttribute( MAPPING_DESCRIPTOR_CONTROLLER_ATTRIBUTE );
        if ( !nullToEmpty( controllerPath ).isBlank() )
        {
            builder.controller( ResourceKey.from( this.currentApplication, controllerPath ) );
        }

        final String filterPath = mappingElement.getAttribute( MAPPING_DESCRIPTOR_FILTER_ATTRIBUTE );
        if ( !nullToEmpty( filterPath ).isBlank() )
        {
            builder.filter( ResourceKey.from( this.currentApplication, filterPath ) );
        }

        final String orderValue = mappingElement.getAttribute( MAPPING_DESCRIPTOR_ORDER_ATTRIBUTE );
        if ( !isNullOrEmpty( orderValue ) )
        {
            builder.order( Integer.parseInt( orderValue ) );
        }

        final DomElement matchElement = mappingElement.getChild( MAPPING_DESCRIPTOR_MATCH_TAG_NAME );
        if ( matchElement != null )
        {
            final String match = matchElement.getValue();
            if ( !isNullOrEmpty( match ) )
            {
                builder.contentConstraint( match );
            }
        }

        final DomElement patternElement = mappingElement.getChild( MAPPING_DESCRIPTOR_PATTERN_TAG_NAME );
        if ( patternElement != null )
        {
            final String pattern = patternElement.getValue();
            if ( !isNullOrEmpty( pattern ) )
            {
                final boolean invert = "true".equals( patternElement.getAttribute( MAPPING_DESCRIPTOR_INVERT_ATTRIBUTE, "false" ) );
                builder.pattern( pattern );
                builder.invertPattern( invert );
            }
        }

        return builder.build();
    }
}
