package com.enonic.xp.impl.scheduler;

import com.google.common.base.Preconditions;

import com.enonic.xp.impl.scheduler.serializer.SchedulerSerializer;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.scheduler.ScheduledJob;
import com.enonic.xp.scheduler.SchedulerName;

public class GetScheduledJobCommand
    extends AbstractSchedulerCommand
{
    private final SchedulerName name;

    private GetScheduledJobCommand( final Builder builder )
    {
        super( builder );
        name = builder.name;
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
        final Node node = nodeService.getByPath( NodePath.create( NodePath.ROOT, name.getValue() ).build() );
        if ( node != null )
        {
            return SchedulerSerializer.fromNode( node );
        }

        return null;
    }

    public static final class Builder
        extends AbstractSchedulerCommand.Builder<Builder>
    {
        private SchedulerName name;

        private Builder()
        {
        }

        public Builder name( final SchedulerName name )
        {
            this.name = name;
            return this;
        }

        protected void validate()
        {
            Preconditions.checkNotNull( name, "name cannot be null." );
        }

        public GetScheduledJobCommand build()
        {
            validate();
            return new GetScheduledJobCommand( this );
        }
    }
}
