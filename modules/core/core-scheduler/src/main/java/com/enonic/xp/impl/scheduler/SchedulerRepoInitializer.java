package com.enonic.xp.impl.scheduler;

import com.google.common.base.Preconditions;

import com.enonic.xp.context.Context;
import com.enonic.xp.init.ExternalInitializer;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.scheduler.SchedulerConstants;

public class SchedulerRepoInitializer
    extends ExternalInitializer
{
    private final RepositoryService repositoryService;

    private final Context adminContext;

    protected SchedulerRepoInitializer( final Builder builder )
    {
        super( builder );
        this.repositoryService = builder.repositoryService;
        this.adminContext = SchedulerContext.createAdminContext();
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    protected boolean isInitialized()
    {
        return this.adminContext.callWith( () -> repositoryService.isInitialized( SchedulerConstants.SCHEDULER_REPO_ID ) );
    }

    @Override
    protected void doInitialize()
    {
        this.adminContext.runWith( this::initializeRepository );
    }

    private void initializeRepository()
    {
        final CreateRepositoryParams createRepositoryParams = CreateRepositoryParams.create().
            repositoryId( SchedulerConstants.SCHEDULER_REPO_ID ).
            rootPermissions( SchedulerConstants.SCHEDULER_REPO_DEFAULT_ACL ).
            build();

        this.repositoryService.createRepository( createRepositoryParams );
    }

    @Override
    protected String getInitializationSubject()
    {
        return SchedulerConstants.SCHEDULER_REPO_ID + " repo";
    }

    public static class Builder
        extends ExternalInitializer.Builder<Builder>
    {
        private RepositoryService repositoryService;

        public Builder setRepositoryService( final RepositoryService repositoryService )
        {
            this.repositoryService = repositoryService;
            return this;
        }

        @Override
        protected void validate()
        {
            super.validate();
            Preconditions.checkNotNull( repositoryService );
        }

        public SchedulerRepoInitializer build()
        {
            validate();
            return new SchedulerRepoInitializer( this );
        }
    }
}
