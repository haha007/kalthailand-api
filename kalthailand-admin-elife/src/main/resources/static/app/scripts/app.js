(function () {
    'use strict';

    var app = angular.module('myApp', [
        'ngResource',
        'file-model',
        'ngStorage',
        'ngRoute',
        'ui.bootstrap',
        'ngMask'
    ]);

    app.config(routeConfig);

    app.run(runFn);

    function routeConfig($routeProvider) {
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

    }

    function runFn($rootScope, $location, $templateCache) {
        $rootScope.$on('$routeChangeStart', routeChangeStart);
        
        function routeChangeStart(event, next, current) {
        	$templateCache.remove('app/templates/partials/sidebar.html');
        	$templateCache.remove('app/templates/home.html');
            if (next.data === null || typeof next.data === 'undefined') {
                return;
            }

            $location.path('/');
        }
    }

} ());