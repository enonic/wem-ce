package com.enonic.xp.core.impl.app;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.osgi.framework.Bundle;

import com.enonic.xp.config.Configuration;
import com.enonic.xp.core.impl.app.resolver.ApplicationUrlResolver;
import com.enonic.xp.core.impl.app.resolver.BundleApplicationUrlResolver;
import com.enonic.xp.core.impl.app.resolver.ClassLoaderApplicationUrlResolver;
import com.enonic.xp.core.impl.app.resolver.MultiApplicationUrlResolver;
import com.enonic.xp.server.RunMode;

final class ApplicationFactory
{
    private final RunMode runMode;

    ApplicationFactory( final RunMode runMode )
    {
        this.runMode = runMode;
    }

    public ApplicationImpl create( final Bundle bundle )
    {
        return create( bundle, null );
    }

    public ApplicationImpl create( final Bundle bundle, final Configuration config )
    {
        final ApplicationBuilder builder = new ApplicationBuilder();
        builder.bundle( bundle );
        builder.urlResolver( createUrlResolver( bundle ) );
        builder.config( config );
        return builder.build();
    }

    ApplicationUrlResolver createUrlResolver( final Bundle bundle )
    {
        final ApplicationUrlResolver bundleUrlResolver = new BundleApplicationUrlResolver( bundle );
        if ( this.runMode != RunMode.DEV )
        {
            return bundleUrlResolver;
        }

        final List<String> sourcePaths = ApplicationHelper.getSourcePaths( bundle );
        if ( sourcePaths.isEmpty() )
        {
            return bundleUrlResolver;
        }

        final ApplicationUrlResolver classLoaderUrlResolver = createClassLoaderUrlResolver( sourcePaths );

        return new MultiApplicationUrlResolver( classLoaderUrlResolver, bundleUrlResolver );
    }

    private ApplicationUrlResolver createClassLoaderUrlResolver( final List<String> paths )
    {
        return ClassLoaderApplicationUrlResolver.create( getSearchPathUrls( paths ) );
    }

    private URL[] getSearchPathUrls( final List<String> paths )
    {
        return paths.stream().map( this::getSearchPathUrl ).filter( url -> url != null ).toArray( URL[]::new );
    }

    private URL getSearchPathUrl( final String path )
    {
        try
        {
            return Path.of( path ).toUri().toURL();
        }
        catch ( final Exception e )
        {
            return null;
        }
    }
}
