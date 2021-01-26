package com.enonic.xp.scheduler;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class ModifyScheduledJobParams
{
    private final SchedulerName name;

    private final ScheduledJobEditor editor;

    private ModifyScheduledJobParams( final Builder builder )
    {
        this.name = builder.name;
        this.editor = builder.editor;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public SchedulerName getName()
    {
        return name;
    }

    public ScheduledJobEditor getEditor()
    {
        return editor;
    }

    public static class Builder
    {
        private SchedulerName name;

        private ScheduledJobEditor editor;

        public Builder name( final SchedulerName name )
        {
            this.name = name;
            return this;
        }

        public Builder editor( final ScheduledJobEditor editor )
        {
            this.editor = editor;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( name, "name must be set." );
            Preconditions.checkNotNull( editor, "editor must be set." );
        }

        public ModifyScheduledJobParams build()
        {
            validate();
            return new ModifyScheduledJobParams( this );
        }
    }
}
