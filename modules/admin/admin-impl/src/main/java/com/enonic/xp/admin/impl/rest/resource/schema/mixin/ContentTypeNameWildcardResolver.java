package com.enonic.xp.admin.impl.rest.resource.schema.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationWildcardResolver;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.page.DescriptorKeyMatcher;
import com.enonic.xp.schema.content.ContentTypeService;
import com.enonic.xp.schema.content.ContentTypes;

public class ContentTypeNameWildcardResolver
{
    private final ContentTypeService contentTypeService;

    private final ApplicationWildcardResolver applicationWildcardResolver;

    public ContentTypeNameWildcardResolver( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
        this.applicationWildcardResolver = new ApplicationWildcardResolver();
    }

    public List<String> resolveWildcards( final List<String> namesToResolve, final ApplicationKey currentApplicationKey )
    {
        final List<String> resolvedNames = new ArrayList<>();

        namesToResolve.forEach( name -> {
            if ( this.applicationWildcardResolver.hasAnyWildcard( name ) )
            {
                final DescriptorKeyMatcher descriptorKeyMatcher = DescriptorKeyMatcher.legacy( currentApplicationKey, name );
                final ContentTypes allContentTypes = contentTypeService.getAll();

                // ContentTypeName is not a DescriptorKey, but behaves equally
                resolvedNames.addAll( allContentTypes.
                    stream().
                    map( type -> type.getName().toString() ).
                    filter( contentTypeName -> descriptorKeyMatcher.matches( DescriptorKey.from( contentTypeName ) ) ).
                    collect( Collectors.toList() ) );
            }
            else
            {
                resolvedNames.add( this.applicationWildcardResolver.resolveAppWildcard( name, currentApplicationKey ) );
            }
        } );

        return resolvedNames;
    }
}
