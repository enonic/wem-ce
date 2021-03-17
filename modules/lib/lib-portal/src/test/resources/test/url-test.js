var assert = require('/lib/xp/testing.js');
var portal = require('/lib/xp/portal.js');

exports.assetUrlTest = function () {
    var result = portal.assetUrl({
        path: 'styles/my.css',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('AssetUrlParams{type=server, params={a=[1], b=[1, 2]}, path=styles/my.css}', result);
    return true;
};

exports.assetUrlTest_unknownProperty = function () {
    var result = portal.assetUrl({
        path: 'styles/my.css',
        unknownProperty: 'value',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    assert.assertEquals('AssetUrlParams{type=server, params={a=[1], b=[1, 2]}, path=styles/my.css}', result);
    return true;
};

exports.assetUrlTest_invalidProperty = function () {
    var result = portal.assetUrl({
        _path: 'styles/my.css',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    assert.assertEquals('AssetUrlParams{type=server, params={a=[1], b=[1, 2]}}', result);
    return true;
};

exports.attachmentUrlTest = function () {
    var result = portal.attachmentUrl({
        name: 'myattachment.pdf',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('AttachmentUrlParams{type=server, params={a=[1], b=[1, 2]}, name=myattachment.pdf, download=false}', result);
    return true;
};

exports.attachmentUrlTest_unknownProperty = function () {
    var result = portal.attachmentUrl({
        name: 'myattachment.pdf',
        unknownProperty: 'value',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    assert.assertEquals('AttachmentUrlParams{type=server, params={a=[1], b=[1, 2]}, name=myattachment.pdf, download=false}', result);
    return true;
};

exports.componentUrlTest = function () {
    var result = portal.componentUrl({
        component: 'mycomp',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('ComponentUrlParams{type=server, params={a=[1], b=[1, 2]}, component=mycomp}', result);
    return true;
};

exports.componentUrlTest_unknownProperty = function () {
    var result = portal.componentUrl({
        component: 'mycomp',
        unknownProperty: 'value',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    assert.assertEquals('ComponentUrlParams{type=server, params={a=[1], b=[1, 2]}, component=mycomp}', result);
    return true;
};

exports.imageUrlTest = function () {
    var result = portal.imageUrl({
        id: '123',
        background: 'ffffff',
        quality: 90,
        filter: 'scale(1,1)',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('ImageUrlParams{type=server, params={a=[1], b=[1, 2]}, id=123, quality=90, filter=scale(1,1), background=ffffff}',
        result);
    return true;
};

exports.imageUrlTest_unknownProperty = function () {
    var result = portal.imageUrl({
        id: '123',
        background: 'ffffff',
        quality: 90,
        filter: 'scale(1,1)',
        params: {
            a: 1,
            b: [1, 2]
        },
        unknownProperty: 'value'
    });

    assert.assertEquals('ImageUrlParams{type=server, params={a=[1], b=[1, 2]}, id=123, quality=90, filter=scale(1,1), background=ffffff}',
        result);
    return true;
};

exports.pageUrlTest = function () {
    var result = portal.pageUrl({
        path: 'a/b',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('PageUrlParams{type=server, params={a=[1], b=[1, 2]}, path=a/b}', result);
    return true;
};

exports.pageUrlTest_unknownProperty = function () {
    var result = portal.pageUrl({
        path: 'a/b',
        params: {
            a: 1,
            b: [1, 2]
        },
        unknownProperty: 'value'
    });

    assert.assertEquals('PageUrlParams{type=server, params={a=[1], b=[1, 2]}, path=a/b}', result);
    return true;
};

exports.serviceUrlTest = function () {
    var result = portal.serviceUrl({
        service: 'myservice',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('ServiceUrlParams{type=server, params={a=[1], b=[1, 2]}, service=myservice}', result);
    return true;
};

exports.serviceUrlTest_unknownProperty = function () {
    var result = portal.serviceUrl({
        service: 'myservice',
        unknownProperty: 'value',
        params: {
            a: 1,
            b: [1, 2]
        }
    });

    assert.assertEquals('ServiceUrlParams{type=server, params={a=[1], b=[1, 2]}, service=myservice}', result);
    return true;
};

exports.processHtmlTest = function () {
    var result = portal.processHtml({
        value: '<p><a title="Link tooltip" href="content://3e266eea-9875-4cb7-b259-41ad152f8532" target="_blank">link</a></p>'
    });

    // NOTE: This is not the actual url. Only a mock representation.
    assert.assertEquals('ProcessHtmlParams{type=server, params={}, value=<p><a title="Link tooltip" ' +
                        'href="content://3e266eea-9875-4cb7-b259-41ad152f8532" target="_blank">link</a></p>, imageWidths=[]}', result);
    return true;
};

exports.processHtmlTest_unknownProperty = function () {
    var result = portal.processHtml({
        value: '<p><a title="Link tooltip" href="content://3e266eea-9875-4cb7-b259-41ad152f8532" target="_blank">link</a></p>',
        unknownProperty: 'value'
    });

    assert.assertEquals('ProcessHtmlParams{type=server, params={}, value=<p><a title="Link tooltip" ' +
                        'href="content://3e266eea-9875-4cb7-b259-41ad152f8532" target="_blank">link</a></p>, imageWidths=[]}', result);
    return true;
};

exports.processHtmlTest_imageUrlProcessing = function() {
    var result = portal.processHtml({
        value: '<p><figure class="editor-align-justify">' +
                       '<img alt="Alt text" src="image://3e266eea-9875-4cb7-b259-41ad152f8532"/>' +
                       '<figcaption>Caption text</figcaption></figure></p>',
        imageWidths: [660, 1024]
    });

    assert.assertEquals('ProcessHtmlParams{type=server, params={}, value=<p><figure class="editor-align-justify"><img alt="Alt text" src="image://3e266eea-9875-4cb7-b259-41ad152f8532"/><figcaption>Caption text</figcaption></figure></p>, imageWidths=[660, 1024]}', result);

    return true;
};

exports.imagePlaceholderTest = function () {
    var result = portal.imagePlaceholder({
        width: 32,
        height: 10
    });

    assert.assertEquals(
        'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAKCAYAAADVTVykAAAAEklEQVR42mNgGAWjYBSMgpEOAAUKAAEVKR6qAAAAAElFTkSuQmCC',
        result);
    return true;
};
