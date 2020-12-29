package com.enonic.xp.core.impl.app.resolver;

import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrefixApplicationUrlResolverTest
{
    private ApplicationUrlResolver delegate;

    private PrefixApplicationUrlResolver resolver;

    @BeforeEach
    public void setup()
    {
        this.delegate = Mockito.mock( ApplicationUrlResolver.class );
        this.resolver = new PrefixApplicationUrlResolver( this.delegate, "/a/b" );
    }

    @Test
    public void testFindFiles()
    {
        Mockito.when( this.delegate.findFiles() ).thenReturn( Set.of( "a/b/c.txt", "a/b/c/d.txt", "a/other.txt" ) );

        final Set<String> files = this.resolver.findFiles();
        assertEquals( 2, files.size() );

        assertTrue( files.contains( "c.txt" ) );
        assertTrue( files.contains( "c/d.txt" ) );
    }

    @Test
    public void testFindUrl()
        throws Exception
    {
        final URL expected = Path.of( "." ).toUri().toURL();
        Mockito.when( this.delegate.findUrl( "a/b/c/d.txt" ) ).thenReturn( expected );

        final URL url1 = this.resolver.findUrl( "c/d.txt" );
        assertSame( expected, url1 );

        final URL url2 = this.resolver.findUrl( "other.txt" );
        assertNull( url2 );
    }
}
