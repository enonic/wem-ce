package com.enonic.wem.xslt.internal.function;

import com.enonic.wem.portal.view.ViewFunctions;

final class ComponentUrlFunction
    extends AbstractUrlFunction
{
    public ComponentUrlFunction( final ViewFunctions functions )
    {
        super( "componentUrl", functions );
    }

    @Override
    protected String execute( final String... params )
    {
        return this.functions.componentUrl( params );
    }
}
