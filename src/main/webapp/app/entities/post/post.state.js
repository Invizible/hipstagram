(function() {
    'use strict';

    angular
        .module('hipstagramApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('post', {
            parent: 'entity',
            url: '/?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'hipstagramApp.post.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/post/posts.html',
                    controller: 'PostController',
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
                    $translatePartialLoader.addPart('post');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('post-detail', {
            parent: 'entity',
            url: '/post/{postId}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'hipstagramApp.post.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/post/post-detail.html',
                    controller: 'PostDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('post');
                    return $translate.refresh();
                }],
                post: ['$stateParams', 'Post', function($stateParams, Post) {
                    return Post.get({id : $stateParams.postId}).$promise;
                }]
            }
        })
        .state('post.new', {
            parent: 'post',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/post/post-dialog.html',
                    controller: 'PostDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        post: function () {
                            return {
                                text: null,
                                photo: null,
                                photoContentType: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('post', null, { reload: true });
                }, function() {
                    $state.go('post');
                });
            }]
        })
        .state('post.edit', {
            parent: 'post',
            url: '/{postId}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/post/post-dialog.html',
                    controller: 'PostDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        post: ['Post', function(Post) {
                            return Post.get({id : $stateParams.postId}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('post', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('post.delete', {
            parent: 'post',
            url: '/{postId}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/post/post-delete-dialog.html',
                    controller: 'PostDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        post: ['Post', function(Post) {
                            return Post.get({id : $stateParams.postId}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('post', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
