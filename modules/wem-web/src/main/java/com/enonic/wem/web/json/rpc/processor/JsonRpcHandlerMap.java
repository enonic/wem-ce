package com.enonic.wem.web.json.rpc.processor;

import java.util.Set;

import com.google.common.collect.ImmutableMap;

import com.enonic.wem.web.json.rpc.JsonRpcError;
import com.enonic.wem.web.json.rpc.JsonRpcException;
import com.enonic.wem.web.json.rpc.JsonRpcHandler;

final class JsonRpcHandlerMap
{
    private final ImmutableMap<String, JsonRpcHandler> map;

    public JsonRpcHandlerMap( final Set<JsonRpcHandler> handlers )
    {
        final ImmutableMap.Builder<String, JsonRpcHandler> builder = ImmutableMap.builder();
        for ( final JsonRpcHandler handler : handlers )
        {
            builder.put( handler.getName(), handler );
        }

        this.map = builder.build();
    }

    public JsonRpcHandler getHandler( final String name )
        throws JsonRpcException
    {
        final JsonRpcHandler handler = this.map.get( name );
        if ( handler != null )
        {
            return handler;
        }

        final JsonRpcError error = JsonRpcError.methodNotFound( name );
        throw new JsonRpcException( error );
    }
}
