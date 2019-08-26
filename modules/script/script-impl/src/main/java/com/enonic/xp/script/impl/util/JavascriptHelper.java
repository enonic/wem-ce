package com.enonic.xp.script.impl.util;

import org.graalvm.polyglot.Value;

public interface JavascriptHelper
{
    Value newJsArray();

    Value newJsObject();

    Value parseJson( String text );
}
