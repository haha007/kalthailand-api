(function () {
    'use strict';

    var app = angular.module('myApp');

    app.factory('CollectionFile', function ($resource) {
        return $resource('RLS/collectionFile', {}, {
            save: {
                method: 'POST',
                transformRequest: function (data) {
                    var formData = new FormData();

                    angular.forEach(data, function (value, key) {
                        formData.append(key, value);
                    });

                    return formData;
                },
                headers: {
                    'Content-Type': undefined,
                    enctype: 'multipart/form-data'
                }
            }
        });
    });


    app.factory('BlackListFile', function ($resource) {
        return $resource('admin/blackList', {pageNumber:'@pageNumber', pageSize:'@pageSize'}, {});
    });

    app.factory('BlackListFileUpload', function ($resource) {
        return $resource('admin/blackList', {}, {
            save: {
                method: 'POST',
                transformRequest: function (data) {
                    var formData = new FormData();

                    angular.forEach(data, function (value, key) {
                        formData.append(key, value);
                    });

                    return formData;
                },
                headers: {
                    'Content-Type': undefined,
                    enctype: 'multipart/form-data'
                }
            }
        });
    });

    app.factory('PolicyDetail', function ($resource) {
        return $resource('admin/policies/:id', {id: '@id'}, {});
    });

    app.factory('PolicyNotification', function ($resource) {
        return $resource('admin/policies/:id/reminder/:reminderId', {id: '@id', reminderId: '@reminderId'}, {});
    });

})();