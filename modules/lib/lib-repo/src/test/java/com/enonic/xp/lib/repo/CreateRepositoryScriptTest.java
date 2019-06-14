package com.enonic.xp.lib.repo;

import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.xp.branch.BranchInfos;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositoryConstants;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.testing.ScriptTestSupport;

public class CreateRepositoryScriptTest
    extends ScriptTestSupport
{
    private RepositoryService repositoryService;

    @Override
    public void initialize()
        throws Exception
    {
        super.initialize();
        repositoryService = Mockito.mock( RepositoryService.class );
        Mockito.when( repositoryService.createRepository( Mockito.isA( CreateRepositoryParams.class ) ) ).
            thenAnswer( invocation -> {
                final CreateRepositoryParams params = (CreateRepositoryParams) invocation.getArguments()[0];
                return Repository.create().
                    id( params.getRepositoryId() ).
                    branchInfos( RepositoryConstants.MASTER_BRANCH_INFO  ).
                    settings( params.getRepositorySettings() ).
                    build();
            } );
        addService( RepositoryService.class, repositoryService );
    }

    @Test
    public void testExample()
    {
        runScript( "/lib/xp/examples/repo/create.js" );
        Mockito.verify( this.repositoryService, Mockito.times( 2 ) ).createRepository( Mockito.isA( CreateRepositoryParams.class ) );
    }
}
