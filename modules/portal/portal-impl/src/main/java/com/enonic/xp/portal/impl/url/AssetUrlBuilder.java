package com.enonic.xp.portal.impl.url;

import com.google.common.collect.Multimap;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.internal.security.MessageDigests;
import com.enonic.xp.portal.url.AssetUrlParams;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.util.HexEncoder;

final class AssetUrlBuilder
    extends GenericEndpointUrlBuilder<AssetUrlParams>
{
    private static final String ROOT_ASSET_PREFIX = "assets/";

    public AssetUrlBuilder()
    {
        super( "asset" );
    }

    @Override
    protected void buildUrl( final StringBuilder url, final Multimap<String, String> params )
    {
        super.buildUrl( url, params );

        ApplicationKey application = resolveApplication();
        String applicationKey = application.toString();
        final ResourceKey resourceKey;
        if ( this.params.getPath() == null )
        {
            resourceKey = ResourceKey.from( application, "META-INF/MANIFEST.MF" );
        }
        else
        {
            resourceKey = ResourceKey.from( application, ROOT_ASSET_PREFIX + this.params.getPath() );
        }
        final String hash = this.resourceService.processResource( createProcessor( resourceKey ) );

        if ( hash == null )
        {
            throw new IllegalArgumentException( "Could not find asset [" + resourceKey + "]" );
        }
        appendPart( url, applicationKey + ":" + hash );

        appendPart( url, this.params.getPath() );
    }

    private ApplicationKey resolveApplication()
    {
        return new ApplicationResolver().
            portalRequest( this.portalRequest ).
            application( this.params.getApplication() ).
            resolve();
    }

    private ResourceProcessor<ResourceKey, String> createProcessor( final ResourceKey key )
    {
        return new ResourceProcessor.Builder<ResourceKey, String>().
            key( key ).
            segment( "assetDescriptor" ).
            keyTranslator( rk -> rk ).
            processor( resource -> HexEncoder.toHex( MessageDigests.sha512().digest( resource.readBytes() ) ) ).
            build();
    }
}
