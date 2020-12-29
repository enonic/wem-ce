package com.enonic.xp.core.impl.i18n;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.i18n.MessageBundle;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceKeys;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;

public class LocaleServiceImplTest
{
    private LocaleServiceImpl localeService;

    private ResourceService resourceService;

    @BeforeEach
    public void before()
        throws Exception
    {
        this.resourceService = Mockito.mock( ResourceService.class );
        Mockito.when( resourceService.getResource( Mockito.any() ) ).thenAnswer( this::loadResource );
        this.localeService = new LocaleServiceImpl( resourceService );
    }

    private Resource loadResource( final InvocationOnMock invocation )
    {
        final ResourceKey resourceKey = (ResourceKey) invocation.getArguments()[0];
        final String path = resourceKey.getName();
        final URL url = getClass().getResource( path.substring( path.lastIndexOf( '/' ) + 1 ) );
        return new UrlResource( resourceKey, url );
    }

    @Test
    public void get_bundle()
        throws Exception
    {
        final MessageBundle bundle = localeService.getBundle( ApplicationKey.from( "myapplication" ), Locale.ENGLISH );

        assertNotNull( bundle );
        assertEquals( 8, bundle.getKeys().size() );
        assertEquals( "en", bundle.localize( "msg" ) );
    }

    @Test
    public void get_bundle_no_application()
        throws Exception
    {
        final MessageBundle bundle = localeService.getBundle( null, Locale.ENGLISH );
        assertNull( bundle );
    }

    @Test
    public void get_bundle_no_locale()
        throws Exception
    {
        final MessageBundle bundle = localeService.getBundle( ApplicationKey.from( "myapplication" ), null );

        assertNotNull( bundle );
        assertEquals( 7, bundle.getKeys().size() );
        assertEquals( "default", bundle.localize( "msg" ) );
    }

    @Test
    public void get_bundle_with_country()
        throws Exception
    {
        final MessageBundle bundle = localeService.getBundle( ApplicationKey.from( "myapplication" ), Locale.US );

        assertNotNull( bundle );
        assertEquals( 9, bundle.getKeys().size() );
        assertEquals( "en_US", bundle.localize( "msg" ) );
    }

    @Test
    public void get_bundle_with_variant()
        throws Exception
    {
        final Locale locale = new Locale( "en", "US", "1" );
        final MessageBundle bundle = localeService.getBundle( ApplicationKey.from( "myapplication" ), locale );

        assertNotNull( bundle );
        assertEquals( 10, bundle.getKeys().size() );
        assertEquals( "en_US_1", bundle.localize( "msg" ) );
    }

    @Test
    public void get_UTF8_chars()
    {
        final MessageBundle bundle = localeService.getBundle( ApplicationKey.from( "myapplication" ), Locale.ENGLISH );

        assertNotNull( bundle );
        assertEquals( "æøå", bundle.localize( "norwegian" ) );
        assertEquals( "ÄäÜüß", bundle.localize( "german" ) );
        assertEquals( "ŁĄŻĘĆŃŚŹ", bundle.localize( "polish" ) );
        assertEquals( "ЯБГДЖЙ", bundle.localize( "russian" ) );
        assertEquals( "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃ", bundle.localize( "japanese" ) );
    }

    @Test
    public void get_bundle_multi()
        throws Exception
    {
        final MessageBundle bundle =
            localeService.getBundle( ApplicationKey.from( "myapplication" ), Locale.ENGLISH, "/phrases", "/override" );

        assertNotNull( bundle );
        assertEquals( 9, bundle.getKeys().size() );
        assertEquals( "override", bundle.localize( "msg" ) );
    }

    @Test
    public void getLocales()
        throws Exception
    {
        final ResourceKeys resourceKeys =
            ResourceKeys.from( "myapplication:/i18n/myphrases.properties", "myapplication:/i18n/myphrases_en.properties",
                               "myapplication:/i18n/myphrases_en_US.properties",
                               "myapplication:/i18n/myphrases_en_US_1.properties", "myapplication:/i18n/myphrases_fr.properties",
                               "myapplication:/i18n/myphrases_ca.properties" );

        Mockito.when( resourceService.findFiles( Mockito.any(), notNull() ) ).thenReturn( resourceKeys );

        final Set<Locale> locales = localeService.getLocales( ApplicationKey.from( "myapplication" ), "/phrases" );

        assertNotNull( locales );
        assertEquals( 5, locales.size() );
        assertTrue( locales.contains( new Locale( "en" ) ) );
        assertTrue( locales.contains( new Locale( "en", "US" ) ) );
        assertTrue( locales.contains( new Locale( "en", "US", "1" ) ) );
        assertTrue( locales.contains( new Locale( "fr" ) ) );
        assertTrue( locales.contains( new Locale( "ca" ) ) );
    }

