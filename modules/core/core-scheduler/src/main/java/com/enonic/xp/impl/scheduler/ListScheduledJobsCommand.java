package com.enonic.xp.impl.scheduler;

import java.util.List;
import java.util.stream.Collectors;

import com.enonic.xp.impl.scheduler.serializer.SchedulerSerializer;
import com.enonic.xp.node.FindNodesByParentParams;
import com.enonic.xp.node.FindNodesByParentResult;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.scheduler.ScheduledJob;

public class ListScheduledJobsCommand
    extends AbstractSchedulerCommand
{

    private ListScheduledJobsCommand( final Builder builder )
    {
        super( builder );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public List<ScheduledJob> execute()
    {
        return SchedulerContext.createContext().callWith( this::doExecute );
    }

    private List<ScheduledJob> doExecute()
    {
        final FindNodesByParentResult result = nodeService.findByParent( FindNodesByParentParams.create().
            parentPath( NodePath.ROOT ).
            size( -1 ).
            build() );

        return nodeService.getByIds( result.getNodeIds() ).
            stream().
            map( SchedulerSerializer::fromNode ).
            collect( Collectors.toList() );
    }

    public static final class Builder
        extends AbstractSchedulerCommand.Builder<Builder>
    {
        private Builder()
        {
        }

        public ListScheduledJobsCommand build()
        {
            validate();
            return new ListScheduledJobsCommand( this );
        }
    }
}
