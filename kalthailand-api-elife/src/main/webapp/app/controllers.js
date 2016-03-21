(function() {
    'use strict';

    var app = angular.module('myApp');

    app.controller('AppController', function($scope, Item) {
        Item.query(function(response) {
            $scope.collectionFiles = response;
        });

        $scope.file = null;
        $scope.upload = function(event) {
            event.preventDefault();
            var newItem = new Item;
            newItem.file = $scope.file;

            newItem.$save()
                .then(function(resp) {
                    // For successfully state
                    console.log(resp);
                    $scope.errorMessage = null;
                })
                .catch(function(resp) {
                    // For error state
                    console.log(resp);
                    console.log(resp.data);
                    console.log(resp.data.userMessage);
                    $scope.errorMessage = resp.data.userMessage;
                });
        }
    });

    app.controller('ValidationController', function($scope, PolicyDetail) {
        $scope.search = function(event) {
            event.preventDefault();
            PolicyDetail.get({id: $scope.policyID},
                function (successResponse) {
                    $scope.errorMessage = null;
                    $scope.policyDetail = successResponse;
                },
                function (errorResponse) {
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.policyDetail = null;
                });
        };
    });
})();