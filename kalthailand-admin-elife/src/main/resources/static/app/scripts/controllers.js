(function () {
    'use strict';

    var app = angular.module('myApp');

    app.controller('LoginController', function ($scope, $rootScope, $http, $localStorage, $location) {
        $localStorage.token = null;

        $scope.onClickLogin = function () {
            var requestForToken = {};
            requestForToken.userName = $scope.username;
            requestForToken.password = $scope.password;

            $http.post(window.location.origin + '/api-auth/auth', requestForToken)
                .then(
                    function (successResponse) {
                        $rootScope.errorMsg = null;
                        $localStorage.token = successResponse.data.token;
                        $location.path('/home');
                    },
                    function (errorResponse) {
                        console.log(errorResponse);
                        $rootScope.errorMsg = 'Invalid credentials. Please check username/password.'
                    });
        };
    });

    app.controller('DashboardController', function ($scope, $rootScope , $http, $route, Dashboard, PolicyQuotaConfig, $localStorage, $location) {
        $scope.$route = $route;

        $scope.currentPage = 1;
        $scope.itemsPerPage = 20;
        $scope.searchContent = '';
        $scope.policies = null;
        $scope.policyIdSearch = null;
        $scope.productTypeSearch = null;
        $scope.statusSearch = null;
        $scope.nonEmptyAgentCodeSearch = null;

        var aMonthAgo = new Date();
        aMonthAgo.setMonth(new Date().getMonth() - 1);
        $scope.toDateSearch = new Date();
        $scope.fromDateSearch = aMonthAgo;

        $scope.search = searchForPolicies;
        $scope.pageChanged = searchForPolicies;
        searchForPolicies();
        fetchPolicyQuotaInfo();

        $scope.dateOptions = {
            dateDisabled: false,
            formatYear: 'yyyy',
            maxDate: new Date(),
            startingDay: 1
        };

        $scope.fromDateSearchOpen = function () {
            $scope.fromDateSearch.opened = true;
        };

        $scope.toDateSearchOpen = function () {
            $scope.toDateSearch.opened = true;
        };

        $scope.search = function (event) {
            event.preventDefault();
            searchForPolicies();
        };
        
        function fetchPolicyQuotaInfo() {
        	
        	var callCount = 0;
        	var policyQuota;
        	var availablePolicyCount
        	var triggerPercent;
        	
        	PolicyQuotaConfig
        	.get({id: 0},
                function (successResponse) {
                    triggerPercent = successResponse.triggerPercent;
                    done();
                },
                function (errorResponse) {
                    console.log(errorResponse.data.userMessage);
                    done();
                });
        	
            $http.get(window.location.origin + '/api-elife/policy-numbers/count', {}).then(
                function (successResponse) {
                    policyQuota = successResponse.data;
                    done();
                },
                function (errorResponse) {
                    console.log(errorResponse);
                    done();
                });

            $http.get(window.location.origin + '/api-elife/policy-numbers/available/count', {}).then(
                function (successResponse) {
                    availablePolicyCount = successResponse.data;
                    done();
                },
                function (errorResponse) {
                    console.log(errorResponse);
                    done();
                });
            
            function done(){
            	if(++callCount === 3) {
            		
            		if(!availablePolicyCount) {
            			console.log('Unable to fetch availablePolicyCount');
            			return;
            		}
            		if(!policyQuota) {
            			console.log('Unable to fetch policyQuota');
            			return;
            		}
            		if(!triggerPercent) {
            			console.log('Unable to fetch triggerPercent');
            			return;
            		}
            		
            		var usagePercent = (policyQuota - availablePolicyCount) / policyQuota * 100;
            		
            		if( usagePercent >= triggerPercent) {
            			$scope.policyAlmostFull = true;
            		}
            		
            		return;
            	}
            }

        }


        function searchForPolicies() {
            Dashboard.get(
                {
                    pageNumber: $scope.currentPage - 1,
                    pageSize: $scope.itemsPerPage,
                    policyId: $scope.policyIdSearch,
                    productType: $scope.productTypeSearch,
                    status: $scope.statusSearch,
                    nonEmptyAgentCode: $scope.nonEmptyAgentCodeSearch,
                    fromDate: $scope.fromDateSearch,
                    toDate: $scope.toDateSearch
                },
                function (successResponse) {
                    $scope.totalPages = successResponse.totalPages;
                    $scope.totalItems = successResponse.totalElements;
                    $scope.currentPage = successResponse.number + 1;

                    $scope.policies = successResponse;
                    $scope.errorMessage = null;
                    $scope.downloadUrl = window.location.origin + '/api-elife/policies/extract/download?';
                    if ($scope.policyIdSearch) {
                        $scope.downloadUrl += 'policyId=' + $scope.policyIdSearch;
                    }
                    if ($scope.productTypeSearch) {
                        $scope.downloadUrl += '&productType=' + $scope.productTypeSearch;
                    }
                    if ($scope.statusSearch) {
                        $scope.downloadUrl += '&status=' + $scope.statusSearch;
                    }
                    if ($scope.nonEmptyAgentCodeSearch) {
                        $scope.downloadUrl += '&nonEmptyAgentCode=' + $scope.nonEmptyAgentCodeSearch;
                    }
                    if ($scope.fromDateSearch) {
                        $scope.downloadUrl += '&fromDate=' + $scope.fromDateSearch.toISOString();
                    }
                    if ($scope.toDateSearch) {
                        $scope.downloadUrl += '&toDate=' + $scope.toDateSearch.toISOString();
                    }
                },
                function (errorResponse) {
                    $scope.policies = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.downloadUrl = null;
                }
            );
        }
    });

    app.controller('CollectionFileController', function ($scope, $route, CollectionFile, $localStorage) {
        $scope.$route = $route;


        fetchCollectionFileDetails();

        $scope.file = null;
        $scope.upload = function (event) {
            event.preventDefault();
            var newCollectionFile = new CollectionFile;
            newCollectionFile.file = $scope.file;

            newCollectionFile.$save()
                .then(function (successResponse) {
                    // For successfully state
                    $scope.errorMessage = null;
                    fetchCollectionFileDetails();
                })
                .catch(function (errorResponse) {
                    // For error state
                    $scope.errorMessage = errorResponse.data.userMessage;
                });
        }

        function fetchCollectionFileDetails() {
            CollectionFile.query(function (response) {
                $scope.collectionFiles = response;
            });
        }
    });

    app.controller('ConfigurationController', function ($scope, $route, $http, PolicyQuotaConfig, PolicyNumberUpload, $localStorage) {
        $scope.$route = $route;
        $scope.settings = {};

        $scope.uploadProgress = null;
        $scope.numberOfLines = 0;
        $scope.numberOfLinesAdded = 0;
        $scope.numberOfDuplicateLines = 0;
        $scope.numberOfEmptyLines = 0;

        $scope.stacked = [];
        $scope.stacked.push({value: 0, type: 'success'});
        $scope.stacked.push({value: 0, type: 'warning'});
        $scope.stacked.push({value: 0, type: 'info'});

        var stompClient = null;
        var nbLinesAdded = 0;

        fetchPolicyQuotaInfo();
        
        $scope.timeTriggerList = [{"value":3600,"name":"1 Time/Hour"},{"value":86400,"name":"1 Time/Day"}];

        $scope.uploadNewPolicyNumbers = function (event) {
            event.preventDefault();
            $scope.isUploading = true;
            $scope.hasUploaded = true;
            connect();
            var newPolicyNumberFileUpload = new PolicyNumberUpload;
            newPolicyNumberFileUpload.file = $scope.file;

            newPolicyNumberFileUpload.$save()
                .then(function (successResponse) {
                    $scope.errorMessage = null;
                    $scope.isUploading = null;
                    disconnect();
                    updateProgressBar(angular.fromJson(successResponse), true);
                    fetchPolicyQuotaInfo();
                })
                .catch(function (errorResponse) {
                    $scope.uploadErrorMessage = errorResponse.data.userMessage;
                    $scope.isUploading = null;
                    $scope.hasUploaded = false;
                    disconnect();
                    fetchPolicyQuotaInfo();
                });
        };

        function connect() {
            var socket = new SockJS(window.location.origin + '/api-elife/adminwebsocket/policy-numbers/upload/progress');
            stompClient = Stomp.over(socket);
            stompClient.debug = null
            stompClient.connect({}, function (frame) {
                stompClient.subscribe('/topic/policy-numbers/upload/progress/result', function (response) {
                    updateProgressBar(angular.fromJson(response.body), false);
                });
            });
        }

        function disconnect() {
            if (stompClient != null) {
                stompClient.disconnect();
            }
        }

        function updateProgressBar(uploadProgress, lastCall) {
            $scope.numberOfLinesAdded = uploadProgress.numberOfLinesAdded;
            $scope.numberOfDuplicateLines = uploadProgress.numberOfDuplicateLines;
            $scope.numberOfEmptyLines = uploadProgress.numberOfEmptyLines;
            $scope.numberOfLines = uploadProgress.numberOfLines;
            $scope.stacked[0] = {value: $scope.numberOfLinesAdded, type: 'success'};
            $scope.stacked[1] = {value: $scope.numberOfDuplicateLines, type: 'warning'};
            $scope.stacked[2] = {value: $scope.numberOfEmptyLines, type: 'info'};

            if (!lastCall) {
                $scope.$apply();
            }
        }

        $scope.updateQuotaAlert = function ($event) {

            if ($scope.settings.emailList) {
                $scope.settings.emailList = $scope.settings.emailList.replace(/(\r\n|\n|\r| )/gm, "").split(',');
            }

            PolicyQuotaConfig.update($scope.settings,
                function (successResponse) {
                    $scope.errorMessage = null;

                    $scope.settings.triggerPercent = successResponse.triggerPercent;
                    $scope.settings.emailList = '';
                    $scope.settings.rowId = successResponse.rowId;
                    $scope.settings.timeTrigger = successResponse.timeTrigger;

                    successResponse.emailList.forEach(function (email) {
                        $scope.settings.emailList += email;
                        $scope.settings.emailList += ',\n';
                    });

                    if (successResponse.emailList.length > 0) {
                        $scope.settings.emailList = $scope.settings.emailList.slice(0, -2);
                    }

                },
                function (errorResponse) {
                    $scope.errorMessage = errorResponse.data.userMessage;
                });


        }

        PolicyQuotaConfig.get({id: 0},
            function (successResponse) {
                $scope.errorMessage = null;

                $scope.settings.triggerPercent = successResponse.triggerPercent;
                $scope.settings.emailList = '';
                $scope.settings.rowId = successResponse.rowId;
                $scope.settings.timeTrigger = successResponse.timeTrigger;

                successResponse.emailList.forEach(function (email) {
                    $scope.settings.emailList += email;
                    $scope.settings.emailList += ',\n';
                });

                if (successResponse.emailList.length > 0) {
                    $scope.settings.emailList = $scope.settings.emailList.slice(0, -2);
                }

            },
            function (errorResponse) {
                console.log(errorResponse.data.userMessage);
            });

        function fetchPolicyQuotaInfo() {
            $http.get(window.location.origin + '/api-elife/policy-numbers/count', {}).then(
                function (successResponse) {
                    $scope.policyQuota = successResponse.data;

                },
                function (errorResponse) {
                    console.log(errorResponse);
                });

            $http.get(window.location.origin + '/api-elife/policy-numbers/available/count', {}).then(
                function (successResponse) {

                    $scope.availablePolicyCount = successResponse.data;

                },
                function (errorResponse) {
                    console.log(errorResponse);
                });

        }


    });

    app.controller('BlackListController', function ($scope, $route, BlackList, BlackListFileUpload) {
        $scope.$route = $route;

        $scope.currentPage = 1;
        $scope.itemsPerPage = 20;
        $scope.searchContent = '';
        $scope.blackList = null;
        $scope.uploadProgress = null;
        $scope.numberOfLines = 0;
        $scope.numberOfLinesAdded = 0;
        $scope.numberOfDuplicateLines = 0;
        $scope.numberOfEmptyLines = 0;

        $scope.stacked = [];
        $scope.stacked.push({value: 0, type: 'success'});
        $scope.stacked.push({value: 0, type: 'warning'});
        $scope.stacked.push({value: 0, type: 'info'});

        $scope.search = searchForBlackList;
        $scope.pageChanged = searchForBlackList;
        searchForBlackList();

        var stompClient = null;
        var nbLinesAdded = 0;

        $scope.uploadBlackList = function (event) {
            event.preventDefault();
            $scope.isUploading = true;
            $scope.hasUploaded = true;
            connect();
            $scope.blackList = null;
            var newBlackListFileUpload = new BlackListFileUpload;
            newBlackListFileUpload.file = $scope.file;

            newBlackListFileUpload.$save()
                .then(function (successResponse) {
                    $scope.errorMessage = null;
                    $scope.isUploading = null;
                    disconnect();
                    updateProgressBar(angular.fromJson(successResponse), true);
                    searchForBlackList();
                })
                .catch(function (errorResponse) {
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.isUploading = null;
                    $scope.hasUploaded = false;
                    disconnect();
                });
        };

        function searchForBlackList() {
            BlackList.get(
                {
                    pageNumber: $scope.currentPage - 1,
                    pageSize: $scope.itemsPerPage,
                    searchContent: $scope.searchContent
                },
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
        }

        function connect() {
            var socket = new SockJS(window.location.origin + '/api-blacklist/adminwebsocket/blacklist/upload/progress');
            stompClient = Stomp.over(socket);
            stompClient.debug = null
            stompClient.connect({}, function (frame) {
                stompClient.subscribe('/topic/blacklist/upload/progress/result', function (response) {
                    updateProgressBar(angular.fromJson(response.body), false);
                });
            });
        }

        function disconnect() {
            if (stompClient != null) {
                stompClient.disconnect();
            }
        }

        function updateProgressBar(uploadProgress, lastCall) {
            $scope.numberOfLinesAdded = uploadProgress.numberOfLinesAdded;
            $scope.numberOfDuplicateLines = uploadProgress.numberOfDuplicateLines;
            $scope.numberOfEmptyLines = uploadProgress.numberOfEmptyLines;
            $scope.numberOfLines = uploadProgress.numberOfLines;
            ;
            $scope.stacked[0] = {value: $scope.numberOfLinesAdded, type: 'success'};
            $scope.stacked[1] = {value: $scope.numberOfDuplicateLines, type: 'warning'};
            $scope.stacked[2] = {value: $scope.numberOfEmptyLines, type: 'info'};

            if (!lastCall) {
                $scope.$apply();
            }
        }
    });

    app.controller('PolicyDetailController', function ($scope, $route, $http, $routeParams, PolicyDetail, PolicyNotification) {
        $scope.$route = $route;
        $scope.apiELifeUrl = window.location.origin + '/api-elife';

        $scope.policyID = $routeParams.policyID;
        if ($scope.policyID) {
            searchForPolicyDetail();
        }

        // AKT-820
        $scope.onSubmitPaymentDetails = function () {
            $scope.isFetching = true;

            var data = {
                paymentId: $scope.policyDetail.payments[0].paymentId,
                value: $scope.policyDetail.payments[0].amount.value,
                currencyCode: $scope.policyDetail.payments[0].amount.currencyCode,
                channelType: 'LINE',
                orderId: $scope.payment.orderId,
                transactionId: $scope.payment.transactionId
            };

            if ($scope.policyDetail.premiumsData.financialScheduler.periodicity.code == 'EVERY_MONTH') {
                if (!$scope.payment.regKey) {
                    alert('Error! required "regKey" on monthly mode payment');
                    return;
                }

                data.regKey = $scope.payment.regKey;
            }

            $http({
                url: '/api-elife/policies/' + $scope.policyDetail.policyId + '/update/status/pendingValidation',
                method: 'PUT',
                params: data,
                data: data,
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).
            then(
                function (response) {
                    if (response.data.error) {
                        $scope.isFetching = false;
                        $scope.successMessage = null;
                        $scope.errorMessage = response.data.message;
                    } else {
                        window.location.reload();
                    }
                },
                function (err) {
                    $scope.isFetching = false;
                    $scope.successMessage = null;
                    $scope.errorMessage = err.toString();
                }
            )

            return false;
        };

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
                url: '/api-elife/policies/' + policyNumber + '/update/status/validated',
                method: 'PUT',
                data: $.param({agentName: $scope.agentName, agentCode: $scope.agentCode, linePayCaptureMode: $scope.linePayCaptureMode}),
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
            searchForPolicyDetail();
        };

        function searchForPolicyDetail() {
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
        }
    });
})();