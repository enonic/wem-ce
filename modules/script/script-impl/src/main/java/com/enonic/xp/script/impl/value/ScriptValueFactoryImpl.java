package com.enonic.xp.script.impl.value;

import org.graalvm.polyglot.Value;

import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.util.JavascriptHelper;
import com.enonic.xp.script.impl.util.NashornHelper;

public final class ScriptValueFactoryImpl
    implements ScriptValueFactory
{
    private final JavascriptHelper helper;

    public ScriptValueFactoryImpl( final JavascriptHelper helper )
    {
        this.helper = helper;
    }

    @Override
    public JavascriptHelper getJavascriptHelper()
    {
        return this.helper;
    }

    @Override
    public ScriptValue newValue( final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( NashornHelper.isUndefined( value ) )
        {
            return null;
        }


        if ( value instanceof Value )
        {
            Value v = (Value) value;
            if (v.isBoolean()) {
                return new ScalarScriptValue(v.asBoolean());
            } else if (v.isNumber() && v.fitsInLong()) {
                return new ScalarScriptValue(v.asLong());
            } else if (v.isNumber() && v.fitsInDouble()) {
                return new ScalarScriptValue(v.asDouble());
            } else if (v.isString()) {
                return new ScalarScriptValue(v.asString());
            }
            return newValue( (Value) value );
        }

        return new ScalarScriptValue( value );
    }

    private ScriptValue newValue( final Value value )
    {
        if ( NashornHelper.isDateType( value ) )
        {
            return new ScalarScriptValue( NashornHelper.toDate( value ) );
        }

        if ( value.canExecute() )
        {
            return new FunctionScriptValue( this, value );
        }

        if ( value.hasArrayElements() )
        {
            return new ArrayScriptValue( this, value );
        }

        return new ObjectScriptValue( this, value );
    }
}
