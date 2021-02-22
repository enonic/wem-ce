package com.enonic.xp.scheduler;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.node.NodeType;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;

public class SchedulerConstants
{
    public static final RepositoryId SCHEDULER_REPO_ID = RepositoryId.from( "system.scheduler" );

    public static final Branch SCHEDULER_BRANCH = Branch.create().
        value( "master" ).
        build();

    public static final AccessControlList SCHEDULER_REPO_DEFAULT_ACL = AccessControlList.create().
        add( AccessControlEntry.create().
            allowAll().
            principal( RoleKeys.ADMIN ).
            build() ).
        build();

    /*
        public static final IndexPath TIME = IndexPath.from( AuditLogPropertyNames.TIME );

        public static final ChildOrder AUDIT_LOG_REPO_DEFAULT_CHILD_ORDER = ChildOrder.create().
            add( FieldOrderExpr.create( TIME, OrderExpr.Direction.DESC ) ).
            build();
    */
    public static final NodeType NODE_TYPE = NodeType.from( "scheduler" );
}
