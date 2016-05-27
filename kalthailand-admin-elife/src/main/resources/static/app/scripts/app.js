(function () {
    'use strict';

    var app = angular.module('myApp', [
        'ngResource',
        'file-model',
        'ngStorage',
        'ngRoute',
        'ui.bootstrap'
    ]);


    app.config(routeConfig);

    app.run(runFn);

    function routeConfig($routeProvider, $locationProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'app/templates/login.html',
                controller: 'LoginController'
            })
            .when('/home', {
                templateUrl: 'app/templates/home.html',
                controller: 'DashboardController',
                data: {
                    authorizedRoles: ['ELIFE_ADMIN']
                },
                activeTab: 'home'
            })
            .when('/blacklist', {
                templateUrl: 'app/templates/blacklist.html',
                controller: 'BlackListController',
                data: {
                    authorizedRoles: ['ELIFE_ADMIN']
                },
                activeTab: 'blacklist'
            })
            .when('/collection-file', {
                templateUrl: 'app/templates/collection-file.html',
                controller: 'CollectionFileController',
                data: {
                    authorizedRoles: ['ELIFE_ADMIN']
                },
                activeTab: 'collection-file'
            })
            .when('/policy-detail', {
                templateUrl: 'app/templates/policy-detail.html',
                controller: 'PolicyDetailController',
                data: {
                    authorizedRoles: ['ELIFE_ADMIN']
                },
                activeTab: 'policy-detail'
            })
            .otherwise({
                redirectTo: '/home'
            });

        // use the HTML5 History API
        $locationProvider.html5Mode(true);

    }

    function runFn($rootScope, AuthService, $location) {

        $rootScope.$on('$routeChangeStart', routeChangeStart);

        function routeChangeStart(event, next) {

            var authorizedRoles = [];

            if (next.data === null || typeof next.data === 'undefined') {
                return;
            }

            authorizedRoles = next.data.authorizedRoles;

            if (!AuthService.isAuthorized(authorizedRoles)) {
                event.preventDefault();

                if (AuthService.isAuthenticated()) {
                    // User is not authorised but authenticated
                    console.log('Not authorised but logged in');
                    $location.path('/');
                } else {
                    // User is not logged in
                    console.log('User not logged in');
                    $rootScope.errorExists = true;
                    $location.path('/');
                    setTimeout(function () {
                        $rootScope.$apply(function () {
                            $rootScope.errorExists = false;
                        });
                    }, 4000);
                }
            }
        }
    }

} ());