package com.enonic.xp.portal.url;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class ProcessHtmlParams
    extends AbstractUrlParams<ProcessHtmlParams>
{
    private String value;

    private List<Integer> imageWidths;

    public String getValue()
    {
        return this.value;
    }

    public ProcessHtmlParams value( final String value )
    {
        this.value = Strings.emptyToNull( value );
        return this;
    }

    public List<Integer> getImageWidths()
    {
        return imageWidths;
    }

    public ProcessHtmlParams imageWidths( final List<Integer> imageWidths )
    {
        this.imageWidths = imageWidths;
        return this;
    }

    @Override
    public ProcessHtmlParams setAsMap( final Multimap<String, String> map )
    {
        super.setAsMap( map );
        value( singleValue( map, "_value" ) );
        imageWidths( Objects.requireNonNullElse( map.removeAll( "_imageWidths" ), List.<String>of() ).
            stream().
            map( Integer::parseInt ).
            collect( Collectors.toList() ) );
        getParams().putAll( map );
        return this;
    }

    @Override
    protected void buildToString( final MoreObjects.ToStringHelper helper )
    {
        super.buildToString( helper );
        helper.add( "value", this.value );
        helper.add( "imageWidths", this.imageWidths );
    }
}
