(function() {
    'use strict';
    angular
        .module('hipstagramApp')
        .factory('Post', Post);

    Post.$inject = ['$resource', 'DateUtils'];

    function Post ($resource, DateUtils) {
        var resourceUrl =  'api/posts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.date = DateUtils.convertDateTimeFromServer(data.date);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
