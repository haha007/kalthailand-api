(function() {
    'use strict';

    var app = angular.module('myApp');

    app.controller('MainController', function($rootScope, $scope) {
        $rootScope.errorMessage = null;
    });

    app.controller('AccessRightsController', function($rootScope, $scope, AccessRights) {
        AccessRights.get(
            {},
            function(successResponse) {
                console.log("success");
                $rootScope.errorMessage = null;
            },
            function(errorResponse) {
                console.log("error");
                $rootScope.errorMessage = errorResponse.data.userMessage;
            })
    });

    app.controller('AppController', function($scope, Item) {
        Item.query(function(response) {
            $scope.collectionFiles = response;
        });

        $scope.file = null;
        $scope.upload = function(event) {
            event.preventDefault();
            var newItem = new Item;
            newItem.file = $scope.file;

            newItem.$save()
                .then(function(resp) {
                    // For successfully state
                    console.log(resp);
                    $scope.errorMessage = null;
                })
                .catch(function(resp) {
                    // For error state
                    console.log(resp);
                    console.log(resp.data);
                    console.log(resp.data.userMessage);
                    $scope.errorMessage = resp.data.userMessage;
                });
        }
    });

    app.controller('ValidationController', function($scope, PolicyDetail) {
        $scope.search = function(event) {
            event.preventDefault();
            PolicyDetail.get({id: $scope.policyID},
                function (successResponse) {
                    $scope.errorMessage = null;
                    $scope.policyDetail = successResponse;
                    if (successResponse.premiumsData.product10ECPremium) {
                        $scope.sumInsured = successResponse.premiumsData.product10ECPremium.sumInsured.value + " " + successResponse.premiumsData.product10ECPremium.sumInsured.currencyCode;
                    }
                    else {
                        $scope.sumInsured = successResponse.premiumsData.productIFinePremium.sumInsured.value + " " + successResponse.premiumsData.productIFinePremium.sumInsured.currencyCode;
                    }
                    var periodicity = '' + successResponse.premiumsData.financialScheduler.periodicity.code;
                    $scope.periodicity = periodicity;
                    var premium = successResponse.premiumsData.financialScheduler.modalAmount.value;
                    if (periodicity == 'EVERY_MONTH') {
                        $scope.annualPremium = (premium * 12) + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    } else if (periodicity == 'EVERY_QUARTER') {
                        $scope.annualPremium =  (premium * 4) + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    } else if (periodicity == 'EVERY_HALF_YEAR') {
                        $scope.annualPremium =  (premium * 2) + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    }
                    else {
                        $scope.annualPremium =  premium + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    }
                },
                function (errorResponse) {
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.policyDetail = null;
                });
        };
    });
})();