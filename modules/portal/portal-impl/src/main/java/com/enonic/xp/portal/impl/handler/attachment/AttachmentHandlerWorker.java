package com.enonic.xp.portal.impl.handler.attachment;

import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import com.enonic.xp.attachment.Attachment;
import com.enonic.xp.attachment.Attachments;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.handler.PortalHandlerWorker;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.acl.Permission;
import com.enonic.xp.util.BinaryReference;
import com.enonic.xp.web.HttpMethod;
import com.enonic.xp.web.HttpStatus;
import com.enonic.xp.web.WebException;

import static com.enonic.xp.web.servlet.ServletRequestUrlHelper.contentDispositionAttachment;
import static com.google.common.base.Strings.nullToEmpty;

final class AttachmentHandlerWorker
    extends PortalHandlerWorker<PortalRequest>
{
    ContentService contentService;

    ContentId id;

    String name;

    boolean download;

    String fingerprint;

    String privateCacheControlHeaderConfig;

    String publicCacheControlHeaderConfig;

    AttachmentHandlerWorker( final PortalRequest request )
    {
        super( request );
    }

    @Override
    public PortalResponse execute()
        throws Exception
    {
        final Content content = getContent( this.id );
        final Attachment attachment = resolveAttachment( content, this.name );
        final BinaryReference binaryReference = attachment.getBinaryReference();
        final ByteSource binary = resolveBinary( this.id, binaryReference );

        if ( request.getMethod() == HttpMethod.OPTIONS )
        {
            // it will be handled by default OPTIONS handler in BaseWebHandler
            return PortalResponse.create().status( HttpStatus.METHOD_NOT_ALLOWED ).build();
        }

        final MediaType contentType = MediaType.parse( attachment.getMimeType() );
        final PortalResponse.Builder portalResponse = PortalResponse.create().
            contentType( contentType ).
            body( binary );

        if ( this.download )
        {
            portalResponse.header( "Content-Disposition", contentDispositionAttachment( attachment.getName() ) );
        }
        if ( this.name.endsWith( ".svgz" ) )
        {
            portalResponse.header( "Content-Encoding", "gzip" );
        }

        if ( !nullToEmpty( this.fingerprint ).isBlank() )
        {
            final boolean isPublic = content.getPermissions().isAllowedFor( RoleKeys.EVERYONE, Permission.READ ) &&
                ContentConstants.BRANCH_MASTER.equals( request.getBranch() );
            final String cacheControlHeaderConfig = isPublic ? publicCacheControlHeaderConfig : privateCacheControlHeaderConfig;

            if ( !nullToEmpty( cacheControlHeaderConfig ).isBlank() && this.fingerprint.equals( resolveHash( this.id, binaryReference ) ) )
            {
                portalResponse.header( HttpHeaders.CACHE_CONTROL, cacheControlHeaderConfig );
            }
        }

        new RangeRequestHelper().handleRangeRequest( request, portalResponse, binary, contentType );

        return portalResponse.build();
    }

    private Content getContent( final ContentId contentId )
    {
        final Content content = getContentById( contentId );
        if ( content == null )
        {
            if ( this.contentService.contentExists( contentId ) )
            {
                throw WebException.forbidden( String.format( "You don't have permission to access [%s]", contentId ) );
            }
            else
            {
                throw WebException.notFound( String.format( "Content with id [%s] not found", contentId.toString() ) );
            }
        }

        return content;
    }

    private Content getContentById( final ContentId contentId )
    {
        try
        {
            return this.contentService.getById( contentId );
        }
        catch ( final Exception e )
        {
            return null;
        }
    }

    private ByteSource resolveBinary( final ContentId id, final BinaryReference binaryReference )
    {
        final ByteSource binary = this.contentService.getBinary( id, binaryReference );
        if ( binary == null )
        {
            throw WebException.notFound( String.format( "Binary [%s] not found for [%s]", binaryReference, id ) );
        }

        return binary;
    }

    private String resolveHash( final ContentId contentId, final BinaryReference binaryReference )
    {
        return this.contentService.getBinaryKey( contentId, binaryReference );
    }

    private Attachment resolveAttachment( final Content content, final String name )
    {
        final Attachments attachments = content.getAttachments();
        final Attachment attachment = attachments.byName( name );
        if ( attachment != null )
        {
            return attachment;
        }

        throw WebException.notFound( String.format( "Attachment [%s] not found for [%s]", name, content.getPath() ) );
    }
}
