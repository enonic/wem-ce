package com.enonic.xp.script.impl.util;

import java.util.Date;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.graalvm.polyglot.Value;

public final class NashornHelper
{

    public static ScriptEngine getScriptEngine( final ClassLoader loader )
    {
        return new ScriptEngineManager().getEngineByName("graal.js");
    }

    public static boolean isUndefined( final Object value )
    {
        return false;
        //return value == null ||  ( (Value) value ).isNull();
    }

    static boolean isNativeArray( final Object value )
    {
        return ( value instanceof Value) && ( (Value) value ).hasArrayElements();
    }

    static boolean isNativeObject( final Object value )
    {
        return ( value instanceof Value ) && !isNativeArray( value );
    }

    static void addToNativeObject( final Object object, final String key, final Object value )
    {
        ( (Value) object ).putMember( key, value );
    }

    static void addToNativeArray( final Object array, final Object value )
    {
        ( (Value) array ).invokeMember( "push", value );
    }

    public static boolean isDateType( final Value value )
    {
        return value.canInvokeMember("getTime" );
    }

    public static Date toDate( final Value value )
    {
        final long time = value.invokeMember( "getTime" ).asLong();
        return new Date( time );
    }
}
