package com.enonic.xp.core.internal.osgi;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class OsgiSupport
{
    private static Bundle bundle;

    private OsgiSupport()
    {
    }

    static void setBundle( final Bundle bundle )
    {
        OsgiSupport.bundle = bundle;
    }

    public static Bundle getBundle( final Class<?> clazz )
    {
        return Objects.requireNonNullElseGet( bundle, () -> FrameworkUtil.getBundle( clazz ) );
    }

    public static <T, R> R withServiceOrElseGet( Class<T> serviceClass, String filter, Function<? super T, R> function,
                                                 Supplier<R> noServiceValueSupplier )
    {
        final Bundle bundle = getBundle( serviceClass );
        if ( bundle == null )
        {
            throw new IllegalStateException( serviceClass + " class is not defined by a bundle class loader" );
        }

        final BundleContext bundleContext = bundle.getBundleContext();
        if ( bundleContext == null )
        {
            throw new IllegalStateException( bundle + " bundle has no valid BundleContext" );
        }

        final Collection<ServiceReference<T>> serviceReferences;
        try
        {
            serviceReferences = bundleContext.getServiceReferences( serviceClass, filter );
        }
        catch ( InvalidSyntaxException e )
        {
            throw new RuntimeException( e );
        }

        if ( serviceReferences.isEmpty() )
        {
            return noServiceValueSupplier.get();
        }

        final ServiceReference<T> serviceReference = serviceReferences.iterator().next();
        final T service = bundleContext.getService( serviceReference );
        try
        {
            if ( service != null )
            {
                return function.apply( service );
            }
            else
            {
                return noServiceValueSupplier.get();
            }
        }
        finally
        {
            bundleContext.ungetService( serviceReference );
        }
    }


    public static <T, R> R withServiceOrElse( Class<T> serviceClass, String filter, Function<? super T, R> function, R noServiceValue )
    {
        return withServiceOrElseGet( serviceClass, filter, function, () -> noServiceValue );
    }

    public static <T, R> R withService( Class<T> serviceClass, String filter, Function<? super T, R> function )
    {
        return withServiceOrElseGet( serviceClass, filter, function, () -> {
            throw new IllegalStateException( "No service found " + serviceClass + " " + filter );
        } );
    }

    public static <T, R> R withService( Class<T> serviceClass, Function<? super T, R> function )
    {
        return withServiceOrElseGet( serviceClass, null, function, () -> {
            throw new IllegalStateException( "No service found " + serviceClass );
        } );
    }
}
