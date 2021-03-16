package com.enonic.xp.site.components;

import com.google.common.base.Preconditions;

public final class DescriptorPattern
{
    public enum ComponentMatcher
    {
        ABSOLUTE, RELATIVE, LEGACY
    }

    private final ComponentMatcher matcher;

    private final String expression;

    public ComponentMatcher getMatcher()
    {
        return matcher;
    }

    public String getExpression()
    {
        return expression;
    }

    public DescriptorPattern( final Builder builder )
    {
        this.matcher = Preconditions.checkNotNull( builder.matcher );
        this.expression = Preconditions.checkNotNull( builder.expression );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
    {
        private ComponentMatcher matcher = ComponentMatcher.RELATIVE;

        private String expression;

        public Builder matcher( final ComponentMatcher matcher )
        {
            this.matcher = matcher;
            return this;
        }

        public Builder expression( final String expression )
        {
            this.expression = expression;
            return this;
        }

        public DescriptorPattern build()
        {
            Preconditions.checkNotNull( this.matcher );
            Preconditions.checkNotNull( this.expression );

            return new DescriptorPattern( this );
        }
    }
}
