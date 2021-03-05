package com.enonic.xp.admin.impl.rest.resource.status;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.enonic.xp.server.BuildInfo;
import com.enonic.xp.server.ServerInfo;
import com.enonic.xp.server.VersionInfo;

final class ProductInfoBuilder
{
    private final ServerInfo serverInfo;

    public ProductInfoBuilder( final ServerInfo serverInfo )
    {
        this.serverInfo = serverInfo;
    }

    public void build( final ObjectNode json )
    {
        json.put( "version", VersionInfo.get().getVersion() );
        json.set( "build", buildBuildInfo() );
        json.put( "installation", this.serverInfo.getName() );
    }

    private ObjectNode buildBuildInfo()
    {
        final BuildInfo info = this.serverInfo.getBuildInfo();

        final ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put( "hash", info.getHash() );
        node.put( "shortHash", info.getShortHash() );
        node.put( "branch", info.getBranch() );
        node.put( "timestamp", info.getTimestamp() );

        return node;
    }
}
