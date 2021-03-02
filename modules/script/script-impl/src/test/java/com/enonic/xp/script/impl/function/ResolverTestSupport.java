package com.enonic.xp.script.impl.function;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;

public class ResolverTestSupport
{
    public Path temporaryFolder;

    protected ResourceService resourceService;

    @BeforeEach
    public final void setup()
        throws Exception
    {
        //TODO @TempDir JUnit5 suits better, but tests fail due to https://bugs.openjdk.java.net/browse/JDK-6956385
        temporaryFolder = Files.createTempDirectory("resolverTestSupport");
        this.resourceService = Mockito.mock( ResourceService.class );
        Mockito.when( this.resourceService.getResource( Mockito.any() ) ).then( this::loadResource );
    }

    private Resource loadResource( final InvocationOnMock invocation )
        throws Exception
    {
        return loadResource( (ResourceKey) invocation.getArguments()[0] );
    }

    private Resource loadResource( final ResourceKey key )
        throws Exception
    {
        final String relativeKeyPath = key.getPath().substring( 1 );
        return new UrlResource( key, this.temporaryFolder.resolve( relativeKeyPath ).toUri().toURL() );
    }

    final void touchFile( final String path )
        throws Exception
    {
        final Path filePath = this.temporaryFolder.resolve( path );
        Files.createDirectories( filePath.getParent() );
        Files.createFile( filePath );
    }
}
