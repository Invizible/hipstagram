(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('post-detail.comment', {
            url: '/comment?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'hipstagramApp.comment.home.title'
            },
            views: {
                'comments': {
                    templateUrl: 'app/entities/comment/comments.html',
                    controller: 'CommentController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('comment');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('comment-new', {
            parent: 'post-detail.comment',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', 'post', function($stateParams, $state, $uibModal, post) {
                $uibModal.open({
                    templateUrl: 'app/entities/comment/comment-dialog.html',
                    controller: 'CommentDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        comment: function () {
                            return {
                                text: null,
                                id: null
                            };
                        },
                        post: post
                    }
                }).result.then(function() {
                    $state.go('post-detail.comment', null, { reload: true });
                }, function() {
                    $state.go('post-detail.comment');
                });
            }]
        })
        .state('comment-edit', {
            parent: 'post-detail.comment',
            url: '/{commentId}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', 'post', function($stateParams, $state, $uibModal, post) {
                $uibModal.open({
                    templateUrl: 'app/entities/comment/comment-dialog.html',
                    controller: 'CommentDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        comment: ['Comment', function(Comment) {
                            return Comment.get({id : $stateParams.commentId}).$promise;
                        }],
                        post: post
                    }
                }).result.then(function() {
                    $state.go('post-detail.comment', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('comment-delete', {
            parent: 'post-detail.comment',
            url: '/{commentId}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/comment/comment-delete-dialog.html',
                    controller: 'CommentDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        comment: ['Comment', function(Comment) {
                            return Comment.get({id : $stateParams.commentId}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('post-detail.comment', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
