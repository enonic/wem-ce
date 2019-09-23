/* global __*/

/**
 * Node repository related functions.
 *
 * @example
 * var repoLib = require('/lib/xp/repo');
 *
 * @module repo
 */

function checkRequiredValue(value, name) {
    if (value === undefined) {
        throw 'Parameter \'' + name + '\' is required';
    }
}

function checkRequiredParams(params, names) {
    for (const name of names) {
        if (Object.prototype.hasOwnProperty.call(params, name) === false) {
            throw 'Parameter \'' + name + '\' is required';
        }
    }
}

/**
 * Refresh the data for the given index-type in the current repository.
 *
 * @example-ref examples/repo/refresh.js
 *
 * @param {object?} params JSON with the parameters.
 * @param {string} [params.mode='all'] Index type to be refreshed. Possible values: 'all' | 'search' | 'storage'.
 * @param {string} [params.repo='com.enonic.cms.default'] Repository id: 'com.enonic.cms.default' | 'system-repo'. Default is the current repository set in portal.
 * @param {string} [params.repo='branch'=master] Branch. Default is the current repository set in portal.
 *
 */
exports.refresh = function (params) {
    const bean = __.newBean('com.enonic.xp.lib.repo.RefreshHandler');
    params = params || {};
    bean.mode = __.nullOrValue(params.mode);
    bean.repoId = __.nullOrValue(params.repo);
    bean.branch = __.nullOrValue(params.branch);
    bean.refresh();
};

/**
 @typedef IndexDefinition
 @type {object}
 @property {object} [settings] - Index definition settings.
 @property {object} [mapping] - Index definition settings.
 */
/**
 * Creates a repository
 *œ
 * @example-ref examples/repo/create.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.id Repository ID.
 * @param {array} [params.rootPermissions] Array of root permissions.
 * By default, all permissions to 'system.admin' and read permission to 'system.authenticated'
 * @param {string} [params.rootChildOrder] Root child order.
 * @param {object} [params.settings] Repository settings.
 * @param {object} [params.settings.definitions] Index definitions.
 * @param {IndexDefinition} [params.settings.definitions.search] Search index definition.
 * @param {IndexDefinition} [params.settings.definitions.version] Version index definition.
 * @param {IndexDefinition} [params.settings.definitions.branch] Branch indexes definition.
 *
 * @returns {object} Repository created as JSON.
 *
 */
exports.create = function (params) {
    checkRequiredParams(params, ['id']);
    const bean = __.newBean('com.enonic.xp.lib.repo.CreateRepositoryHandler');
    bean.repositoryId = params.id;
    bean.rootPermissions = params.rootPermissions ? __.toScriptValue(params.rootPermissions) : null;
    bean.rootChildOrder = __.nullOrValue(params.rootChildOrder);
    bean.indexDefinitions = params.settings && params.settings.definitions ? __.toScriptValue(params.settings.definitions) : null;
    return __.toNativeObject(bean.execute());
};

/**
 * Deletes a repository
 *
 * @example-ref examples/repo/delete.js
 *
 * @param {string} id Repository ID.
 * @return {boolean} true if deleted, false otherwise.
 *
 */
exports.delete = function (id) {
    checkRequiredValue(id, 'id');
    const bean = __.newBean('com.enonic.xp.lib.repo.DeleteRepositoryHandler');
    bean.repositoryId = id;
    return bean.execute();
};

/**
 * Retrieves the list of repositories
 *
 * @example-ref examples/repo/list.js
 * @return {object} The repositories (as JSON array).
 *
 */
exports.list = function () {
    const bean = __.newBean('com.enonic.xp.lib.repo.ListRepositoriesHandler');
    return __.toNativeObject(bean.execute());
};


/**
 * Retrieves a repository
 *
 * @example-ref examples/repo/get.js
 *
 * @param {string} id Repository ID.
 * @return {object} The repository (as JSON).
 *
 */
exports.get = function (id) {
    checkRequiredValue(id, 'id');
    const bean = __.newBean('com.enonic.xp.lib.repo.GetRepositoryHandler');
    bean.repositoryId = id;
    return __.toNativeObject(bean.execute());
};

/**
 * Creates a branch
 *
 * @example-ref examples/repo/createBranch.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.branchId Branch ID.
 * @param {string} params.repoId Repository where the branch should be created.
 * @return {object} The branch (as JSON).
 *
 */
exports.createBranch = function (params) {
    checkRequiredParams(params, ['repoId', 'branchId']);
    const bean = __.newBean('com.enonic.xp.lib.repo.CreateBranchHandler');
    bean.branchId = params.branchId;
    bean.repoId = params.repoId;
    return __.toNativeObject(bean.execute());
};

/**
 * Deletes a branch
 *
 * @example-ref examples/repo/deleteBranch.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.branchId Branch ID.
 * @param {string} params.repoId Repository where the branch should be deleted.
 * @return {object} The branch (as JSON).
 *
 */
exports.deleteBranch = function (params) {
    checkRequiredParams(params, ['repoId', 'branchId']);
    const bean = __.newBean('com.enonic.xp.lib.repo.DeleteBranchHandler');
    bean.branchId = params.branchId;
    bean.repoId = params.repoId;
    return __.toNativeObject(bean.execute());
};

/**
 * Updates a repository
 *
 * @example-ref examples/repo/update.js
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.id Repository ID.
 * @param {object} [params.data] Array of attachments.
 * @param {array} [params.attachments] Array of attachments.
 *
 * @returns {object} Repository updated as JSON.
 *
 */
exports.updateRepository = function (params) {
    checkRequiredParams(params, ['id']);
    const bean = __.newBean('com.enonic.xp.lib.repo.UpdateRepositoryHandler');
    bean.id = params.id;
    bean.data = __.toScriptValue(params.data);
    bean.attachments = __.nullOrValue(params.attachments);
    return __.toNativeObject(bean.execute());
};