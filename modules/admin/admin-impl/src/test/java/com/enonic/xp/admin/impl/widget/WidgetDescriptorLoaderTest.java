package com.enonic.xp.admin.impl.widget;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

import com.enonic.xp.admin.widget.WidgetDescriptor;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.impl.app.ApplicationTestSupport;
import com.enonic.xp.core.impl.app.MockApplication;
import com.enonic.xp.descriptor.DescriptorKeys;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.security.PrincipalKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WidgetDescriptorLoaderTest
    extends ApplicationTestSupport
{
    private WidgetDescriptorLoader loader;

    @Override
    protected void initialize()
        throws Exception
    {
        this.loader = new WidgetDescriptorLoader();
        this.loader.setResourceService( this.resourceService );
        this.loader.setApplicationService( this.applicationService );

        final Bundle bundle = Mockito.mock( Bundle.class );
        final URL iconUrl = getClass().getResource( "/apps/myapp1/admin/widgets/widget1/widget1.svg" );
        Mockito.when( bundle.getEntry( "/admin/widgets/widget2/widget2.svg" ) ).thenReturn( null );
        Mockito.when( bundle.getEntry( "/admin/widgets/widget1/widget1.svg" ) ).thenReturn( iconUrl );
        Mockito.when( bundle.getResource( "/admin/widgets/widget1/widget1.svg" ) ).thenReturn( iconUrl );

        final MockApplication app = addApplication( "myapp1", "/apps/myapp1" );
        app.setBundle( bundle );
    }

    @Test
    public void testGetType()
    {
        assertEquals( WidgetDescriptor.class, this.loader.getType() );
    }

    @Test
    public void testPostProcess()
    {
        final WidgetDescriptor descriptor = WidgetDescriptor.create().key( DescriptorKey.from( "myapp:a" ) ).build();
        assertSame( descriptor, this.loader.postProcess( descriptor ) );
    }

    @Test
    public void testCreateDefault()
    {
        final DescriptorKey key = DescriptorKey.from( "myapp1:widget1" );
        final WidgetDescriptor descriptor = this.loader.createDefault( key );

        assertEquals( key, descriptor.getKey() );
        assertEquals( "widget1", descriptor.getName() );
    }

    @Test
    public void testFind()
    {
        final DescriptorKeys keys = this.loader.find( ApplicationKey.from( "myapp1" ) );
        assertThat( keys ).containsExactlyInAnyOrder( DescriptorKey.from( "myapp1:widget1" ), DescriptorKey.from( "myapp1:widget2" ) );
    }

    @Test
    public void testLoadMax()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "myapp1:widget1" );
        final WidgetIconUrlResolver iconUrlResolver = new WidgetIconUrlResolver();

        final ResourceKey resourceKey = this.loader.toResource( descriptorKey );
        assertEquals( "myapp1:/admin/widgets/widget1/widget1.xml", resourceKey.toString() );

        final Resource resource = this.resourceService.getResource( resourceKey );
        final WidgetDescriptor descriptor = this.loader.load( descriptorKey, resource );

        assertEquals( "MyWidget", descriptor.getDisplayName() );
        assertTrue( iconUrlResolver.resolve( descriptor ).startsWith( "/admin/rest/widget/icon/myapp1/widget1?hash=" ) );
        assertEquals( 1, descriptor.getInterfaces().size() );
        assertTrue( descriptor.getInterfaces().contains( "com.enonic.xp.my-interface" ) );
        assertEquals( 1, descriptor.getAllowedPrincipals().getSize() );
        assertTrue( descriptor.getAllowedPrincipals().contains( PrincipalKey.from( "role:system.user.admin" ) ) );
    }

    @Test
    public void testLoadMin()
    {
        final DescriptorKey descriptorKey = DescriptorKey.from( "myapp1:widget2" );
        final ResourceKey resourceKey = this.loader.toResource( descriptorKey );
        final Resource resource = this.resourceService.getResource( resourceKey );
        final WidgetDescriptor descriptor = this.loader.load( descriptorKey, resource );

        assertEquals( "MyWidget2", descriptor.getDisplayName() );
        assertEquals( "MyWidget2 description", descriptor.getDescription() );
        assertEquals( 1, descriptor.getInterfaces().size() );
        assertTrue( descriptor.getInterfaces().contains( "com.enonic.xp.my-interface" ) );
        assertNull( descriptor.getAllowedPrincipals() );
    }
}
