package com.enonic.xp.page;

import java.util.regex.Pattern;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationWildcardResolver;

public interface DescriptorKeyMatcher
{
    boolean matches( final DescriptorKey descriptorKey );

    static DescriptorKeyMatcher relative( final ApplicationKey applicationKey, final String pattern )
    {
        return new DescriptorKeyMatcher()
        {
            final Pattern compiled = Pattern.compile( pattern );

            @Override
            public boolean matches( final DescriptorKey descriptorKey )
            {
                return applicationKey.equals( descriptorKey.getApplicationKey() ) && compiled.matcher( descriptorKey.getName() ).matches();
            }
        };
    }

    static DescriptorKeyMatcher absolute( final String pattern )
    {
        return new DescriptorKeyMatcher()
        {
            final Pattern compiled = Pattern.compile( pattern );

            @Override
            public boolean matches( final DescriptorKey descriptorKey )
            {
                return compiled.matcher( descriptorKey.toString() ).matches();
            }
        };
    }

    static DescriptorKeyMatcher legacy( final ApplicationKey applicationKey, final String pattern )
    {
        final ApplicationWildcardResolver applicationWildcardResolver = new ApplicationWildcardResolver();
        String resolvedPattern = applicationWildcardResolver.resolveAppWildcard( pattern, applicationKey );
        resolvedPattern = resolvedPattern.replace( "*", ".*" );
        return absolute( resolvedPattern );
    }
}
