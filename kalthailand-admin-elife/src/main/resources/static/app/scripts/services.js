(function () {
    'use strict';

    var app = angular.module('myApp');
    
    app.factory('AuthService', function ($http, $localStorage) {
        var authService = {};

        authService.isAuthenticated = isAuthenticated;

        function isAuthenticated() {
            return !!$localStorage.token;
        }

        return authService;
    });

    app.factory('CollectionFile', function ($resource) {
        return $resource(window.location.origin + '/api-elife/RLS/collectionFile', {}, {
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

    app.factory('BlackListFileUpload', function ($resource) {
        return $resource(window.location.origin + '/api-elife/admin/blackList/upload', {}, {
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

    app.factory('Dashboard', function ($resource) {
        return $resource(window.location.origin + '/api-elife/admin/policies', { pageNumber: '@pageNumber', pageSize: '@pageSize' }, {});
    });

    app.factory('BlackList', function ($resource) {
        return $resource(window.location.origin + '/api-elife/admin/blackList', { pageNumber: '@pageNumber', pageSize: '@pageSize' }, {});
    });

    app.factory('PolicyDetail', function ($resource) {
        return $resource(window.location.origin + '/api-elife/admin/policies/:id', { id: '@id' }, {});
    });

    app.factory('PolicyNotification', function ($resource) {
        return $resource(window.location.origin + '/api-elife/admin/policies/:id/reminder/:reminderId', { id: '@id', reminderId: '@reminderId' }, {});
    });

    app.factory('httpRequestInterceptor', function ($localStorage) {
        return {
            request: function (config) {
                config.headers.Authorization = $localStorage.token || '';
                return config;
            }
        };
    });

    app.config(function ($httpProvider) {
        $httpProvider.interceptors.push('httpRequestInterceptor');
    });

})();