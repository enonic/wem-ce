package com.enonic.xp.script.impl.function;

import org.graalvm.polyglot.Value;

import com.enonic.xp.app.Application;
import com.enonic.xp.config.Configuration;
import com.enonic.xp.script.impl.util.JavascriptHelper;

import java.util.Map;

public final class ApplicationInfoBuilder
{
    private Application application;

    private JavascriptHelper javascriptHelper;

    public ApplicationInfoBuilder application( final Application application )
    {
        this.application = application;
        return this;
    }

    public ApplicationInfoBuilder javascriptHelper( final JavascriptHelper javascriptHelper )
    {
        this.javascriptHelper = javascriptHelper;
        return this;
    }

    public Value build()
    {
        final Value result = this.javascriptHelper.newJsObject();
        result.putMember( "name", toString( this.application.getKey() ) );
        result.putMember( "version", toString( this.application.getVersion() ) );
        result.putMember( "config", buildConfig() );
        return result;
    }

    private Value buildConfig()
    {
        final Value result = this.javascriptHelper.newJsObject();
        final Configuration config = this.application.getConfig();

        if ( config != null )
        {
            for (Map.Entry<String, String> entry : config.asMap().entrySet()) {
                result.putMember( entry.getKey(), entry.getValue() );
            }
        }

        return result;
    }

    private String toString( final Object value )
    {
        return value != null ? value.toString() : "";
    }
}
