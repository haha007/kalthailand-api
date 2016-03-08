(function() {
    'use strict';

    angular.module('myApp')
        .controller('AppController', function($scope, Item) {
            Item.query(function(response) {
                console.log(response);
                $scope.collectionFiles = response;
            });
        });
})();