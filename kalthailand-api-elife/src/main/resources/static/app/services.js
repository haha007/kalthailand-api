(function() {
    'use strict';

    angular.module('myApp').factory('Item', function($resource) {
        return $resource('/RLS/collectionFile');
    });
})();