package com.enonic.wem.api.query.aggregation;

public class Bucket
{
    final String name;

    final long docCount;

    public Bucket( final String name, final long docCount )
    {
        this.name = name;
        this.docCount = docCount;
    }

    public String getName()
    {
        return name;
    }

    public long getDocCount()
    {
        return docCount;
    }
}
