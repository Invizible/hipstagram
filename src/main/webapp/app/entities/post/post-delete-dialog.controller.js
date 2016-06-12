(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .controller('PostDeleteController',PostDeleteController);

    PostDeleteController.$inject = ['$uibModalInstance', 'post', 'Post'];

    function PostDeleteController($uibModalInstance, post, Post) {
        var vm = this;

        vm.post = post;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Post.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
