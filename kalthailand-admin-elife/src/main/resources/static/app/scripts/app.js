(function () {
    'use strict';

    var app = angular.module('myApp', [
        //'ngSanitize',
        'ngResource',
        'file-model',
        'ngStorage',
        'ngRoute',
        'ui.bootstrap',
        'ngMask'
    ]);

    app.config(routeConfig);

    app.run(runFn);

    //GLOBAL ERROR HANDLER ////////////////////////////////////////////////////////////////////////
    app.factory('errorHttpInterceptor', ['$q', '$rootScope', function ($q, $rootScope) {
            return {
                responseError: function responseError(rejection) {
                    //TODO Maybe 403 is for authorization only. For Session expired, only need to check 401 status.
                    if (rejection.status == 403) {
                        //It does not throw you to the login page immediately because you may need to capture some progressing input data.
                        $rootScope.globalMessage = "Your session is expired! Please login again.";
                    } else if (rejection.status == 401) {
                        $rootScope.globalMessage = "Your session is expired. Please login again!";
                    }
                    return $q.reject(rejection);
                }
            };
        }])
        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('errorHttpInterceptor');
        }]);
    ////////////////////////////////////////////////////////////////////////////////////////////////

    function routeConfig($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'app/templates/home.html',
                controller: 'DashboardController',
                activeTab: 'home'
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
            .when('/total-quote-count', {
                templateUrl: 'app/templates/total-quote-count.html',
                controller: 'TotalQuoteCountController',
                activeTab: 'total-quote-count'
            })
            .when('/commission', {
                templateUrl: 'app/templates/commission.html',
                controller: 'CommissionController',
                activeTab: 'commission'
            })
            .when('/commission-result', {
                templateUrl: 'app/templates/commission-result.html',
                controller: 'CommissionResultController',
                activeTab: 'commission-result'
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


    app.directive("limitTo", [function () {
        return {
            restrict: "A",
            link: function (scope, elem, attrs) {
                var limit = parseInt(attrs.limitTo);
                angular.element(elem).on("keypress", function (e) {
                    if (this.value.length == limit) e.preventDefault();
                });
            }
        }
    }]);
}());