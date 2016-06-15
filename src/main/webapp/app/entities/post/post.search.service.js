(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .factory('PostSearch', PostSearch);

    PostSearch.$inject = ['$resource'];

    function PostSearch($resource) {
        var resourceUrl =  'api/_search/posts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
