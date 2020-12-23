package com.enonic.xp.repo.impl.project;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.project.Project;
import com.enonic.xp.project.ProjectName;
import com.enonic.xp.project.ProjectService;
import com.enonic.xp.project.Projects;
import com.enonic.xp.repo.impl.node.AbstractNodeTest;
import com.enonic.xp.repo.impl.upgrade.SyncTask;

class SyncTaskTest
    extends AbstractNodeTest
{
    @Test
    void run()
    {
        final ProjectService projectService = Mockito.mock( ProjectService.class );

        final Projects projects = Projects.create().
            addAll( Set.of( createProject( "turkey-tr-tr", "turkey-tr" ), createProject( "collicare-common", null ),
                            createProject( "corporate", "collicare-common" ), createProject( "corporate-no", "corporate" ),
                            createProject( "countries", "collicare-common" ), createProject( "china", "countries" ),
                            createProject( "denmark", "countries" ), createProject( "denmark-de", "denmark" ),
                            createProject( "finland", "countries" ), createProject( "finland-fi", "finland" ),
                            createProject( "india", "countries" ), createProject( "india-in", "india" ),
                            createProject( "sweden", "countries" ), createProject( "sweden-sw", "sweden" ),
                            createProject( "sweden-sw-sw", "sweden-sw" ), createProject( "root1", null ),
                            createProject( "child1", "root1" ), createProject( "without-actual-parent", "invalid-parent" ),
                            createProject( "turkey", "countries" ), createProject( "turkey-tr", "turkey" )

            ) ).
            build();

        Mockito.when( projectService.list() ).thenReturn( projects );

        SyncTask.create().
            indexService( indexService ).
            blobStore( blobStore ).
            versionService( versionService ).
            branchService( branchService ).
            projectService( projectService ).
            build().run();
    }

    private Project createProject( final String name, final String parent )
    {
        return Project.create().
            name( ProjectName.from( name ) ).
            displayName( name ).
            description( name ).
            parent( parent != null ? ProjectName.from( parent ) : null ).
            build();
    }

}
