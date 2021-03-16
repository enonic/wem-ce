package com.enonic.xp.app;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationWildcardResolverTest
{
    private ApplicationWildcardResolver applicationWildcardResolver;

    @BeforeEach
    public void init()
    {
        this.applicationWildcardResolver = new ApplicationWildcardResolver();
    }

    @Test
    public void test_has_any_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "*test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test@!*%$sw" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test w* test" ) );
        assertTrue( this.applicationWildcardResolver.hasAnyWildcard( "test test*" ) );

        assertFalse( this.applicationWildcardResolver.hasAnyWildcard( "test$%^&" ) );
    }

    @Test
    public void test_string_has_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${app}test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${app}  test$%^&" ) );

        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "*test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "test@!*%$sw" ) );

        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "${ap}test*$%^&" ) );
        assertTrue( this.applicationWildcardResolver.stringHasWildcard( "*${app}  test$%^&" ) );
    }

    @Test
    public void test_has_app_wildcard()
    {
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}  test$%^&" ) );
        assertTrue( this.applicationWildcardResolver.startWithAppWildcard( "${app}${app} test$%^&" ) );

        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "{app}test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "$(app)test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "${ap}test$%^&" ) );
        assertFalse( this.applicationWildcardResolver.startWithAppWildcard( "S${app}test$%^&" ) );
    }

    @Test
    public void test_resolve_app_wildcard()
    {
        assertEquals( "app.myapp1:folder",
                      this.applicationWildcardResolver.resolveAppWildcard( "${app}:folder", ApplicationKey.from( "app.myapp1" ) ) );
    }

}
