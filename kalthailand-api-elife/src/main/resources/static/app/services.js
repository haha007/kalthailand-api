(function() {
    'use strict';

    var app = angular.module('myApp');

    app.factory('Item', function($resource) {
        return $resource('RLS/collectionFile', {}, {
            save: {
                method: 'POST',
                transformRequest: function(data) {
                    var formData = new FormData();

                    angular.forEach(data, function(value, key) {
                        formData.append(key, value);
                    });

                    return formData;
                },
                headers: {
                    'Content-Type': undefined,
                    enctype:'multipart/form-data'
                }
            }
        });
    });

    app.factory('PolicyDetail', function($resource) {
        return $resource('admin/policies/validate/:id', {id : '@id'}, {});
    });

    app.factory('PolicyReminder', function($resource) {
        return $resource('admin/policies/:id/reminder/:reminderId', {id : '@id', reminderId : '@reminderId'}, {});
    });

    app.factory('AccessRightsDashboard', function($resource) {
        return $resource('admin/check/access/dashboard', {}, {});
    });

    app.factory('AccessRightsAutopay', function($resource) {
        return $resource('admin/check/access/autopay', {}, {});
    });

    app.factory('AccessRightsValidation', function($resource) {
        return $resource('admin/check/access/validation', {}, {});
    });
})();