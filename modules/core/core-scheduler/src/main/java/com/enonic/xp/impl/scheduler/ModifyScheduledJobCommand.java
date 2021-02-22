package com.enonic.xp.impl.scheduler;

import com.google.common.base.Preconditions;

import com.enonic.xp.impl.scheduler.serializer.SchedulerSerializer;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.RefreshMode;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.scheduler.ModifyScheduledJobParams;
import com.enonic.xp.scheduler.ScheduledJob;

public class ModifyScheduledJobCommand
    extends AbstractSchedulerCommand
{
    private final ModifyScheduledJobParams params;

    private ModifyScheduledJobCommand( final Builder builder )
    {
        super( builder );
        params = builder.params;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public ScheduledJob execute()
    {
        return SchedulerContext.createContext().callWith( this::doExecute );
    }

    private ScheduledJob doExecute()
    {
        final ScheduledJob original = GetScheduledJobCommand.create().
            nodeService( nodeService ).
            name( params.getName() ).
            build().
            execute();

        final UpdateNodeParams updateNodeParams = UpdateNodeParams.create().
            path( NodePath.create( NodePath.ROOT, params.getName().getValue() ).build() ).
            editor( ( toBeEdited -> toBeEdited.data = SchedulerSerializer.toUpdateNodeData( params, original ) ) ).
            build();

        final Node updatedNode = nodeService.update( updateNodeParams );
        nodeService.refresh( RefreshMode.ALL );

        return SchedulerSerializer.fromNode( updatedNode );
    }

    public static final class Builder
        extends AbstractSchedulerCommand.Builder<Builder>
    {
        private ModifyScheduledJobParams params;

        private Builder()
        {
        }

        public Builder params( final ModifyScheduledJobParams params )
        {
            this.params = params;
            return this;
        }

        protected void validate()
        {
            Preconditions.checkNotNull( params, "params cannot be null" );
        }

        public ModifyScheduledJobCommand build()
        {
            validate();
            return new ModifyScheduledJobCommand( this );
        }
    }
}
