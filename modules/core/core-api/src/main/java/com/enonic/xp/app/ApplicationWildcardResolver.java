package com.enonic.xp.app;

public class ApplicationWildcardResolver
{
    public static final String APP_WILDCARD = "${app}";

    public static final String ANY_WILDCARD = "*";

    public boolean stringHasWildcard( final String contentTypeName )
    {
        return this.hasAnyWildcard( contentTypeName ) || this.startWithAppWildcard( contentTypeName );
    }

    public boolean startWithAppWildcard( String s )
    {
        return s.startsWith( APP_WILDCARD );
    }

    public boolean hasAnyWildcard( String s )
    {
        return s.contains( ANY_WILDCARD );
    }

    public String resolveAppWildcard( final String nameToResolve, final ApplicationKey applicationKey )
    {
        return startWithAppWildcard( nameToResolve ) ? nameToResolve.replace( APP_WILDCARD, applicationKey.toString() ) : nameToResolve;
    }

}
