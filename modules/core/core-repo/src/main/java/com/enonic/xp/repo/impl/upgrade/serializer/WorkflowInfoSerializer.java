package com.enonic.xp.repo.impl.upgrade.serializer;

import com.google.common.collect.ImmutableMap;

import com.enonic.xp.content.WorkflowCheckState;
import com.enonic.xp.content.WorkflowInfo;
import com.enonic.xp.data.PropertySet;

import static com.enonic.xp.content.ContentPropertyNames.WORKFLOW_INFO_CHECKS;
import static com.enonic.xp.content.ContentPropertyNames.WORKFLOW_INFO_STATE;

public class WorkflowInfoSerializer
{
    public WorkflowInfo extract( final PropertySet workflowInfo )
    {
        if ( workflowInfo == null )
        {
            return WorkflowInfo.ready();
        }

        final ImmutableMap.Builder<String, WorkflowCheckState> mapBuilder = ImmutableMap.builder();
        PropertySet checkSet = workflowInfo.getSet( WORKFLOW_INFO_CHECKS );
        if ( checkSet != null )
        {
            for ( String checks : checkSet.getPropertyNames() )
            {
                mapBuilder.put( checks, WorkflowCheckState.valueOf( checkSet.getString( checks ) ) );
            }
        }

        return WorkflowInfo.create().
            state( workflowInfo.getString( WORKFLOW_INFO_STATE ) ).
            checks( mapBuilder.build() ).
            build();
    }
}
