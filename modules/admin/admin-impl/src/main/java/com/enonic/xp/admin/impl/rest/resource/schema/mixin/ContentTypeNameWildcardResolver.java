package com.enonic.xp.admin.impl.rest.resource.schema.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationWildcardResolver;
import com.enonic.xp.schema.content.ContentTypeService;

public class ContentTypeNameWildcardResolver
{
    private final ContentTypeService contentTypeService;

    private static final ApplicationWildcardResolver APPLICATION_WILDCARD_RESOLVER = new ApplicationWildcardResolver();

    public ContentTypeNameWildcardResolver( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
    }

    private List<String> resolveContentTypeName( final Predicate<String> pattern )
    {
        return contentTypeService.
            getAll().
            stream().
            map( type -> type.getName().toString() ).
            filter( pattern ).
            collect( Collectors.toList() );
    }

    public List<String> resolveWildcards( final List<String> namesToResolve, final ApplicationKey currentApplicationKey )
    {
        final List<String> resolvedNames = new ArrayList<>();

        namesToResolve.forEach( name -> {
            final String resolvedName =
                APPLICATION_WILDCARD_RESOLVER.startWithAppWildcard( name )
                    ? APPLICATION_WILDCARD_RESOLVER.resolveAppWildcard( name, currentApplicationKey )
                    : name;

            if ( APPLICATION_WILDCARD_RESOLVER.hasAnyWildcard( resolvedName ) )
            {
                resolvedNames.addAll( resolveContentTypeName( this.resolveAnyWildcard( resolvedName ) ) );
            }
            else
            {
                resolvedNames.add( resolvedName );
            }
        } );

        return resolvedNames;
    }

    private Predicate<String> resolveAnyWildcard( final String nameToResolve )
    {
        return Pattern.compile( nameToResolve.replace( "*", ".*" ) ).asPredicate();
    }
}
