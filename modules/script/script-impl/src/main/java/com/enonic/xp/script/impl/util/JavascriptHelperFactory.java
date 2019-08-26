package com.enonic.xp.script.impl.util;

import javax.script.ScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.HashMap;

public final class JavascriptHelperFactory
{
    private final ScriptEngine engine;

    public JavascriptHelperFactory( final ScriptEngine engine )
    {
        this.engine = engine;
    }

    public JavascriptHelper create()
    {
        final Context context = Context.create();

        return new JavascriptHelper()
        {
            @Override
            public Value newJsArray()
            {
                return Value.asValue(ProxyArray.fromList(new ArrayList<>()));
            }

            @Override
            public Value newJsObject()
            {
                return Value.asValue(ProxyObject.fromMap(new HashMap<>()));
            }

            @Override
            public Value parseJson( final String text )
            {
                    return  context.eval("js","JSON.parse").execute( text );
            }
        };
    }
}
