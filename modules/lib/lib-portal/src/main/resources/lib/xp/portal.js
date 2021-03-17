/**
 * Functions to access portal functionality.
 *
 * @example
 * var portalLib = require('/lib/xp/portal');
 *
 * @module portal
 */
/* global __*/

/**
 * This function generates a URL pointing to a static file.
 *
 * @example-ref examples/portal/assetUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} params.path Path to the asset.
 * @param {string} [params.application] Other application to reference to. Defaults to current application.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.assetUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.AssetUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to an image.
 *
 * @example-ref examples/portal/imageUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} params.id ID of the image content.
 * @param {string} params.path Path to the image. If `id` is specified, this parameter is not used.
 * @param {string} params.scale Required. Options are width(px), height(px), block(width,height) and square(px).
 * @param {number} [params.quality=85] Quality for JPEG images, ranges from 0 (max compression) to 100 (min compression).
 * @param {string} [params.background] Background color.
 * @param {string} [params.format] Format of the image.
 * @param {string} [params.filter] A number of filters are available to alter the image appearance, for example, blur(3), grayscale(), rounded(5), etc.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.imageUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.ImageUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to a component.
 *
 * @example-ref examples/portal/componentUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} [params.id] Id to the page.
 * @param {string} [params.path] Path to the page.
 * @param {string} [params.component] Path to the component. If not set, the current path is set.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.componentUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.ComponentUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to an attachment.
 *
 * @example-ref examples/portal/attachmentUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} [params.id] Id to the content holding the attachment.
 * @param {string} [params.path] Path to the content holding the attachment.
 * @param {string} [params.name] Name to the attachment.
 * @param {string} [params.label=source] Label of the attachment.
 * @param {boolean} [params.download=false] Set to true if the disposition header should be set to attachment.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.attachmentUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.AttachmentUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to a page.
 *
 * @example-ref examples/portal/pageUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} [params.id] Id to the page. If id is set, then path is not used.
 * @param {string} [params.path] Path to the page. Relative paths is resolved using the context page.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.pageUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.PageUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to a service.
 *
 * @example-ref examples/portal/serviceUrl.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} params.service Name of the service.
 * @param {string} [params.application] Other application to reference to. Default is current application.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.serviceUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.ServiceUrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function generates a URL pointing to an ID provider.
 *
 *
 * @param {object} [params] Input parameters as JSON.
 * @param {string} [params.idProvider] Key of an ID provider.
 * If idProvider is not set, then the id provider corresponding to the current execution context will be used.
 * @param {string} [params.contextPath=vhost] Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.idProviderUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.IdProviderUrlHandler');
    return bean.createUrl(__.toScriptValue(params || {}));
};

/**
 * This function generates a URL pointing to the login function of an ID provider.
 *
 *
 * @param {object} [params] Input parameters as JSON.
 * @param {string} [params.idProvider] Key of a id provider using an application.
 * If idProvider is not set, then the id provider corresponding to the current execution context will be used.
 * @param {string} [params.redirect] The URL to redirect to after the login.
 * @param {string} [params.contextPath=vhost] Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.loginUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.LoginUrlHandler');
    return bean.createUrl(__.toScriptValue(params || {}));
};

/**
 * This function generates a URL pointing to the logout function of the application corresponding to the current user.
 *
 *
 * @param {object} [params] Input parameters as JSON.
 * @param {string} [params.redirect] The URL to redirect to after the logout.
 * @param {string} [params.contextPath=vhost] Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.logoutUrl = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.LogoutUrlHandler');
    return bean.createUrl(__.toScriptValue(params || {}));
};

/**
 * This function generates a URL.
 *
 * @example-ref examples/portal/url.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} params.path Path of the resource.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {object} [params.params] Custom parameters to append to the url.
 *
 * @returns {string} The generated URL.
 */
exports.url = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.UrlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function replaces abstract internal links contained in an HTML text by generated URLs.
 *
 * When outputting processed HTML in Thymeleaf, use attribute `data-th-utext="${processedHtml}"`.
 *
 * @example-ref examples/portal/processHtml.js
 *
 * @param {object} params Input parameters as JSON.
 * @param {string} params.value Html value string to process.
 * @param {string} [params.type=server] URL type. Either `server` (server-relative URL) or `absolute`.
 * @param {number[]} [params.imageWidths] List of image width. Allows to generate image URLs for given image widths and use them in the `srcset` attribute of a `img` tag.
 *
 * @returns {string} The processed HTML.
 */
exports.processHtml = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.ProcessHtmlHandler');
    return bean.createUrl(__.toScriptValue(params));
};

