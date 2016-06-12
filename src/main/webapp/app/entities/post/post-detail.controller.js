(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .controller('PostDetailController', PostDetailController);

    PostDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'post', 'Post', 'Tag', 'User', 'Comment'];

    function PostDetailController($scope, $rootScope, $stateParams, DataUtils, post, Post, Tag, User, Comment) {
        var vm = this;

        vm.post = post;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('hipstagramApp:postUpdate', function(event, result) {
            vm.post = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