    @Test
    public void bundleInvalidateCaching()
    {
        final ResourceKeys resourceKeys = ResourceKeys.empty();
        Mockito.when( resourceService.findFiles( Mockito.any(), Mockito.anyString() ) ).thenReturn( resourceKeys );

        final ApplicationKey myApp = ApplicationKey.from( "myapplication" );
        final ApplicationKey otherApp = ApplicationKey.from( "otherapp" );

        MessageBundle bundleCached = localeService.getBundle( myApp, Locale.ENGLISH, "/phrases", "/override" );
        MessageBundle bundle = localeService.getBundle( myApp, Locale.ENGLISH, "/phrases", "/override" );

        MessageBundle otherBundleCached = localeService.getBundle( otherApp, Locale.ENGLISH, "/texts" );
        MessageBundle otherBundle = localeService.getBundle( otherApp, Locale.ENGLISH, "/texts" );

        assertSame( bundle, bundleCached );
        assertSame( otherBundle, otherBundleCached );

        Application application = Mockito.mock( Application.class );
        Mockito.when( application.getKey() ).thenReturn( myApp );
        localeService.activated( application );

        bundle = localeService.getBundle( myApp, Locale.ENGLISH, "/phrases", "/override" );
        otherBundle = localeService.getBundle( otherApp, Locale.ENGLISH, "/texts" );

        assertNotSame( bundle, bundleCached );
        assertSame( otherBundle, otherBundleCached );
    }

    @Test
    public void getSupportedLocale_onePreferredFound()
        throws Exception
    {
        final ResourceKeys resourceKeys =
            ResourceKeys.from( "myapplication:/site/i18n/myphrases.properties", "myapplication:/site/i18n/myphrases_en.properties",
                               "myapplication:/site/i18n/myphrases_en_US.properties",
                               "myapplication:/site/i18n/myphrases_en_US_1.properties", "myapplication:/site/i18n/myphrases_fr.properties",
                               "myapplication:/site/i18n/myphrases_ca.properties" );

        Mockito.when( resourceService.findFiles( Mockito.any(), Mockito.anyString() ) ).thenReturn( resourceKeys );

        List<Locale> preferredLocales = localeList( "en-US" );
        Locale supportedLocale = localeService.getSupportedLocale( preferredLocales, ApplicationKey.from( "myapplication" ), "/myphrases" );

        assertNotNull( supportedLocale );
        assertEquals( "en-US", supportedLocale.toLanguageTag() );
    }

    @Test
    public void getSupportedLocale_LanguagePreferredFound()
        throws Exception
    {
        final ResourceKeys resourceKeys =
            ResourceKeys.from( "myapplication:/site/i18n/myphrases.properties", "myapplication:/site/i18n/myphrases_en.properties",
                               "myapplication:/site/i18n/myphrases_en_US.properties",
                               "myapplication:/site/i18n/myphrases_en_US_1.properties", "myapplication:/site/i18n/myphrases_fr.properties",
                               "myapplication:/site/i18n/myphrases_ca.properties" );

        Mockito.when( resourceService.findFiles( Mockito.any(), Mockito.anyString() ) ).thenReturn( resourceKeys );

        List<Locale> preferredLocales = localeList( "en-UK" );
        Locale supportedLocale = localeService.getSupportedLocale( preferredLocales, ApplicationKey.from( "myapplication" ), "/myphrases" );

        assertNotNull( supportedLocale );
        assertEquals( "en", supportedLocale.toLanguageTag() );
    }

    @Test
    public void getSupportedLocale_moreThanOneFound()
        throws Exception
    {
        final ResourceKeys resourceKeys =
            ResourceKeys.from( "myapplication:/site/i18n/myphrases.properties", "myapplication:/site/i18n/myphrases_en.properties",
                               "myapplication:/site/i18n/myphrases_en_US.properties",
                               "myapplication:/site/i18n/myphrases_en_US_1.properties", "myapplication:/site/i18n/myphrases_fr.properties",
                               "myapplication:/site/i18n/myphrases_ca.properties" );

        Mockito.when( resourceService.findFiles( Mockito.any(), Mockito.anyString() ) ).thenReturn( resourceKeys );

        List<Locale> preferredLocales = localeList( "no", "ca-ES", "en" );
        Locale supportedLocale = localeService.getSupportedLocale( preferredLocales, ApplicationKey.from( "myapplication" ), "/myphrases" );

        assertNotNull( supportedLocale );
        assertEquals( "ca", supportedLocale.toLanguageTag() );
    }


    @Test
    public void getSupportedLocale_noPreferredFound()
        throws Exception
    {
        final ResourceKeys resourceKeys =
            ResourceKeys.from( "myapplication:/site/i18n/myphrases.properties", "myapplication:/site/i18n/myphrases_en.properties",
                               "myapplication:/site/i18n/myphrases_en_US.properties",
                               "myapplication:/site/i18n/myphrases_en_US_1.properties", "myapplication:/site/i18n/myphrases_fr.properties",
                               "myapplication:/site/i18n/myphrases_ca.properties" );

        Mockito.when( resourceService.findFiles( Mockito.any(), Mockito.anyString() ) ).thenReturn( resourceKeys );

        List<Locale> preferredLocales = localeList( "no" );
        Locale supportedLocale = localeService.getSupportedLocale( preferredLocales, ApplicationKey.from( "myapplication" ), "/myphrases" );

        assertNull( supportedLocale );
    }

    private List<Locale> localeList( final String... localeTags )
    {
        return Arrays.stream( localeTags ).map( Locale::forLanguageTag ).collect( Collectors.toList() );
    }
}