/**
 * This function sanitizes an HTML string by stripping all potentially unsafe tags and attributes.
 *
 * HTML sanitization can be used to protect against cross-site scripting (XSS) attacks by sanitizing any HTML code submitted by a user.
 *
 * @example-ref examples/portal/sanitizeHtml.js
 *
 * @param {string} html Html string value to process.
 *
 * @returns {string} The sanitized HTML.
 */
exports.sanitizeHtml = function (html) {
    var bean = __.newBean('com.enonic.xp.lib.portal.SanitizeHtmlHandler');
    return __.toNativeObject(bean.sanitizeHtml(html));
};

/**
 * This function returns the parent site of the content corresponding to the current execution context. It is meant to be
 * called from a page, layout or part controller.
 *
 * @example-ref examples/portal/getSite.js
 *
 * @returns {object} The current site as JSON.
 */
exports.getSite = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.current.GetCurrentSiteHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns the site configuration for this app in the parent site of the content corresponding to the current
 * execution context. It is meant to be called from a page, layout or part controller.
 *
 * @example-ref examples/portal/getSiteConfig.js
 *
 * @returns {object} The site configuration for current application as JSON.
 */
exports.getSiteConfig = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.current.GetCurrentSiteConfigHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns the content corresponding to the current execution context. It is meant to be called from a page, layout or
 * part controller
 *
 * @example-ref examples/portal/getContent.js
 *
 * @returns {object} The current content as JSON.
 */
exports.getContent = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.current.GetCurrentContentHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns the component corresponding to the current execution context. It is meant to be called
 * from a layout or part controller.
 *
 * @example-ref examples/portal/getComponent.js
 *
 * @returns {object} The current component as JSON.
 */
exports.getComponent = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.current.GetCurrentComponentHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns the id provider key corresponding to the current execution context.
 *
 * @example-ref examples/portal/getIdProviderKey.js
 *
 * @returns {object} The current id provider as JSON.
 */
exports.getIdProviderKey = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.current.GetCurrentIdProviderKeyHandler');
    return __.toNativeObject(bean.execute());
};

/**
 * This function returns a JSON containing multipart items. If not a multipart request, then this function returns `undefined`.
 *
 * @example-ref examples/portal/getMultipartForm.js
 *
 * @returns {object} The multipart form items.
 */
exports.getMultipartForm = function () {
    var bean = __.newBean('com.enonic.xp.lib.portal.multipart.MultipartHandler');
    return __.toNativeObject(bean.getForm());
};

/**
 * This function returns a JSON containing a named multipart item. If the item does not exists, it returns `undefined`.
 *
 * @example-ref examples/portal/getMultipartItem.js
 *
 * @param {string} name Name of the multipart item.
 * @param {number} [index] Optional zero-based index. It should be specified if there are multiple items with the same name.
 *
 * @returns {object} The named multipart form item.
 */
exports.getMultipartItem = function (name, index) {
    var bean = __.newBean('com.enonic.xp.lib.portal.multipart.MultipartHandler');
    return __.toNativeObject(bean.getItem(name, index || 0));
};

/**
 * This function returns a data-stream for a named multipart item.
 *
 * @example-ref examples/portal/getMultipartStream.js
 *
 * @param {string} name Name of the multipart item.
 * @param {number} [index] Optional zero-based index. It should be specified if there are multiple items with the same name.
 *
 * @returns {*} Stream of multipart item data.
 */
exports.getMultipartStream = function (name, index) {
    var bean = __.newBean('com.enonic.xp.lib.portal.multipart.MultipartHandler');
    return bean.getBytes(name, index || 0);
};

/**
 * This function returns the multipart item data as text.
 *
 * @example-ref examples/portal/getMultipartText.js
 *
 * @param {string} name Name of the multipart item.
 * @param {number} [index] Optional zero-based index. It should be specified if there are multiple items with the same name.
 *
 * @returns {string} Text for multipart item data.
 */
exports.getMultipartText = function (name, index) {
    var bean = __.newBean('com.enonic.xp.lib.portal.multipart.MultipartHandler');
    return bean.getText(name, index || 0);
};

/**
 * This function generates a URL to an image placeholder.
 *
 * @example-ref examples/portal/imagePlaceholder.js
 *
 * @param {object} params Image parameters as JSON.
 * @param {number} params.width Width of the image in pixels.
 * @param {number} params.height Height of the image in pixels.
 *
 * @returns {string} Placeholder image URL.
 */
exports.imagePlaceholder = function (params) {
    var bean = __.newBean('com.enonic.xp.lib.portal.url.ImagePlaceholderHandler');
    bean.width = params.width || 0;
    bean.height = params.height || 0;
    return bean.createImagePlaceholder();
};
