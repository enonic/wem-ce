package com.enonic.xp.xml.parser;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.site.SiteDescriptor;
import com.enonic.xp.site.mapping.ControllerMappingDescriptor;
import com.enonic.xp.support.ResourceTestHelper;
import com.enonic.xp.support.XmlTestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XmlSiteParserTest
{
    private final XmlTestHelper xmlTestHelper;

    private final XmlSiteParser parser;

    public XmlSiteParserTest()
    {
        this.xmlTestHelper = new XmlTestHelper( this );
        this.parser = new XmlSiteParser();
    }

    private String loadTestXml( final String fileName )
    {
        return this.xmlTestHelper.loadTestXml( fileName );
    }

    @Test
    public void testSiteXmlDeserialization()
    {
        final String xml = loadTestXml( "serialized-site.xml" );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 1, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 2, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 2, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 0, siteDescriptor.getMappingDescriptors().getSize() );
        assertEquals( 4, siteDescriptor.getSiteComponents().getPartSiteDescriptors().size() );
    }

    @Test
    public void testEmptySiteXmlDeserialization()
    {
        final String xml = loadTestXml( "empty-site.xml" );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 0, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 0, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 0, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 0, siteDescriptor.getMappingDescriptors().getSize() );
    }

    @Test
    public void testSiteXmlDeserializationWithMappings()
    {
        final String xml = loadTestXml( "serialized-site-with-mappings.xml" );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 1, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 2, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 2, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 3, siteDescriptor.getMappingDescriptors().getSize() );

        final ControllerMappingDescriptor mapping1 = siteDescriptor.getMappingDescriptors().get( 0 );
        final ControllerMappingDescriptor mapping2 = siteDescriptor.getMappingDescriptors().get( 1 );
        final ControllerMappingDescriptor mapping3 = siteDescriptor.getMappingDescriptors().get( 2 );
        assertEquals( "myapplication:/site/page/person/person.js", mapping1.getController().toString() );
        assertNull( mapping1.getContentConstraint() );
        assertEquals( "/person/*", mapping1.getPattern().toString() );
        assertEquals( 10, mapping1.getOrder() );

        assertEquals( "myapplication:/controller1.js", mapping2.getController().toString() );
        assertEquals( "_path:'/*/fisk'", mapping2.getContentConstraint().toString() );
        assertEquals( "/.*", mapping2.getPattern().toString() );
        assertEquals( 50, mapping2.getOrder() );

        assertEquals( "myapplication:/controller2.js", mapping3.getController().toString() );
        assertEquals( "type:'portal:fragment'", mapping3.getContentConstraint().toString() );
        assertEquals( "/.*", mapping3.getPattern().toString() );
        assertEquals( 5, mapping3.getOrder() );
    }

    @Test
    public void testSiteXmlDeserializationWithMappingFilters()
    {
        final String xml = loadTestXml( "serialized-site-with-mapping-filters.xml" );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 2, siteDescriptor.getMappingDescriptors().getSize() );

        final ControllerMappingDescriptor mapping1 = siteDescriptor.getMappingDescriptors().get( 0 );
        final ControllerMappingDescriptor mapping2 = siteDescriptor.getMappingDescriptors().get( 1 );
        assertEquals( "myapplication:/filter1.js", mapping1.getFilter().toString() );
        assertEquals( "_path:'/*/fisk'", mapping1.getContentConstraint().toString() );
        assertEquals( "/.*", mapping1.getPattern().toString() );
        assertEquals( 50, mapping1.getOrder() );

        assertEquals( "myapplication:/filter2.js", mapping2.getFilter().toString() );
        assertEquals( "type:'portal:fragment'", mapping2.getContentConstraint().toString() );
        assertEquals( "/.*", mapping2.getPattern().toString() );
        assertEquals( 5, mapping2.getOrder() );
    }

    @Test
    public void testSiteXmlWithUtf8BomEncoding()
    {
        final String xml = loadTestFile( "utf8bom.xml", StandardCharsets.UTF_8 );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 1, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 2, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 2, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 0, siteDescriptor.getMappingDescriptors().getSize() );
    }

    @Test
    public void testSiteXmlWithUtf16LeBomEncoding()
    {
        final String xml = loadTestFile( "utf16lebom.xml", StandardCharsets.UTF_16LE );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 1, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 2, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 2, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 0, siteDescriptor.getMappingDescriptors().getSize() );
    }

    @Test
    public void testSiteXmlWithUtf16BeBomEncoding()
    {
        final String xml = loadTestFile( "utf16bebom.xml", StandardCharsets.UTF_16BE );

        final SiteDescriptor.Builder siteDescriptorBuilder = SiteDescriptor.create();
        ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );

        this.parser.source( xml ).
            currentApplication( applicationKey ).
            siteDescriptorBuilder( siteDescriptorBuilder ).
            parse();

        SiteDescriptor siteDescriptor = siteDescriptorBuilder.build();

        assertEquals( 1, siteDescriptor.getForm().getFormItems().size() );
        assertEquals( 2, siteDescriptor.getXDataMappings().getSize() );
        assertEquals( 2, siteDescriptor.getResponseProcessors().getSize() );
        assertEquals( 0, siteDescriptor.getMappingDescriptors().getSize() );
    }

    private String loadTestFile( final String fileName, Charset charset )
    {
        final URL url = new ResourceTestHelper( this ).getTestResource( fileName );
        try (InputStream stream = url.openStream())
        {
            return new String( stream.readAllBytes(), charset );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( "Failed to load test file: " + url, e );
        }
    }
}
