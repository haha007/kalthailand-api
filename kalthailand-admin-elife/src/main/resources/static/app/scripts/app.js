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
            .when('/configuration', {
                templateUrl: 'app/templates/configuration.html',
                controller: 'ConfigurationController',
                activeTab: 'configuration'
            })
            .otherwise({
                redirectTo: '/home'
            });

    }

    function runFn($rootScope, $location, $templateCache, AuthService) {
        $rootScope.$on('$routeChangeStart', routeChangeStart);
        
        function routeChangeStart(event, next, current) {
        	
        	// Remove template caching to accept server side rules ->
        	$templateCache.remove('app/templates/partials/sidebar.html');
        	$templateCache.remove('app/templates/home.html');
        	// Remove template caching to accept server side rules <-
            
        	if (!AuthService.isAuthenticated()) {
                // User is not logged in
                console.log('User not logged in');
                $rootScope.errorMsg = 'Session expired. Please login again.';
                $location.path('/');
                setTimeout(function () {
                    $rootScope.$apply(function () {
                        $rootScope.errorMsg = null;
                    });
                }, 4000);
            }
        }
    }

} ());