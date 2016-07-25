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
        return $resource(window.location.origin + '/api-blacklist/blacklist/upload', {}, {
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
    
    app.factory('PolicyNumberTestUpload', function ($resource) {
        return $resource(window.location.origin + '/api-elife/policy-quota/upload', {}, {
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
        return $resource(window.location.origin + '/api-elife/policies', { pageNumber: '@pageNumber', pageSize: '@pageSize' }, {});
    });

    app.factory('BlackList', function ($resource) {
        return $resource(window.location.origin + '/api-blacklist/blacklist', { pageNumber: '@pageNumber', pageSize: '@pageSize' }, {});
    });
    
    app.factory('PolicyNumberTestService', function ($resource) {
    	return $resource(window.location.origin + '/api-elife/policy-quota/available', { pageNumber: '@pageNumber', pageSize: '@pageSize' }, {});
    });

    app.factory('PolicyDetail', function ($resource) {
        return $resource(window.location.origin + '/api-elife/policies/:id', { id: '@id' }, {});
    });

    app.factory('PolicyNotification', function ($resource) {
        return $resource(window.location.origin + '/api-elife/policies/:id/reminder/:reminderId', { id: '@id', reminderId: '@reminderId' }, {});
    });
    
    app.factory('PolicyQuotaConfig', function ($resource) {
    	return $resource(window.location.origin + '/api-elife/policy-quota/:id', { id: '@rowId' }, {
    		update: {
    			method: 'PUT'
    		}
    	});
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