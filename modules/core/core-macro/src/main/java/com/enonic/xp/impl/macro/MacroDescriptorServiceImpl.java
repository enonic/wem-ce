package com.enonic.xp.impl.macro;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.descriptor.DescriptorKeyLocator;
import com.enonic.xp.form.Form;
import com.enonic.xp.macro.MacroDescriptor;
import com.enonic.xp.macro.MacroDescriptorService;
import com.enonic.xp.macro.MacroDescriptors;
import com.enonic.xp.macro.MacroKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceProcessor;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.xml.XmlException;
import com.enonic.xp.xml.parser.XmlMacroDescriptorParser;

@Component
public final class MacroDescriptorServiceImpl
    implements MacroDescriptorService
{

    private static final String PATH = "/site/macros";

    private final BuiltinMacroDescriptors builtinMacrosDescriptors = new BuiltinMacroDescriptors();

    private ResourceService resourceService;

    private ApplicationService applicationService;

    @Override
    public MacroDescriptor getByKey( final MacroKey key )
    {
        MacroDescriptor descriptor;
        if ( isSystem( key.getApplicationKey() ) )
        {
            descriptor = builtinMacrosDescriptors.getByKey( key );
        }
        else
        {
            final ResourceProcessor<MacroKey, MacroDescriptor> descriptorProcessor = newDescriptorProcessor( key );
            descriptor = this.resourceService.processResource( descriptorProcessor );
            if ( descriptor == null )
            {
                final ResourceProcessor<MacroKey, MacroDescriptor> controllerProcessor = newControllerProcessor( key );
                descriptor = this.resourceService.processResource( controllerProcessor );
            }
        }

        return descriptor;
    }

    @Override
    public MacroDescriptors getByApplication( final ApplicationKey applicationKey )
    {
        final List<MacroDescriptor> list = new ArrayList<>();
        if ( isSystem( applicationKey ) )
        {
            list.addAll( builtinMacrosDescriptors.getAll().getSet() );
        }
        else
        {
            for ( final MacroKey descriptorKey : findDescriptorKeys( applicationKey ) )
            {
                final MacroKey macroKey = MacroKey.from( descriptorKey.getApplicationKey(), descriptorKey.getName() );
                final MacroDescriptor descriptor = getByKey( macroKey );
                if ( descriptor != null )
                {
                    list.add( descriptor );
                }
            }
        }

        return MacroDescriptors.from( list );
    }

    @Override
    public MacroDescriptors getByApplications( final ApplicationKeys applicationKeys )
    {
        final List<MacroDescriptor> list = new ArrayList<>();
        for ( final ApplicationKey key : applicationKeys )
        {
            list.addAll( getByApplication( key ).getSet() );
        }

        return MacroDescriptors.from( list );
    }

    @Override
    public MacroDescriptors getAll()
    {
        final Set<MacroDescriptor> set = new LinkedHashSet<>( builtinMacrosDescriptors.getAll().getSet() );

        for ( final Application application : this.applicationService.getInstalledApplications() )
        {
            final MacroDescriptors macroDescriptors = getByApplication( application.getKey() );
            set.addAll( macroDescriptors.getSet() );
        }

        return MacroDescriptors.from( set );
    }

    private boolean isSystem( ApplicationKey applicationKey )
    {
        return ApplicationKey.SYSTEM.equals( applicationKey );
    }

    private ResourceProcessor<MacroKey, MacroDescriptor> newDescriptorProcessor( final MacroKey key )
    {
        return new ResourceProcessor.Builder<MacroKey, MacroDescriptor>().
            key( key ).
            segment( "macroDescriptor" ).
            keyTranslator( MacroDescriptor::toDescriptorResourceKey ).
            processor( resource -> loadDescriptor( key, resource ) ).
            build();
    }

    private ResourceProcessor<MacroKey, MacroDescriptor> newControllerProcessor( final MacroKey key )
    {
        return new ResourceProcessor.Builder<MacroKey, MacroDescriptor>().
            key( key ).
            segment( "macroDescriptor" ).
            keyTranslator( MacroDescriptor::toControllerResourceKey ).
            processor( resource -> createDefaultDescriptor( key ) ).
            build();
    }

    private void parseXml( final Resource resource, final MacroDescriptor.Builder builder )
    {
        try
        {
            final XmlMacroDescriptorParser parser = new XmlMacroDescriptorParser();
            parser.builder( builder );
            parser.currentApplication( resource.getKey().getApplicationKey() );
            parser.source( resource.readString() );
            parser.parse();
        }
        catch ( final Exception e )
        {
            throw new XmlException( e, "Could not load macro descriptor [" + resource.getUrl() + "]: " + e.getMessage() );
        }
    }

    private MacroDescriptor loadDescriptor( final MacroKey key, final Resource resource )
    {
        final MacroDescriptor.Builder builder = MacroDescriptor.create();
        parseXml( resource, builder );
        builder.key( key ).icon( IconLoader.loadIcon( key, this.resourceService, PATH ) );
        return builder.build();
    }

    private MacroDescriptor createDefaultDescriptor( final MacroKey key )
    {
        return MacroDescriptor.
            create().
            key( key ).
            displayName( key.getName() ).
            form( Form.create().build() ).
            build();
    }

    private Set<MacroKey> findDescriptorKeys( final ApplicationKey key )
    {
        return new DescriptorKeyLocator( this.resourceService, PATH, true ).
            findKeys( key ).
            stream().
            map( dk -> MacroKey.from( dk.getApplicationKey(), dk.getName() ) ).collect( Collectors.toCollection( LinkedHashSet::new ) );
    }

    @Reference
    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }

    @Reference
    public void setApplicationService( final ApplicationService applicationService )
    {
        this.applicationService = applicationService;
    }
}
