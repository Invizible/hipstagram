(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .controller('CommentDeleteController',CommentDeleteController);

    CommentDeleteController.$inject = ['$uibModalInstance', 'comment', 'Comment'];

    function CommentDeleteController($uibModalInstance, comment, Comment) {
        var vm = this;

        vm.comment = comment;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Comment.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
