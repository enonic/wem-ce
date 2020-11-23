package com.enonic.xp.elasticsearch.impl;

import java.util.Map;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.transport.TransportService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.cluster.ClusterConfig;
import com.enonic.xp.core.internal.Condition;

@Component(immediate = true, configurationPid = "com.enonic.xp.elasticsearch")
public final class ElasticsearchActivator
{
    private Node node;

    private ServiceRegistration<Node> nodeReg;

    private ServiceRegistration<Client> clientServiceRegistration;

    private ServiceRegistration<AdminClient> adminClientReg;

    private ServiceRegistration<ClusterAdminClient> clusterAdminClientReg;

    private ServiceRegistration<ClusterService> clusterServiceReg;

    private ServiceRegistration<TransportService> transportServiceReg;

    private final ClusterConfig clusterConfig;

    @Reference(target = "(" + Condition.CONDITION_ID + "=HazelcastActivatorActivated)")
    @SuppressWarnings("unused")
    private Condition condition;

    @Activate
    public ElasticsearchActivator( @Reference final ClusterConfig clusterConfig )
    {
        ESLoggerFactory.setDefaultFactory( new Slf4jESLoggerFactory() );
        this.clusterConfig = clusterConfig;
    }

    @Activate
    @SuppressWarnings("WeakerAccess")
    public void activate( final BundleContext context, final Map<String, String> map )
    {
        final Settings settings = new NodeSettingsBuilder( context, this.clusterConfig ).
            buildSettings( map );

        this.node = NodeBuilder.nodeBuilder().settings( settings ).build();
        this.node.start();

        final Injector injector = this.node.injector();
        final ClusterService clusterService = injector.getInstance( ClusterService.class );
        final TransportService transportService = injector.getInstance( TransportService.class );

        this.nodeReg = context.registerService( Node.class, this.node, null );
        this.clusterServiceReg = context.registerService( ClusterService.class, clusterService, null );
        this.transportServiceReg = context.registerService( TransportService.class, transportService, null );
        this.clientServiceRegistration = context.registerService( Client.class, this.node.client(), null );
        this.adminClientReg = context.registerService( AdminClient.class, this.node.client().admin(), null );
        this.clusterAdminClientReg = context.registerService( ClusterAdminClient.class, this.node.client().admin().cluster(), null );
    }

    @Deactivate
    @SuppressWarnings("WeakerAccess")
    public void deactivate()
    {
        this.nodeReg.unregister();
        this.transportServiceReg.unregister();
        this.clusterServiceReg.unregister();
        this.adminClientReg.unregister();
        this.clusterAdminClientReg.unregister();
        this.clientServiceRegistration.unregister();
        this.node.close();
    }
}

