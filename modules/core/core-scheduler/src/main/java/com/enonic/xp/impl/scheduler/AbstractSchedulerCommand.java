package com.enonic.xp.impl.scheduler;

import com.google.common.base.Preconditions;

import com.enonic.xp.node.NodeService;

public abstract class AbstractSchedulerCommand
{
    protected final NodeService nodeService;

    protected <B extends Builder<B>> AbstractSchedulerCommand( final Builder<B> builder )
    {
        this.nodeService = builder.nodeService;
    }

    public abstract static class Builder<B extends Builder<B>>
    {
        private NodeService nodeService;

        protected Builder()
        {
        }

        public B nodeService( final NodeService nodeService )
        {
            this.nodeService = nodeService;
            return (B) this;
        }

        protected void validate()
        {
            Preconditions.checkNotNull( nodeService, "nodeService cannot be null." );
        }

        abstract AbstractSchedulerCommand build();
    }
}
