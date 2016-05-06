(function () {
    'use strict';

    var app = angular.module('myApp');

    app.controller('AppController', function ($scope, CollectionFile) {
        CollectionFile.query(function (response) {
            $scope.collectionFiles = response;
        });

        $scope.file = null;
        $scope.upload = function (event) {
            event.preventDefault();
            var newCollectionFile = new CollectionFile;
            newCollectionFile.file = $scope.file;

            newCollectionFile.$save()
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

    app.controller('BlackListController', function ($scope, BlackListFile) {
        $scope.currentPage = 1;
        $scope.itemsPerPage = 20;
        $scope.searchContent = '';

        $scope.search = function() {
            BlackListFile.get(
                {pageNumber: $scope.currentPage - 1, pageSize: $scope.itemsPerPage, searchContent: $scope.searchContent},
                function (successResponse) {
                    $scope.totalPages = successResponse.totalPages;
                    $scope.totalItems = successResponse.totalElements;
                    $scope.currentPage = successResponse.number + 1;

                    $scope.blackList = successResponse;
                    $scope.errorMessage = null;
                },
                function (errorResponse) {
                    $scope.blackList = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                }
            );
        };

        $scope.pageChanged = function() {
            BlackListFile.get(
                {pageNumber: $scope.currentPage - 1, pageSize: $scope.itemsPerPage, searchContent: $scope.searchContent},
                function (successResponse) {
                    $scope.totalPages = successResponse.totalPages;
                    $scope.totalItems = successResponse.totalElements;
                    $scope.currentPage = successResponse.number + 1;

                    $scope.blackList = successResponse;
                    $scope.errorMessage = null;
                },
                function (errorResponse) {
                    $scope.blackList = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                }
            );
        };

        BlackListFile.get(
            {pageNumber: $scope.currentPage - 1, pageSize: $scope.itemsPerPage, searchContent: $scope.searchContent},
            function (successResponse) {
                $scope.totalPages = successResponse.totalPages;
                $scope.totalItems = successResponse.totalElements;
                $scope.currentPage = successResponse.number + 1;

                $scope.blackList = successResponse;
                $scope.errorMessage = null;
            },
            function (errorResponse) {
                $scope.blackList = null;
                $scope.errorMessage = errorResponse.data.userMessage;
            }
        );
    });

    app.controller('DetailController', function ($scope, $http, PolicyDetail, PolicyNotification) {
        $scope.onClickNotification = function () {
            $scope.isValidating = true;
            PolicyNotification.get({id: $scope.policyID, reminderId: $scope.scenarioID},
                function (successResponse) {
                    $scope.successMessage = "Notifications have been sent successfully";
                    $scope.errorMessage = null;
                    $scope.isValidating = null;
                    $(window).scrollTop(0);
                },
                function (errorResponse) {
                    $scope.successMessage = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.isValidating = null;
                    $(window).scrollTop(0);
                });
        };

        $scope.onClickValidate = function (policyNumber) {
            $scope.isValidating = true;
            $http({
                url: 'policies/' + policyNumber + '/update/status/validated',
                method: 'PUT',
                data: $.param({ agentCode: $scope.agentCode, linePayCaptureMode: $scope.linePayCaptureMode }),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .then(
                function (successResponse) {
                    $scope.successMessage = "Policy [" + successResponse.data.policyId + "] has been validated";
                    $scope.errorMessage = null;
                    $scope.policyDetail = null;
                    $scope.annualPremium = null;
                    $scope.sumInsured = null;
                    $scope.isValidating = null;
                    $(window).scrollTop(0);
                },
                function (errorResponse) {
                    $scope.successMessage = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.policyDetail = null;
                    $scope.annualPremium = null;
                    $scope.sumInsured = null;
                    $scope.isValidating = null;
                    $(window).scrollTop(0);
                });
        };

        $scope.search = function (event) {
            event.preventDefault();
            PolicyDetail.get({id: $scope.policyID},
                function (successResponse) {
                    $scope.scenarioID = 1;
                    $scope.linePayCaptureMode = 'FAKE_WITH_SUCCESS';
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