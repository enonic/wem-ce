package com.enonic.xp.xml.parser;

import org.w3c.dom.Attr;

import com.google.common.base.CaseFormat;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationRelativeResolver;
import com.enonic.xp.inputtype.InputTypeConfig;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.inputtype.InputTypeProperty;
import com.enonic.xp.xml.DomElement;

import static com.google.common.base.Strings.isNullOrEmpty;

final class XmlInputTypeConfigMapper
{
    private final ApplicationRelativeResolver relativeResolver;

    private final InputTypeName inputTypeName;

    public XmlInputTypeConfigMapper( final ApplicationKey currentApplication, final InputTypeName inputTypeName )
    {
        this.relativeResolver = new ApplicationRelativeResolver( currentApplication );
        this.inputTypeName = inputTypeName;
    }

    public InputTypeConfig build( final DomElement root )
    {
        final InputTypeConfig.Builder builder = InputTypeConfig.create();

        if ( root != null )
        {
            build( builder, root );
        }

        return builder.build();
    }

    private void build( final InputTypeConfig.Builder builder, final DomElement root )
    {
        for ( final DomElement child : root.getChildren() )
        {
            builder.property( buildProperty( child ) );
        }
    }

    private InputTypeProperty buildProperty( final DomElement root )
    {
        final String name = resolveName( root.getTagName() );
        final String value = resolveValue( name, root.getValue() );

        final InputTypeProperty.Builder builder = InputTypeProperty.create( name, value );
        for ( final Attr attr : root.getAttributes() )
        {
            addPropertyAttribute( builder, attr );
        }

        return builder.build();
    }

    private void addPropertyAttribute( final InputTypeProperty.Builder builder, final Attr attr )
    {
        final String name = resolveName( attr.getName() );
        final String value = resolveValue( name, attr.getValue() );
        builder.attribute( name, value );
    }

    private String resolveName( final String name )
    {
        final String result = InputTypeAliasConverters.convert( this.inputTypeName, name );

        if ( result.contains( "-" ) )
        {
            return CaseFormat.LOWER_HYPHEN.to( CaseFormat.LOWER_CAMEL, result );
        }

        return result;
    }

    private String resolveValue( final String name, final String value )
    {
        if ( isNullOrEmpty( value ) )
        {
            return null;
        }

        final String lowerCasedName = name.toLowerCase();
        if ( lowerCasedName.endsWith( "service" ) )
        {
            return this.relativeResolver.toServiceUrl( value );
        }
        if ( lowerCasedName.endsWith( "contenttype" ) )
        {
            return this.relativeResolver.toContentTypeName( value ).toString();
        }
        else if ( lowerCasedName.endsWith( "mixintype" ) )
        {
            return this.relativeResolver.toMixinName( value ).toString();
        }
        else if ( lowerCasedName.endsWith( "relationshiptype" ) )
        {
            return this.relativeResolver.toRelationshipTypeName( value ).toString();
        }
        else
        {
            return value;
        }
    }
}
