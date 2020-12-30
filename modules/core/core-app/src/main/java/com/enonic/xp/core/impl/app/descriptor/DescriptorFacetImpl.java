package com.enonic.xp.core.impl.app.descriptor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.core.impl.app.ApplicationRegistry;
import com.enonic.xp.descriptor.Descriptor;
import com.enonic.xp.descriptor.DescriptorKeys;
import com.enonic.xp.descriptor.DescriptorLoader;
import com.enonic.xp.descriptor.Descriptors;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.resource.ResourceService;

final class DescriptorFacetImpl<T extends Descriptor>
    implements DescriptorFacet<T>
{
    private static final Logger LOG = LoggerFactory.getLogger( DescriptorFacetImpl.class );

    private final Class<T> type;

    final DescriptorLoader<T> loader;

    ApplicationRegistry applicationRegistry;

    ResourceService resourceService;

    DescriptorFacetImpl( final DescriptorLoader<T> loader )
    {
        this.loader = loader;
        this.type = this.loader.getType();
    }

    @Override
    public T get( final DescriptorKey key )
    {
        final ResourceProcessor<DescriptorKey, T> processor = newProcessor( key );
        final T descriptor = this.resourceService.processResource( processor );

        if ( descriptor == null )
        {
            return this.loader.createDefault( key );
        }

        return this.loader.postProcess( descriptor );
    }

    @Override
    public Descriptors<T> get( final DescriptorKeys keys )
    {
        final List<T> list = new ArrayList<>();
        for ( final DescriptorKey key : keys )
        {
            final T descriptor = get( key );
            if ( descriptor != null )
            {
                list.add( descriptor );
            }
        }

        return Descriptors.from( list );
    }

    @Override
    public Descriptors<T> getAll()
    {
        return get( this.applicationRegistry.getKeys() );
    }

    @Override
    public DescriptorKeys findAll()
    {
        return find( this.applicationRegistry.getKeys() );
    }

    @Override
    public Descriptors<T> get( final ApplicationKeys keys )
    {
        return get( find( keys ) );
    }

    @Override
    public DescriptorKeys find( final ApplicationKeys keys )
    {
        DescriptorKeys all = DescriptorKeys.empty();
        for ( final ApplicationKey key : keys )
        {
            final DescriptorKeys result = this.loader.find( key );
            all = all.concat( result );
        }

        return all;
    }

    private ResourceProcessor<DescriptorKey, T> newProcessor( final DescriptorKey key )
    {
        return new ResourceProcessor.Builder<DescriptorKey, T>().
            key( key ).
            segment( this.type.getName() ).
            keyTranslator( this.loader::toResource ).
            processor( resource -> doLoad( key, resource ) ).
            build();
    }

    private T doLoad( final DescriptorKey key, final Resource resource )
    {
        try
        {
            return this.loader.load( key, resource );
        }
        catch ( final Exception e )
        {
            LOG.error( "Error loading descriptor [" + this.type.getSimpleName() + "]", e );
            return null;
        }
    }
}
