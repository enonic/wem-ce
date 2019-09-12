var repoLib = require('/lib/xp/repo.js');
var t = require('/lib/xp/testing');

// BEGIN
// Retrieves the list of repositories
var result = repoLib.list();
log.info(result.length + ' repositories found');
// END

// BEGIN
// Repositories retrieved.
var expected = [{
    "id": "test-repo",
    "branches": [
        "master"
    ],
    settings: {},
    data: {}
}, {
    "id": "another-repo",
    "branches": [
        "master"
    ],
    settings: {},
    data: {}
}];
// END
t.assertJsonEquals(expected, result);
