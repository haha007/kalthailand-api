(function () {
    'use strict';

    var app = angular.module('myApp');

    app.controller('MainController', function ($rootScope, $scope) {
        $rootScope.accessErrorMessage = null;
    });

    app.controller('AccessRightsDashboardController', function ($rootScope, $scope, AccessRightsDashboard) {
        AccessRightsDashboard.get(
            {},
            function (successResponse) {
                $rootScope.accessErrorMessage = null;
            },
            function (errorResponse) {
                $rootScope.accessErrorMessage = errorResponse.data.userMessage ? errorResponse.data.userMessage : errorResponse.data.message;
            })
    });

    app.controller('AccessRightsAutopayController', function ($rootScope, $scope, AccessRightsAutopay) {
        AccessRightsAutopay.get(
            {},
            function (successResponse) {
                $rootScope.accessErrorMessage = null;
            },
            function (errorResponse) {
                $rootScope.accessErrorMessage = errorResponse.data.userMessage ? errorResponse.data.userMessage : errorResponse.data.message;
            })
    });

    app.controller('AccessRightsValidationController', function ($rootScope, $scope, AccessRightsValidation) {
        AccessRightsValidation.get(
            {},
            function (successResponse) {
                $rootScope.accessErrorMessage = null;
            },
            function (errorResponse) {
                $rootScope.accessErrorMessage = errorResponse.data.userMessage ? errorResponse.data.userMessage : errorResponse.data.message;
            })
    });

    app.controller('AppController', function ($rootScope, $scope, Item) {
        Item.query(function (response) {
            $scope.collectionFiles = response;
        });

        $scope.file = null;
        $scope.upload = function (event) {
            event.preventDefault();
            var newItem = new Item;
            newItem.file = $scope.file;

            newItem.$save()
                .then(function (successResponse) {
                    // For successfully state
                    $scope.errorMessage = null;
                })
                .catch(function (errorResponse) {
                    // For error state
                    $scope.errorMessage = errorResponse.data.userMessage;
                });
        }
    });

    app.controller('ValidationController', function ($rootScope, $scope, $http, PolicyDetail) {
        $scope.onClickValidate = function (policyNumber) {
            $scope.isValidating = true;
            $http({
                url: 'policies/' + policyNumber + '/update/status/validated',
                method: 'PUT'
            })
            .then(
                function (successResponse) {
                    $scope.successMessage = "Policy [" + successResponse.data.policyId + "] has been validated";
                    $scope.errorMessage = null;
                    $scope.policyDetail = null;
                    $scope.annualPremium = null;
                    $scope.sumInsured = null;
                    $scope.isValidating = null;
                },
                function (errorResponse) {
                    $scope.successMessage = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.policyDetail = null;
                    $scope.annualPremium = null;
                    $scope.sumInsured = null;
                    $scope.isValidating = null;
                });
        };

        $scope.search = function (event) {
            event.preventDefault();
            PolicyDetail.get({id: $scope.policyID},
                function (successResponse) {
                    $scope.errorMessage = null;
                    $scope.successMessage = null;
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
                        $scope.annualPremium = (premium * 4) + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    } else if (periodicity == 'EVERY_HALF_YEAR') {
                        $scope.annualPremium = (premium * 2) + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    }
                    else {
                        $scope.annualPremium = premium + " " + successResponse.premiumsData.financialScheduler.modalAmount.currencyCode;
                    }
                },
                function (errorResponse) {
                    $scope.successMessage = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.policyDetail = null;
                    $scope.annualPremium = null;
                    $scope.sumInsured = null;
                });
        };
    });
})();