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
                activeTab: 'home'
            })
            .when('/blacklist', {
                templateUrl: 'app/templates/blacklist.html',
                controller: 'BlackListController',
                activeTab: 'blacklist'
            })
            .when('/collection-file', {
                templateUrl: 'app/templates/collection-file.html',
                controller: 'CollectionFileController',
                activeTab: 'collection-file'
            })
            .when('/policy-detail', {
                templateUrl: 'app/templates/policy-detail.html',
                controller: 'PolicyDetailController',
                activeTab: 'policy-detail'
            })
            .otherwise({
                redirectTo: '/home'
            });

        // use the HTML5 History API
        $locationProvider.html5Mode(true);
    }

    function runFn($rootScope, $location) {
        $rootScope.$on('$routeChangeStart', routeChangeStart);

        function routeChangeStart(event, next) {
            if (next.data === null || typeof next.data === 'undefined') {
                return;
            }

            $location.path('/');
        }
    }

} ());