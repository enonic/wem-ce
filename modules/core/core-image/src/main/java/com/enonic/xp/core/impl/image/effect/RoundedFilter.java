/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.xp.core.impl.image.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.enonic.xp.core.impl.image.ImageFunction;
import com.enonic.xp.image.ImageHelper;

public final class RoundedFilter
    implements ImageFunction
{
    private final int radius;

    private final int borderSize;

    private final int borderColor;

    public RoundedFilter( int radius, int borderSize, int borderColor )
    {
        this.radius = radius;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
    }

    @Override
    public BufferedImage apply( BufferedImage source )
    {
        BufferedImage dest = ImageHelper.createImage( source, true );
        Graphics2D g = getGraphics( dest );

        int arc = this.radius * 2;
        int margin = this.borderSize;

        if ( margin > 0 )
        {
            g.setPaint( new Color( this.borderColor, false ) );
            g.fillRoundRect( 0, 0, source.getWidth(), source.getHeight(), arc, arc );
        }

        arc = arc - ( margin * 2 );

        g.setPaint( new TexturePaint( source, new Rectangle2D.Float( 0, 0, source.getWidth(), source.getHeight() ) ) );
        g.fillRoundRect( margin, margin, source.getWidth() - ( margin * 2 ), source.getHeight() - ( margin * 2 ), arc, arc );
        g.dispose();

        return dest;
    }

    private static Graphics2D getGraphics( final BufferedImage img )
    {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        return g;
    }
}
