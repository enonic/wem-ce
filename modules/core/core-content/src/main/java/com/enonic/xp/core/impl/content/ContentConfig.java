package com.enonic.xp.core.impl.content;

public @interface ContentConfig
{
    boolean auditlog_enabled() default true;

    boolean attachments_allowUnsafeNames() default false;
}
