package com.enonic.xp.script.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.graalvm.polyglot.Value;

import com.enonic.xp.script.serializer.MapSerializable;

public final class JsObjectConverter
{
    private final JavascriptHelper helper;

    public JsObjectConverter( final JavascriptHelper helper )
    {
        this.helper = helper;
    }

    public Object toJs( final Object value )
    {
        if ( value instanceof MapSerializable )
        {
            return toJs( (MapSerializable) value );
        }

        if ( value instanceof List )
        {
            return toJs( (List) value );
        }

        return value;
    }

    public Object[] toJsArray( final Object[] values )
    {
        final Object[] result = new Object[values.length];
        for ( int i = 0; i < values.length; i++ )
        {
            result[i] = toJs( values[i] );
        }

        return result;
    }

    private Object toJs( final MapSerializable value )
    {
        final ScriptMapGenerator generator = new ScriptMapGenerator( this.helper );
        value.serialize( generator );
        return generator.getRoot();
    }

    private Object toJs( final List list )
    {
        final Object array = this.helper.newJsArray();
        for ( final Object element : list )
        {
            NashornHelper.addToNativeArray( array, toJs( element ) );
        }

        return array;
    }

    public Object fromJs( final Object value )
    {
        return toObject( value );
    }

    private Object toObject( final Object source )
    {
        if ( source instanceof Value )
        {
            return toObject( (Value) source );
        }

        return source;
    }

    private Object toObject( final Value source )
    {
        if ( source.hasArrayElements() )
        {
            return toList( source );
        }
        else if ( source.canExecute() )
        {
            return toFunction( source );
        }
        else if ( NashornHelper.isDateType( source ) )
        {
            return NashornHelper.toDate( source );
        }
        else
        {
            return toMap( source );
        }
    }

    private List<Object> toList( final Value source )
    {
        final List<Object> result = new ArrayList<>();
        for ( long itemIdx = 0; itemIdx < source.getArraySize(); itemIdx++ )
        {
            var item = source.getArrayElement(itemIdx);

            final Object converted = toObject( item );
            if ( converted != null )
            {
                result.add( converted );
            }
        }

        return result;
    }

    public Map<String, Object> toMap( final Object source )
    {
        if ( source instanceof Value )
        {
            return toMap( (Value) source );
        }

        return new HashMap<>();
    }

    private Map<String, Object> toMap( final Value source )
    {
        final Map<String, Object> result = new LinkedHashMap<>();
        for ( final String entryKey : source.getMemberKeys() )
        {
            final Object converted = toObject( source.getMember(entryKey) );
            if ( converted != null )
            {
                result.put( entryKey, converted );
            }
        }

        return result;
    }

    private Function<Object[], Object> toFunction( final Value source )
    {
        return arg -> toObject( source.execute( arg ) );
    }
}
