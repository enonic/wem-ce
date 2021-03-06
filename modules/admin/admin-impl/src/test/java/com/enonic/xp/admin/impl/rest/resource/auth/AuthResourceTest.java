package com.enonic.xp.admin.impl.rest.resource.auth;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.admin.impl.rest.resource.AdminResourceTestSupport;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.LocalScope;
import com.enonic.xp.security.IdProvider;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.security.IdProviders;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.SecurityService;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;
import com.enonic.xp.security.auth.AuthenticationToken;
import com.enonic.xp.session.SessionMock;

public class AuthResourceTest
    extends AdminResourceTestSupport
{
    private static final Instant NOW = Instant.ofEpochSecond( 0 );

    private static Clock clock = Clock.fixed( NOW, ZoneId.of( "UTC" ) );

    private SecurityService securityService;

    @Override
    protected Object getResourceInstance()
    {
        securityService = Mockito.mock( SecurityService.class );

        final AuthResource resource = new AuthResource();

        securityService = Mockito.mock( SecurityService.class );
        resource.setSecurityService( securityService );
        final IdProvider us1 = IdProvider.create().key( IdProviderKey.from( "remote" ) ).displayName( "Remote" ).build();
        final IdProvider us2 = IdProvider.create().key( IdProviderKey.system() ).displayName( "System" ).build();
        final IdProviders idProviders = IdProviders.from( us1, us2 );
        Mockito.when( securityService.getIdProviders() ).thenReturn( idProviders );

        return resource;
    }

    @Test
    public void testLoginWithUsernameSuccess()
        throws Exception
    {
        final User user = User.create().
            key( PrincipalKey.ofUser( IdProviderKey.system(), "user1" ) ).
            displayName( "User 1" ).
            modifiedTime( Instant.now( clock ) ).
            email( "user1@enonic.com" ).
            login( "user1" ).
            build();
        final AuthenticationInfo authInfo = AuthenticationInfo.create().user( user ).principals( RoleKeys.ADMIN_LOGIN ).build();
        Mockito.when( securityService.authenticate( Mockito.any( AuthenticationToken.class ) ) ).
            thenReturn( authInfo );

        String jsonString = request().path( "auth/login" ).
            entity( "{\"user\":\"user1\",\"password\":\"password\",\"rememberMe\":false}", MediaType.APPLICATION_JSON_TYPE ).
            post().getAsString();

        assertJson( "login.json", jsonString );
    }

    @Test
    public void testLoginWithEmailSuccess()
        throws Exception
    {
        final User user = User.create().
            key( PrincipalKey.ofUser( IdProviderKey.system(), "user1" ) ).
            displayName( "User 1" ).
            modifiedTime( Instant.now( clock ) ).
            email( "user1@enonic.com" ).
            login( "user1" ).
            build();
        final AuthenticationInfo authInfo = AuthenticationInfo.create().user( user ).principals( RoleKeys.ADMIN_LOGIN ).build();
        Mockito.when( securityService.authenticate( Mockito.any( AuthenticationToken.class ) ) ).
            thenReturn( authInfo );

        String jsonString = request().path( "auth/login" ).
            entity( "{\"user\":\"user1@enonic.com\",\"password\":\"password\",\"rememberMe\":false}", MediaType.APPLICATION_JSON_TYPE ).
            post().getAsString();

        assertJson( "login.json", jsonString );
    }

    @Test
    public void testLoginWithUserNameIdProvider()
        throws Exception
    {
        final User user = User.create().
            key( PrincipalKey.ofUser( IdProviderKey.system(), "user1" ) ).
            displayName( "User 1" ).
            modifiedTime( Instant.now( clock ) ).
            email( "user1@enonic.com" ).
            login( "user1" ).
            build();
        final AuthenticationInfo authInfo = AuthenticationInfo.create().user( user ).principals( RoleKeys.ADMIN_LOGIN ).build();
        Mockito.when( securityService.authenticate( Mockito.any( AuthenticationToken.class ) ) ).
            thenReturn( authInfo );

        String jsonString = request().path( "auth/login" ).
            entity( "{\"user\":\"system\\\\user1\",\"password\":\"password\",\"rememberMe\":false}", MediaType.APPLICATION_JSON_TYPE ).
            post().getAsString();

        assertJson( "login.json", jsonString );
    }

    @Test
    public void testLoginFail()
        throws Exception
    {
        final AuthenticationInfo authInfo = AuthenticationInfo.unAuthenticated();
        Mockito.when( securityService.authenticate( Mockito.any( AuthenticationToken.class ) ) ).
            thenReturn( authInfo );

        String jsonString = request().path( "auth/login" ).
            entity( "{\"user\":\"user1\",\"password\":\"password\",\"rememberMe\":false}", MediaType.APPLICATION_JSON_TYPE ).
            post().getAsString();

        assertJson( "login_failed.json", jsonString );
    }

    @Test
    public void testLoginUserWithoutLoginAdminRoleFail()
        throws Exception
    {
        final User user = User.create().
            key( PrincipalKey.ofUser( IdProviderKey.system(), "user1" ) ).
            displayName( "User 1" ).
            modifiedTime( Instant.now( clock ) ).
            email( "user1@enonic.com" ).
            login( "user1" ).
            build();
        final AuthenticationInfo authInfo = AuthenticationInfo.create().user( user ).build();
        Mockito.when( securityService.authenticate( Mockito.any( AuthenticationToken.class ) ) ).
            thenReturn( authInfo );

        String jsonString = request().path( "auth/login" ).
            entity( "{\"user\":\"user1\",\"password\":\"password\",\"rememberMe\":false}", MediaType.APPLICATION_JSON_TYPE ).
            post().getAsString();

        assertJson( "login_access_denied.json", jsonString );
    }

    @Test
    public void testAuthenticated_unauthenticated()
        throws Exception
    {
        String jsonString = request().path( "auth/authenticated" ).get().getAsString();

        assertJson( "authenticated_negative.json", jsonString );
    }

    @Test
    public void testAuthenticated_authenticated()
        throws Exception
    {
        final User user = User.create().
            key( PrincipalKey.ofUser( IdProviderKey.system(), "user1" ) ).
            displayName( "User 1" ).
            modifiedTime( Instant.now( clock ) ).
            email( "user1@enonic.com" ).
            login( "user1" ).
            build();

        final LocalScope localScope = ContextAccessor.current().getLocalScope();

        final AuthenticationInfo authInfo = AuthenticationInfo.create().user( user ).principals( RoleKeys.ADMIN_LOGIN ).build();
        localScope.setAttribute( authInfo );
        localScope.setSession( new SessionMock() );

        String jsonString = request().path( "auth/authenticated" ).get().getAsString();

        assertJson( "authenticated_success.json", jsonString );
    }

}
