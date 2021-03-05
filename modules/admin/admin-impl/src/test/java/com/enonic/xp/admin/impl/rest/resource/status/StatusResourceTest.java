package com.enonic.xp.admin.impl.rest.resource.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.admin.impl.rest.resource.AdminResourceTestSupport;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.index.IndexService;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.repository.IndexSettings;
import com.enonic.xp.server.ServerInfo;
import com.enonic.xp.server.VersionInfo;

public class StatusResourceTest
    extends AdminResourceTestSupport
{
    private Properties serverInfo;

    private IndexService indexService;

    @Override
    protected Object getResourceInstance()
    {
        this.serverInfo = new Properties();
        this.indexService = Mockito.mock( IndexService.class );

        final StatusResource resource = new StatusResource();
        resource.info = new ServerInfo( this.serverInfo );
        resource.setIndexService( this.indexService );
        return resource;
    }

    @Test
    public void testGetStatus()
        throws Exception
    {
        initServerInfo();

        final String json = request().path( "/status" ).get().getAsString();
        assertJson( "status_ok.json", json );
    }

    @Test
    public void testGetStatus_readonly()
        throws Exception
    {
        initServerInfo();

        final Map<String, Object> indexes = new HashMap<>();
        indexes.put( "index.blocks.write", true );

        Mockito.when( this.indexService.getIndexSettings( ContentConstants.CONTENT_REPO_ID, IndexType.SEARCH ) ).thenReturn(
            IndexSettings.from( indexes ) );

        final String json = request().path( "/status" ).get().getAsString();
        assertJson( "status_readonly.json", json );
    }

    private void initServerInfo()
    {
        VersionInfo.set( "1.1.1-SNAPSHOT" );

        this.serverInfo.put( "xp.build.branch", "master" );
        this.serverInfo.put( "xp.build.hash", "123456" );
        this.serverInfo.put( "xp.build.shortHash", "123" );
        this.serverInfo.put( "xp.build.timestamp", "2015-11-11T22:11:00" );
        this.serverInfo.put( "xp.name", "production" );
    }
}
