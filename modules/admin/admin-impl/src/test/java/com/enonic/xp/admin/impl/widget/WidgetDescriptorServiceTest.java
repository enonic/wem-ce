package com.enonic.xp.admin.impl.widget;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.admin.widget.WidgetDescriptor;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.descriptor.DescriptorService;
import com.enonic.xp.descriptor.Descriptors;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

public class WidgetDescriptorServiceTest
{
    private DescriptorService descriptorService;

    private ResourceService resourceService;

    private WidgetDescriptorServiceImpl service;

    private WidgetDescriptor widgetDescriptor1;

    private WidgetDescriptor widgetDescriptor2;

    private WidgetDescriptor widgetDescriptor3;

    private WidgetDescriptor widgetDescriptor4;

    private WidgetDescriptor widgetDescriptor5;

    @BeforeEach
    public void setup()
    {
        this.descriptorService = Mockito.mock( DescriptorService.class );
        this.resourceService = Mockito.mock( ResourceService.class );

        this.service = new WidgetDescriptorServiceImpl();
        this.service.setDescriptorService( this.descriptorService );
        this.service.setResourceService( this.resourceService );

        widgetDescriptor1 = WidgetDescriptor.create().
            key( DescriptorKey.from( "app:a" ) ).
            addInterface( "com.enonic.xp.my-interface" ).
            build();

        widgetDescriptor2 = WidgetDescriptor.create().
            key( DescriptorKey.from( "app:b" ) ).
            addInterface( "com.enonic.xp.another-interface" ).
            build();

        widgetDescriptor3 = WidgetDescriptor.create().
            key( DescriptorKey.from( "app:c" ) ).
            addInterface( "com.enonic.xp.my-interface" ).
            setAllowedPrincipals( Collections.singleton( PrincipalKey.from( "role:system.user.admin" ) ) ).
            build();

        widgetDescriptor4 = WidgetDescriptor.create().
            key( DescriptorKey.from( "app:d" ) ).
            addInterface( "com.enonic.xp.my-interface" ).
            setAllowedPrincipals( Collections.singleton( PrincipalKey.from( "user:system:anonymous" ) ) ).
            build();

        widgetDescriptor5 = WidgetDescriptor.create().
            key( DescriptorKey.from( "app:e" ) ).
            addInterface( "com.enonic.xp.my-interface" ).
            setAllowedPrincipals( Collections.emptyList() ).
            build();


        final Descriptors<WidgetDescriptor> widgetDescriptors =
            Descriptors.from( widgetDescriptor1, widgetDescriptor2, widgetDescriptor3, widgetDescriptor4, widgetDescriptor5 );
        Mockito.when( this.descriptorService.getAll( WidgetDescriptor.class ) ).thenReturn( widgetDescriptors );
        Mockito.when( this.descriptorService.get( WidgetDescriptor.class, DescriptorKey.from( "app:c" ) ) ).thenReturn( widgetDescriptor3 );
        Mockito.when( this.descriptorService.get( WidgetDescriptor.class, DescriptorKey.from( "app:d" ) ) ).thenReturn( widgetDescriptor4 );
    }

    @Test
    public void get_by_application()
        throws Exception
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "app" );
        Mockito.when( this.resourceService.findFiles( eq( applicationKey ) ) ).thenReturn(
            Stream.of( ResourceKey.from( "app:admin/widgets/d/d.xml" ), ResourceKey.from( "app:admin/widgets/c/c.xml" ) ) );

        final Descriptors<WidgetDescriptor> result = this.service.getByApplication( applicationKey );

        assertEquals( 2, result.getSize() );
        assertTrue( result.contains( widgetDescriptor3 ) );
        assertTrue( result.contains( widgetDescriptor4 ) );
    }

    @Test
    public void get_by_interfaces()
        throws Exception
    {
        final Descriptors<WidgetDescriptor> result = this.service.getByInterfaces( "com.enonic.xp.my-interface" );
        assertEquals( 4, result.getSize() );
        assertTrue( result.contains( widgetDescriptor1 ) );
        assertTrue( result.contains( widgetDescriptor3 ) );
        assertTrue( result.contains( widgetDescriptor4 ) );
        assertTrue( result.contains( widgetDescriptor5 ) );
    }

    @Test
    public void get_allowed_by_interfaces()
        throws Exception
    {
        final Descriptors<WidgetDescriptor> result = this.service.getAllowedByInterfaces( "com.enonic.xp.my-interface" );
        assertEquals( 2, result.getSize() );
        assertTrue( result.contains( widgetDescriptor1 ) );
        assertTrue( result.contains( widgetDescriptor4 ) );
    }

    @Test
    public void get_allowed_by_interfaces_as_admin()
        throws Exception
    {
        final AuthenticationInfo authenticationInfo = AuthenticationInfo.copyOf( ContextAccessor.current().getAuthInfo() ).
            principals( PrincipalKey.ofRole( "system.admin" ) ).
            build();
        final Context adminContext = ContextBuilder.
            from( ContextAccessor.current() ).
            authInfo( authenticationInfo ).build();

        adminContext.runWith( () -> {
            final Descriptors<WidgetDescriptor> result = this.service.getAllowedByInterfaces( "com.enonic.xp.my-interface" );
            assertEquals( 4, result.getSize() );
            assertTrue( result.contains( widgetDescriptor1 ) );
            assertTrue( result.contains( widgetDescriptor3 ) );
            assertTrue( result.contains( widgetDescriptor4 ) );
            assertTrue( result.contains( widgetDescriptor5 ) );
        } );
    }

    @Test
    public void get_by_key()
        throws Exception
    {
        final WidgetDescriptor allowedWidget = this.service.getByKey( DescriptorKey.from( "app:d" ) );
        assertSame( allowedWidget, widgetDescriptor4 );

        final WidgetDescriptor unknownWidget = this.service.getByKey( DescriptorKey.from( "app:unknown" ) );
        assertNull( unknownWidget );
    }
}
