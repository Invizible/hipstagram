(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .controller('CommentDetailController', CommentDetailController);

    CommentDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Comment', 'Post', 'User'];

    function CommentDetailController($scope, $rootScope, $stateParams, entity, Comment, Post, User) {
        var vm = this;

        vm.comment = entity;

        var unsubscribe = $rootScope.$on('hipstagramApp:commentUpdate', function(event, result) {
            vm.comment = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
