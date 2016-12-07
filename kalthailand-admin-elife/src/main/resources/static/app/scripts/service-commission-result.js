function CommissionResultService($http, $sce) {
    this.$http = $http;
    this.$sce = $sce;

    this.commissionResultAll();
    this.isCalculating = false;
};

CommissionResultService.prototype.commissionResultAll = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
        function (successResponse) {
            self.commissionCalculationSessions = successResponse.data;
            self.showInfoMessage("Loaded commission lists");
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );
};
CommissionResultService.prototype.callGenerateCommission = function () {
    var self = this;
    var obj = {'createdDateTime': 'Waiting...'};
    self.commissionCalculationSessions.splice(0, 0, obj);
    CommissionResultService.generateCommission();
    $scope.loadNewFilter();
};

CommissionResultService.prototype.generateCommission = function (msg) {
    var self = this;
    self.isCalculating = true;

    // API Service
    self.$http.post(window.location.origin + '/api-elife/commissions/calculation', {}).then(
        function (successResponse) {
            // Load after generate
            self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
                function (successResponse) {
                    self.isCalculating = false;
                    self.commissionCalculationSessions = successResponse.data;
                    self.showInfoMessage("Loaded commission lists");
                },
                function (errorResponse) {
                    self.isCalculating = false;
                    self.showErrorMessage(errorResponse.data.userMessage);
                }
            );
        },
        function (errorResponse) {
            self.isCalculating = false;
            self.showErrorMessage(errorResponse.data.userMessage);
        }
    );

    // Load first after click
//    self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
//        function (successResponse) {
//            self.commissionCalculationSessions = successResponse.data;
//            self.showInfoMessage("Loaded commission lists");
//        },
//        function (errorResponse) {
//            self.showErrorMessage(errorResponse.data);
//        }
//    );

};

CommissionResultService.prototype.showInfoMessage = function (msg) {
    var self = this;
    self.infoMessage = msg;
    self.successMessage = null;
    self.errorMessage = null;
};


CommissionResultService.prototype.showErrorMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = null;
    self.errorMessage = hasValue(msg) ? self.$sce.trustAsHtml(msg) : null;
    console.log(msg);
};
