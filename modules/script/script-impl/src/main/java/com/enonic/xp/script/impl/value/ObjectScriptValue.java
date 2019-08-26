package com.enonic.xp.script.impl.value;

import java.util.Map;
import java.util.Set;

import org.graalvm.polyglot.Value;

import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.util.JsObjectConverter;

final class ObjectScriptValue
    extends AbstractScriptValue
{
    private final ScriptValueFactory factory;

    private final Value value;

    ObjectScriptValue( final ScriptValueFactory factory, final Value value )
    {
        this.factory = factory;
        this.value = value;
    }

    @Override
    public boolean isObject()
    {
        return true;
    }

    @Override
    public Set<String> getKeys()
    {
        return this.value.getMemberKeys();
    }

    @Override
    public boolean hasMember( final String key )
    {
        return this.value.hasMember( key );
    }

    @Override
    public ScriptValue getMember( final String key )
    {
        return this.factory.newValue( this.value.getMember( key ) );
    }

    @Override
    public Map<String, Object> getMap()
    {
        final JsObjectConverter converter = new JsObjectConverter( this.factory.getJavascriptHelper() );
        return converter.toMap( this.value );
    }
}
