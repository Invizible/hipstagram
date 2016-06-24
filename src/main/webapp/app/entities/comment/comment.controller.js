(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .controller('CommentController', CommentController);

    CommentController.$inject = ['$scope', '$state', 'Comment', 'CommentSearch', 'ParseLinks', 'AlertService', 'pagingParams', 'paginationConstants', 'post', 'Principal'];

    function CommentController ($scope, $state, Comment, CommentSearch, ParseLinks, AlertService, pagingParams, paginationConstants, post, Principal) {
        var vm = this;
        
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.clear = clear;
        vm.search = search;
        vm.loadAll = loadAll;
        vm.searchQuery = pagingParams.search;
        vm.currentSearch = pagingParams.search;

        Principal.identity().then(function(account) {
            vm.currentAccount = account;
        });

        loadAll();

        function loadAll () {
            if (pagingParams.search) {
                CommentSearch.queryByPostId({
                    query: pagingParams.search,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort(),
                    postId: post.id                  
                }, onSuccess, onError);
            } else {
                Comment.queryByPostId({
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort(),
                    postId: post.id
                }, onSuccess, onError);
            }
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.comments = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage (page) {
            vm.page = page;
            vm.transition();
        }

        function transition () {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch,
                postId: post.id
            });
        }

        function search (searchQuery) {
            if (!searchQuery){
                return vm.clear();
            }
            vm.links = null;
            vm.page = 1;
            vm.predicate = '_score';
            vm.reverse = false;
            vm.currentSearch = searchQuery;
            vm.transition();
        }

        function clear () {
            vm.links = null;
            vm.page = 1;
            vm.predicate = 'id';
            vm.reverse = true;
            vm.currentSearch = null;
            vm.transition();
        }
    }
})();
