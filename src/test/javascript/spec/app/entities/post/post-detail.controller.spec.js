'use strict';

describe('Controller Tests', function() {

    describe('Post Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPost, MockTag, MockUser, MockComment;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPost = jasmine.createSpy('MockPost');
            MockTag = jasmine.createSpy('MockTag');
            MockUser = jasmine.createSpy('MockUser');
            MockComment = jasmine.createSpy('MockComment');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Post': MockPost,
                'Tag': MockTag,
                'User': MockUser,
                'Comment': MockComment
            };
            createController = function() {
                $injector.get('$controller')("PostDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'hipstagramApp:postUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
