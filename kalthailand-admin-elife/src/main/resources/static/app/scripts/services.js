(function () {
    'use strict';

    var app = angular.module('myApp');

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

    app.factory('ValidateToken', function ($resource, $localStorage) {
        return $resource(window.location.origin + '/api-auth/auth/validate/' + $localStorage.role, {}, {
            check: {
                method: 'GET',
                headers: {
                    'Authorization': $localStorage.token
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

    app.factory('AuthService', function ($http, $localStorage) {
        var authService = {};

        // authService.login = login;
        authService.isAuthenticated = isAuthenticated;
        authService.isAuthorized = isAuthorized;
        // authService.logout = logout;

        // function login(credentials) {
        //     return $http
        //         .post('/user/login', credentials)
        //         .then(function (res) {
        //             $localStorage.token = res.data.itokend;
        //             $localStorage.role = 'ELIFE_ADMIN';//res.data.user.role;

        //             return res.data.user;
        //         });
        // }

        // function logout() {
        //     return $http
        //         .post('/api/user/logout')
        //         .then(function (res) {
        //             $localStorage.token = null;
        //             $localStorage.role = null;

        //             return true;
        //         });
        // }

        function isAuthenticated() {
            return !!$localStorage.token;
        }

        function isAuthorized(authorizedRoles) {
            if (!angular.isArray(authorizedRoles)) {
                authorizedRoles = [authorizedRoles];
            }

            return (authService.isAuthenticated() && authorizedRoles.indexOf($localStorage.role) !== -1);
        }

        return authService;
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