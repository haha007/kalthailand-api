(function () {
    'use strict';

    var app = angular.module('myApp');
    app.run(function ($localStorage) {
        if (hasValue(ACCESS_TOKEN)) {
            $localStorage.token = ACCESS_TOKEN;
        }
    });
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

    app.controller('TotalQuoteCountController', function ($scope, $route, $http, $localStorage) {
        $scope.$route = $route;

        var aDateAgo = new Date();
        aDateAgo.setDate(new Date().getDate() - 7);
        $scope.toDateSearch = new Date();
        $scope.fromDateSearch = aDateAgo;
        $scope.responseText = null;
        $scope.sessionQuoteCounts = [];

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
            searchForTotalQuoteCount();
        };

        $scope.downloadExcelFile = function () {
            var url = window.location.origin
                + '/api-elife/quotes/all-products/download?'
                + 'fromDate=' + $scope.fromDateSearch.toISOString()
                + '&toDate=' + $scope.toDateSearch.toISOString();
            window.open(url, "_blank");
        }

        function searchForTotalQuoteCount() {
            var fromDate = $scope.fromDateSearch.toISOString();
            var toDate = $scope.toDateSearch.toISOString();
            $http.get(window.location.origin + '/api-elife/quotes/all-products/counts?'
                    + 'fromDate=' + fromDate
                    + '&toDate=' + toDate)
                .then(
                    function (successResponse) {
                        $scope.sessionQuoteCounts = successResponse.data;
                        showChart($scope.sessionQuoteCounts);
                        $scope.successMessage = "Success! Counted from date: '" + $scope.fromDateSearch.toDateString() + "', to date: '" + $scope.toDateSearch.toDateString() + "'";
                        $scope.errorMessage = null;
                    },
                    function (errorResponse) {
                        $scope.sessionQuoteCounts = [];
                        $scope.successMessage = null;
                        $scope.errorMessage = errorResponse.data.userMessage;
                        console.log(errorResponse);
                    }
                );
        }

        function showChart(sessionQuoteCounts) {
            // Radar chart
            var ctx = document.getElementById("total-quote-count-chart-radar");
            var charLabels = getArrayByFields(sessionQuoteCounts, "productId");
            var sessionQuoteCountsValues = getArrayByFields(sessionQuoteCounts, "sessionQuoteCount");
            var quoteCountsValues = getArrayByFields(sessionQuoteCounts, "quoteCount");
            var data = {
                labels: charLabels,
                datasets: [{
                    label: "Session Quotes",
                    backgroundColor: "rgba(3, 88, 106, 0.2)",
                    borderColor: "rgba(3, 88, 106, 0.80)",
                    pointBorderColor: "rgba(3, 88, 106, 0.80)",
                    pointBackgroundColor: "rgba(3, 88, 106, 0.80)",
                    pointHoverBackgroundColor: "#fff",
                    pointHoverBorderColor: "rgba(220,220,220,1)",
                    data: sessionQuoteCountsValues
                }, {
                    label: "Quote",
                    backgroundColor: "rgba(38, 185, 154, 0.2)",
                    borderColor: "rgba(38, 185, 154, 0.85)",
                    pointColor: "rgba(38, 185, 154, 0.85)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(151,187,205,1)",
                    data: quoteCountsValues
                }]
            };

            var canvasRadar = new Chart(ctx, {
                type: 'radar',
                data: data,
            });
        }
    });

    app.controller('PoliciesController', function ($scope, $rootScope, $http, $route, Dashboard, PolicyQuotaConfig, ProductCriteria, $localStorage, $location) {
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
        $scope.productCriteriaList = ProductCriteria.query();

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


        function searchForPolicies() {
            $scope.isSearching = true;
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
                    $scope.isSearching = null;

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
                    $scope.isSearching = null;

                    $scope.policies = null;
                    $scope.errorMessage = errorResponse.data.userMessage;
                    $scope.downloadUrl = null;
                }
            );
        }
    });

    app.controller('CollectionFileController', function ($scope, $route, $http, CollectionFile, $localStorage) {
        $scope.$route = $route;
        $scope.collectionFilesDataTable;

        fetchCollectionFileDetails();

        $scope.file = null;
        $scope.upload = function (event) {
            event.preventDefault();
            $scope.isUploading = true;
            $scope.errorMessage = null;
            var newCollectionFile = new CollectionFile;
            newCollectionFile.file = $scope.file;

            newCollectionFile.$save()
                .then(function (successResponse) {
                    // For successfully state
                    $scope.isUploading = null;
                    $scope.errorMessage = null;
                    fetchCollectionFileDetails();
                })
                .catch(function (errorResponse) {
                    // For error state
                    $scope.isUploading = null;
                    $scope.showErrorMessage(errorResponse.data.userMessage);
                });
        };

        $scope.processLastCollectionFiles = function (event) {
            $scope.isProcessing = true;
            $scope.errorMessage = null;
            $http.get(window.location.origin + '/api-elife/RLS/collectionFile/process', {}).then(
                function (successResponse) {
                    var newProcessedCollectionFiles = successResponse.data;
                    //$scope.collectionFiles = successResponse.data;
                    fetchCollectionFileDetails(function () {
                        $scope.isProcessing = null;
                        if (hasValue(newProcessedCollectionFiles) && newProcessedCollectionFiles.length > 0) {
                            $scope.showSuccessMessage("Processed " + newProcessedCollectionFiles.length + " collection files!")
                        } else {
                            $scope.showInfoMessage("There's no new collection file to process.")
                        }
                    });
                },
                function (errorResponse) {
                    console.log(errorResponse);
                    $scope.isProcessing = null;
                    $scope.showErrorMessage(errorResponse.data.userMessage);
                });
        };

        $scope.isProccessed = function (collectionFile) {
            return collectionFile.deductionFile.lines && collectionFile.deductionFile.lines.length != 0;
        };
        function fetchCollectionFileDetails(callback) {
            CollectionFile.query(function (response) {
                $scope.collectionFiles = response;
                $scope.collectionFilesDataTable = new DataTable($scope.collectionFiles, 5);
                $scope.collectionFilesDataTable.setPage(0);
                if (hasValue(callback)) {
                    callback.call(this);
                }
            });
        }

        $scope.showInfoMessage = function (msg) {
            $scope.infoMessage = msg;
            $scope.successMessage = null;
            $scope.errorMessage = null;
        };
        $scope.showErrorMessage = function (msg) {
            $scope.infoMessage = null;
            $scope.successMessage = null;
            $scope.errorMessage = msg;
        };
        $scope.showSuccessMessage = function (msg) {
            $scope.infoMessage = null;
            $scope.successMessage = msg;
            $scope.errorMessage = null;
        };
    });

    app.controller('PolicyNumbersController', function ($scope, $route, $http, PolicyQuotaConfig, PolicyNumberUpload, $localStorage) {
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

        $scope.timeTriggerList = [{"value": 3600, "name": "1 Time/Hour"}, {"value": 86400, "name": "1 Time/Day"}];

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
            $scope.AtpMode = {
                NO_AUTOPAY: 0,
                AUTOPAY: 1
            };
            $scope.isEnabledAutoPay = function (policy) {
                if (!hasValue(policy)) {
                    return false;
                }
                return policy.premiumsData.financialScheduler.atpMo;
                de == $scope.AtpMode.AUTOPAY;
            };
            $scope.clickShowAllPayments = function () {
                if ($scope.showAllPayments == "checked") {
                    $scope.showAllPayments = null;
                } else {
                    $scope.showAllPayments = "checked";
                }
            };
            $scope.sortPayments = function (fieldNames) {
                if (hasValue($scope.paymentsOrder) && $scope.paymentsOrder == 1) {
                    $scope.paymentsOrder = -1;
                } else {
                    $scope.paymentsOrder = 1;
                }
                var fieldSorts = [];
                for (i = 0; i < fieldNames.length; i++) {
                    fieldSorts.push(new FieldSort(fieldNames[i], $scope.paymentsOrder));
                }
                $scope.policyDetail.payments.sortByFields(fieldSorts);
            };
            $scope.saveMainInsuredPerson = function () {
                var mainInsuredPerson = $scope.policyDetail.insureds[0].person;
                if (!validateInsuredPerson(mainInsuredPerson)) {
                    return;
                }
                $scope.isSavingMainInsured = true;
                $http.post(window.location.origin + '/api-elife/policies/' + $scope.policyDetail.policyId + '/main-insured/person', mainInsuredPerson)
                    .then(
                        function (successResponse) {
                            $scope.isSavingMainInsured = false;
                            $scope.policyDetail = successResponse.data;
                            $scope.showSuccessMessage("Person info of main insured is updated successfully!");
                        },
                        function (errorResponse) {
                            $scope.isSavingMainInsured = false;
                            var errorCode = errorResponse.data.code;
                            var errorMessage = errorResponse.data.userMessage;
                            var errorDetails;
                            if (errorCode == "0010") {
                                errorDetails = ERROR_HANDLER.fieldErrorsToMessages(errorResponse.data.fieldErrors);
                            }
                            $scope.showErrorMessage(errorMessage, errorDetails);
                            console.log(errorResponse);
                        });

            };
            $scope.validateInsuredPerson = function () {
                validateInsuredPerson($scope.policyDetail.insureds[0].person);
            };
            var validateInsuredPerson = function (insuredPerson) {
                var resultEmail = true;
                if (!isNotBlank(insuredPerson.email)) {
                    resultEmail = false;
                    $scope.showFieldErrorMessage("mainInsured.email", "Email is mandatory.")
                } else if (!validateEmail(insuredPerson.email)) {
                    resultEmail = false;
                    $scope.showFieldErrorMessage("mainInsured.email", "Email is invalid.")
                }

                var resultMobile = true;
                if (!isNotBlank(insuredPerson.mobilePhoneNumber.number)) {
                    resultMobile = false;
                    $scope.showFieldErrorMessage("mainInsured.mobile", "Mobile is mandatory.")
                } else if (!validateNumber(insuredPerson.mobilePhoneNumber.number, 10)) {
                    resultMobile = false;
                    $scope.showFieldErrorMessage("mainInsured.mobile", "Mobile must be number with 10 digits.")
                }
                if (resultEmail) {
                    $scope.showFieldErrorMessage("mainInsured.email", null);
                }
                if (resultMobile) {
                    $scope.showFieldErrorMessage("mainInsured.mobile", null);

                }
                var result = resultEmail && resultMobile;
                return result;
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
                        $scope.showErrorMessage(err.data);
                        //$scope.successMessage = null;
                        //$scope.errorMessage = err.toString();
                    }
                )

                return false;
            };
            $scope.stepStatus = function (stepId) {
                var currentStep = $scope.getCurrentStep();
                var styleClass = "disabled";
                var isdone = 0;
                if (currentStep >= stepId) {
                    styleClass = "selected";
                    isdone = 1;
                }
                return {styleClass: styleClass, isdone: isdone};
            };
            $scope.getCurrentStep = function () {
                var policy = $scope.policyDetail;
                if (!hasValue(policy)) {
                    return 0;
                }
                var policyStatus = policy.status;
                if (policyStatus == 'PENDING_PAYMENT') {
                    return 1;
                } else if (policyStatus == 'PENDING_VALIDATION') {
                    return 2;
                } else if (policyStatus == 'VALIDATED') {
                    return 3;
                }
                return 0;
            };
            /**
             * @param reminderId 1: no answer. 2: wrong email
             */
            $scope.onClickNotification = function (reminderId) {
                $scope.isValidating = true;
                PolicyNotification.get({id: $scope.policyID, reminderId: reminderId},
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
                }).then(
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
                        var msg = formatErrorMessage(errorResponse.data);
                        $scope.showErrorMessage(msg);
                        $scope.policyDetail = null;
                        $scope.annualPremium = null;
                        $scope.sumInsured = null;
                        $scope.isValidating = null;
                        $(window).scrollTop(0);
                    }
                );
            };

            $scope.search = function (event) {
                if (hasValue(event)) {
                    event.preventDefault();
                }
                searchForPolicyDetail();
            };
            $scope.getProductDisplayName = function (policyDetail) {
                if (!hasValue(policyDetail)) {
                    return null;
                }
                var productId = policyDetail.commonData.productId;
                var productName;
                if (productId == "iProtect") {
                    productName = 'iProtect S';
                } else {
                    productName = productId;
                }
                return productName;
            }
            function searchForPolicyDetail() {
                if (!isNotBlank($scope.policyID)) {
                    $scope.showErrorMessage("Policy number is required!");
                    return;
                }
                PolicyDetail.get({id: $scope.policyID},
                    function (successResponse) {
                        $scope.scenarioID = 1;
                        $scope.linePayCaptureMode = 'FAKE_WITH_SUCCESS';
                        $scope.showSuccessMessage(null);
                        $scope.policyDetail = successResponse;
                        var productId = successResponse.commonData.productId;
                        var sumInsuredAmount;
                        if (productId == "10EC") {
                            sumInsuredAmount = successResponse.premiumsData.product10ECPremium.sumInsured;
                        } else if (productId == "iProtect") {
                            sumInsuredAmount = successResponse.premiumsData.productIProtectPremium.sumInsured;
                        } else if (productId == "iFine") {
                            sumInsuredAmount = successResponse.premiumsData.productIFinePremium.sumInsured;
                        } else {
                            if (!hasValue(successResponse.premiumsData.premiumDetail)) {
                                $scope.showErrorMessage("Not found detail of this policy (" + productId + ")");
                                return;
                            }
                            sumInsuredAmount = successResponse.premiumsData.premiumDetail.sumInsured;
                        }
                        $scope.sumInsured = sumInsuredAmount;


                        var periodicity = '' + successResponse.premiumsData.financialScheduler.periodicity.code;
                        $scope.periodicity = periodicity;
                        var premium = successResponse.premiumsData.financialScheduler.modalAmount.value;
                        var annualPremiumValue;
                        if (periodicity == 'EVERY_MONTH') {
                            annualPremiumValue = (premium * 12);
                        } else if (periodicity == 'EVERY_QUARTER') {
                            annualPremiumValue = (premium * 4);
                        } else if (periodicity == 'EVERY_HALF_YEAR') {
                            annualPremiumValue = (premium * 2);
                        } else {
                            annualPremiumValue = premium;
                        }
                        $scope.annualPremium = {value: annualPremiumValue, currencyCode: successResponse.premiumsData.financialScheduler.modalAmount.currencyCode};
                    },
                    function (errorResponse) {
                        $scope.showErrorMessage(errorResponse.data.userMessage);
                        $scope.policyDetail = null;
                        $scope.annualPremium = null;
                        $scope.sumInsured = null;
                    });

            }

            $scope.showErrorMessage = function (msg, errorDetails) {
                $scope.successMessage = null;
                $scope.errorMessage = msg;
                $scope.errorDetails = errorDetails;
                $scope.fieldErrorMessages = null;
            }
            $scope.showSuccessMessage = function (msg) {
                $scope.successMessage = msg;
                $scope.errorMessage = null;
                $scope.errorDetails = null;
                $scope.fieldErrorMessages = null;
            }
            $scope.showFieldErrorMessage = function (fieldKey, msg) {
                $scope.successMessage = null;
                $scope.errorMessage = null;
                $scope.errorDetails = null;
                $scope.fieldErrorMessages = $scope.fieldErrorMessages || {};
                $scope.fieldErrorMessages[fieldKey] = msg;
            }
        }
    );

    app.controller('CommissionController', function (CommissionService, $scope, $route, $http, $localStorage) {
        $scope.service = CommissionService;
    });
    app.controller('HealthCheckController', function (HealthCheckService, $scope, $route, $http, $localStorage) {
        $scope.service = HealthCheckService;
        $scope.service.$scope = $scope;
    });
    app.controller('CommissionResultController', function (CommissionResultService, $scope, $route, $http, $localStorage) {
        $scope.service = CommissionResultService;

        var dateNow = new Date();
        var day = dateNow.getDate();
        day = 1;

        // Calculate Button is enable on 1-10 of month
        if (day >= 1 && day <= 10) {
            // 1-10 is false to Enable button
            $scope.calculateButton = false;
            $scope.redNotice = '';
        } else {
            // 11+ is true to Disable button
            $scope.calculateButton = true;
            $scope.redNotice = '(Commission  can only be generated before 10<sup>th</sup> day of a month)';
        }

        $scope.commissionResultAll = CommissionResultService;

        $scope.callGenerateCommission = function () {
            var obj = {'createdDateTime': 'Waiting...'};
            $scope.commissionResultAll.commissionList.splice(0, 0, obj);
            CommissionResultService.generateCommission();
            $scope.loadNewFilter();
        }

        $scope.loadNewFilter = function () {
            $scope.commissionResultAll = CommissionResultService;
            $scope.calculateButton = false;
            $scope.redNotice = '';
//            $scope.redNotice = 'Please wait system is processing for generate reusult ...';
        }


    });
})();

function formatErrorMessage(error) {
    var prefixMessage = error.userMessage;
    var fieldsMessage = "";
    if (hasValue(error.fieldErrors)) {
        for (i = 0; i < error.fieldErrors.length; i++) {
            var field = error.fieldErrors[i];
            var fieldName = field.field;
            var fieldMessage = field.message;
            if (fieldsMessage.length > 0) {
                fieldsMessage += ". ";
            }
            fieldsMessage += fieldMessage;
        }
    }
    return prefixMessage + fieldsMessage;
}