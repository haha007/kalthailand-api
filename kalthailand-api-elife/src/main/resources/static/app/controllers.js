(function() {
    'use strict';

    angular.module('myApp')
        .controller('AppController', function($scope, Item) {
            Item.query(function(response) {
                console.log(response);
                $scope.collectionFiles = response;
            });

            $scope.file = null;
            $scope.upload = function(event) {
                event.preventDefault();
                var newItem = new Item;
                newItem.file = $scope.file;
                newItem.$save();
            }
        });
})();