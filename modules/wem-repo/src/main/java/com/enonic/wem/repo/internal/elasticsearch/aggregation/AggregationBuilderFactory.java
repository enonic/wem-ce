package com.enonic.wem.repo.internal.elasticsearch.aggregation;

import java.util.Collection;
import java.util.Set;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;

import com.google.common.collect.Sets;

import com.enonic.wem.api.query.aggregation.AbstractHistogramAggregationQuery;
import com.enonic.wem.api.query.aggregation.AbstractRangeAggregationQuery;
import com.enonic.wem.api.query.aggregation.AggregationQueries;
import com.enonic.wem.api.query.aggregation.AggregationQuery;
import com.enonic.wem.api.query.aggregation.TermsAggregationQuery;
import com.enonic.wem.repo.internal.index.query.IndexQueryFieldNameResolver;

public class AggregationBuilderFactory
{
    public static Set<AggregationBuilder> create( final AggregationQueries aggregationQueries )
    {
        return doCreate( aggregationQueries );
    }

    private static Set<AggregationBuilder> doCreate( final AggregationQueries aggregationQueries )
    {
        Set<AggregationBuilder> aggregationBuilders = Sets.newHashSet();

        for ( final AggregationQuery aggregationQuery : aggregationQueries )
        {
            final AggregationBuilder aggregationBuilder;

            if ( aggregationQuery instanceof TermsAggregationQuery )
            {
                aggregationBuilder = createTerms( (TermsAggregationQuery) aggregationQuery );
            }
            else if ( aggregationQuery instanceof AbstractRangeAggregationQuery )
            {
                aggregationBuilder = RangeAggregationBuilderFactory.create( (AbstractRangeAggregationQuery) aggregationQuery );
            }
            else if ( aggregationQuery instanceof AbstractHistogramAggregationQuery )
            {
                aggregationBuilder = HistogramAggregationQueryBuilderFactory.create( (AbstractHistogramAggregationQuery) aggregationQuery );
            }
            else
            {
                throw new IllegalArgumentException( "Unexpected aggregation type: " + aggregationQuery.getClass() );
            }

            final Set<AggregationBuilder> subAggregations = doCreate( aggregationQuery.getSubQueries() );

            for ( final AggregationBuilder subAggregation : subAggregations )
            {
                aggregationBuilder.subAggregation( subAggregation );
            }

            aggregationBuilders.add( aggregationBuilder );
        }

        return aggregationBuilders;
    }

    private static AggregationBuilder createTerms( final TermsAggregationQuery aggregationQuery )
    {
        final String fieldName = IndexQueryFieldNameResolver.resolveStringFieldName( aggregationQuery.getFieldName() );

        final TermsBuilder termsBuilder = new TermsBuilder( aggregationQuery.getName() ).
            minDocCount( 0 ).
            field( fieldName ).
            size( aggregationQuery.getSize() );

        if ( aggregationQuery.getOrderType() == TermsAggregationQuery.Type.TERM )
        {
            termsBuilder.order( Terms.Order.term( aggregationQuery.getOrderDirection().equals( TermsAggregationQuery.Direction.ASC ) ) );
        }
        else
        {
            termsBuilder.order( Terms.Order.count( aggregationQuery.getOrderDirection().equals( TermsAggregationQuery.Direction.ASC ) ) );
        }

        return termsBuilder;
    }


}
