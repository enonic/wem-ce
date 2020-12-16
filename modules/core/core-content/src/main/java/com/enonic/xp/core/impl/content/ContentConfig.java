package com.enonic.xp.core.impl.content;

public @interface ContentConfig
{
    boolean auditlog_enabled() default true;

    String content_sync_period() default "PT0M";
}
