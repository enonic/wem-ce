package com.enonic.xp.core.impl.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.descriptor.DescriptorKeyLocator;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.service.ServiceDescriptor;
import com.enonic.xp.service.ServiceDescriptorService;
import com.enonic.xp.service.ServiceDescriptors;
import com.enonic.xp.xml.XmlException;
import com.enonic.xp.xml.parser.XmlServiceDescriptorParser;

@Component(immediate = true)
public final class ServiceDescriptorServiceImpl
    implements ServiceDescriptorService
{
    private static final Logger LOG = LoggerFactory.getLogger( ServiceDescriptorServiceImpl.class );

    private static final String PATH = "/services";

    private ResourceService resourceService;

    @Override
    public ServiceDescriptor getByKey( final DescriptorKey descriptorKey )
    {
        final ResourceProcessor<DescriptorKey, ServiceDescriptor> processor = newRootProcessor( descriptorKey );
        final ServiceDescriptor descriptor = this.resourceService.processResource( processor );

        if ( descriptor != null )
        {
            return descriptor;
        }

        return createDefaultDescriptor( descriptorKey );
    }

    @Override
    public ServiceDescriptors getByApplication( final ApplicationKey key )
    {
        final List<ServiceDescriptor> list = new ArrayList<>();
        for ( final DescriptorKey descriptorKey : findDescriptorKeys( key ) )
        {
            try
            {
                final ServiceDescriptor descriptor = getByKey( descriptorKey );
                if ( descriptor != null )
                {
                    list.add( descriptor );
                }
            }
            catch ( final IllegalArgumentException e )
            {
                LOG.error( "Error in page descriptor: " + descriptorKey.toString(), e );
            }
        }

        return ServiceDescriptors.from( list );
    }

    private ResourceProcessor<DescriptorKey, ServiceDescriptor> newRootProcessor( final DescriptorKey key )
    {
        return new ResourceProcessor.Builder<DescriptorKey, ServiceDescriptor>().
            key( key ).
            segment( "rootServiceDescriptor" ).
            keyTranslator( ServiceDescriptor::toRootResourceKey ).
            processor( resource -> loadDescriptor( key, resource ) ).
            build();
    }

    private ServiceDescriptor loadDescriptor( final DescriptorKey key, final Resource resource )
    {
        final ServiceDescriptor.Builder builder = ServiceDescriptor.create();
        parseXml( resource, builder );
        builder.key( key );

        return builder.build();
    }

    private void parseXml( final Resource resource, final ServiceDescriptor.Builder builder )
    {
        try
        {
            new XmlServiceDescriptorParser().
                builder( builder ).
                currentApplication( resource.getKey().getApplicationKey() ).
                source( resource.readString() ).
                parse();
        }
        catch ( final Exception e )
        {
            throw new XmlException( e, "Could not load service descriptor [" + resource.getUrl() + "]: " + e.getMessage() );
        }
    }

    private ServiceDescriptor createDefaultDescriptor( final DescriptorKey descriptorKey )
    {
        return ServiceDescriptor.create().
            key( descriptorKey ).
            build();
    }

    private Iterable<DescriptorKey> findDescriptorKeys( final ApplicationKey key )
    {
        return new DescriptorKeyLocator( this.resourceService, PATH, true ).findKeys( key );
    }

    @Reference
    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }
}
