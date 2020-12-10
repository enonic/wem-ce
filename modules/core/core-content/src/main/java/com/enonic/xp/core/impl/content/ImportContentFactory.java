package com.enonic.xp.core.impl.content;

import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.ImportContentParams;
import com.enonic.xp.core.impl.content.serializer.ContentDataSerializer;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeName;

import static com.enonic.xp.content.ContentPropertyNames.ORIGIN_PROJECT;

public class ImportContentFactory
{
    private final ImportContentParams params;

    private final ContentDataSerializer contentDataSerializer;

    private ImportContentFactory( Builder builder )
    {
        this.params = builder.params;
        this.contentDataSerializer = builder.contentDataSerializer;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public Node execute()
    {
        final PropertyTree nodeData = contentDataSerializer.toNodeData( params.getContent() );

        if ( params.getInherit() != null )
        {
            nodeData.removeProperties( ContentPropertyNames.INHERIT );
            nodeData.addStrings( ContentPropertyNames.INHERIT, params.getInherit().
                stream().
                map( Enum::name ).
                collect( Collectors.toSet() ) );
        }

        if ( params.getOriginProject() != null )
        {
            nodeData.setString( ORIGIN_PROJECT, params.getOriginProject().toString() );
        }
        else
        {
            nodeData.removeProperty( ORIGIN_PROJECT );
        }

        return Node.create().
            id( NodeId.from( params.getContent().getId().toString() ) ).
            parentPath( ContentNodeHelper.translateContentPathToNodePath( params.getTargetPath().getParentPath() ) ).
            name( NodeName.from( params.getTargetPath().getName() ) ).
            data( nodeData ).
            childOrder( params.getContent().getChildOrder() ).
            manualOrderValue( params.getContent().getManualOrderValue() ).
            permissions( params.getContent().getPermissions() ).
            inheritPermissions( params.getContent().inheritsPermissions() ).
            nodeType( ContentConstants.CONTENT_NODE_COLLECTION ).
            build();
    }


    public static final class Builder
    {
        private ImportContentParams params;

        private ContentDataSerializer contentDataSerializer;

        private Builder()
        {
        }

        public Builder params( final ImportContentParams params )
        {
            this.params = params;
            return this;
        }

        public Builder contentDataSerializer( final ContentDataSerializer contentDataSerializer )
        {
            this.contentDataSerializer = contentDataSerializer;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( this.params, "params cannot be null" );
        }

        public ImportContentFactory build()
        {
            validate();
            return new ImportContentFactory( this );
        }
    }

}
