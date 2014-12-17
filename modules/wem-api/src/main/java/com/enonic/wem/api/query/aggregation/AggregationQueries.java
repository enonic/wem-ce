package com.enonic.wem.api.query.aggregation;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.enonic.wem.api.support.AbstractImmutableEntitySet;

public class AggregationQueries
    extends AbstractImmutableEntitySet<AggregationQuery>
{
    private AggregationQueries( final ImmutableSet<AggregationQuery> set )
    {
        super( set );
    }

    private AggregationQueries( final Set<AggregationQuery> set )
    {
        super( ImmutableSet.copyOf( set ) );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static AggregationQueries empty()
    {
        final Set<AggregationQuery> returnFields = Sets.newHashSet();
        return new AggregationQueries( returnFields );
    }

    public static AggregationQueries fromCollection( final Collection<AggregationQuery> aggregationQueries )
    {
        return new AggregationQueries( ImmutableSet.copyOf( aggregationQueries ) );
    }

    public static final class Builder
    {
        private final Set<AggregationQuery> aggregationQueries = Sets.newHashSet();

        public Builder add( final AggregationQuery aggregationQuery )
        {
            this.aggregationQueries.add( aggregationQuery );
            return this;
        }

        public AggregationQueries build()
        {
            return new AggregationQueries( ImmutableSet.copyOf( this.aggregationQueries ) );
        }
    }

}
