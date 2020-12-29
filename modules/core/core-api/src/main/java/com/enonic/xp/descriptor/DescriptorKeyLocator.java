package com.enonic.xp.descriptor;

import java.util.Set;
import java.util.stream.Collectors;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;

public final class DescriptorKeyLocator
{
    private final ResourceService service;

    private final String path;

    private final boolean optional;

    public DescriptorKeyLocator( final ResourceService service, final String path, final boolean optional )
    {
        this.service = service;
        this.path = path;
        this.optional = optional;
    }

    public Set<DescriptorKey> findKeys( final ApplicationKey key )
    {
        return this.service.findFiles( key ).
            map( resource -> newDescriptorKey( resource ) ).
            filter( dk -> dk != null ).
            collect( Collectors.toSet() );
    }

    private DescriptorKey newDescriptorKey( final ResourceKey key )
    {
        final String extension = key.getExtension();

        if ( "xml".equals( extension ) || ( optional && "js".equals( extension ) ) )
        {
            final String nameWithExt = key.getName();
            final String nameWithoutExt = nameWithExt.substring( 0, nameWithExt.length() - ( extension.length() + 1 ) );

            if ( key.getPath().equals( this.path + "/" + nameWithoutExt + "/" + nameWithExt ) )
            {
                return DescriptorKey.from( key.getApplicationKey(), nameWithoutExt );
            }
        }
        return null;
    }
}
