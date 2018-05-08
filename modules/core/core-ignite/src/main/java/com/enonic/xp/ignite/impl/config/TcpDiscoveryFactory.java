package com.enonic.xp.ignite.impl.config;

import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.enonic.xp.cluster.NodeDiscovery;

class TcpDiscoveryFactory
{
    private final NodeDiscovery discovery;

    private final IgniteSettings igniteConfig;

    private TcpDiscoveryFactory( final Builder builder )
    {
        discovery = builder.discovery;
        igniteConfig = builder.igniteConfig;
    }

    TcpDiscoverySpi execute()
    {
        final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        discoverySpi.setIpFinder( createStaticIpConfig() ).
            setLocalPort( igniteConfig.discovery_tcp_port() ).
            setLocalPortRange( igniteConfig.discovery_tcp_port_range() ).
            setLocalAddress( igniteConfig.discovery_tcp_localAddress() ).
            setReconnectCount( igniteConfig.discovery_tcp_reconnect() ).
            setAckTimeout( igniteConfig.discovery_tcp_ack_timeout() ).
            setJoinTimeout( igniteConfig.discovery_tcp_join_timeout() ).
            setNetworkTimeout( igniteConfig.discovery_tcp_network_timeout() ).
            setSocketTimeout( igniteConfig.discovery_tcp_socket_timeout() ).
            setStatisticsPrintFrequency( igniteConfig.discovery_tcp_stat_printFreq() );

        return discoverySpi;
    }

    private TcpDiscoveryVmIpFinder createStaticIpConfig()
    {
        return StaticIpFinderFactory.create().
            discovery( this.discovery ).
            igniteConfig( this.igniteConfig ).
            build().
            execute();
    }

    static Builder create()
    {
        return new Builder();
    }

    static final class Builder
    {
        private NodeDiscovery discovery;

        private IgniteSettings igniteConfig;

        private Builder()
        {
        }

        Builder discovery( final NodeDiscovery val )
        {
            discovery = val;
            return this;
        }

        Builder igniteConfig( final IgniteSettings val )
        {
            igniteConfig = val;
            return this;
        }

        TcpDiscoveryFactory build()
        {
            return new TcpDiscoveryFactory( this );
        }
    }
}
