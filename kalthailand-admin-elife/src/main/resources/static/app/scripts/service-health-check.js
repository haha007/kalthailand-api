function HealthCheckService($http, $sce, $scope) {
    this.$http = $http;
    this.$scope = $scope;
    this.$sce = $sce;
    this.metrics = undefined;//Will be loaded from server
    this.showHealth();
    this.loadSetting();
};
HealthCheckService.prototype.showHealth = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/system-health', {}).then(
        function (successResponse) {
            self.metrics = successResponse.data;
            self.showInfoMessage("Health status!");
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );

};

HealthCheckService.prototype.addWarningEmail = function ($event) {
    $event.preventDefault();
    var self = this;
    self.systemHealthSetting.warningEmails.push(new String(""));
};
HealthCheckService.prototype.removeWarningEmail = function (item) {
    var self = this;
    self.systemHealthSetting.warningEmails.remove(new String(item));
};
HealthCheckService.prototype.loadSetting = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/system/health/setting', {}).then(
        function (successResponse) {
            self.setSystemHealthSetting(successResponse.data);
        },
        function (errorResponse) {
            var msg = formatErrorMessage(errorResponse.data);
            self.showErrorMessage(msg);
        }
    );
};
HealthCheckService.prototype.saveSetting = function () {
    var self = this;
    if (self.$scope.systemHealthSettingForm.$invalid) {
        self.showErrorMessage("Invalid inputs.");
        return;
    }
    var warningEmailsArray = self.systemHealthSetting.warningEmailsString.splitToNotBlankValues();
    if (!validateEmails(warningEmailsArray).isValid()) {
        self.showErrorMessage("Invalid emails.");
        return;
    }
    self.systemHealthSetting.warningEmails = warningEmailsArray;
    self.$http.put(window.location.origin + '/api-elife/system/health/setting', self.systemHealthSetting).then(
        function (successResponse) {
            self.setSystemHealthSetting(successResponse.data);
            self.showSuccessMessage("Saved!");
        },
        function (errorResponse) {
            var errorCode = errorResponse.data.code;
            var errorMessage = errorResponse.data.userMessage;
            var errorDetails;
            if (errorCode == "0010") {
                errorDetails = ERROR_HANDLER.fieldErrorsToMessages(errorResponse.data.fieldErrors);
            }
            self.showErrorMessage(errorMessage, errorDetails);
        }
    );
};
HealthCheckService.prototype.setSystemHealthSetting = function (systemHealthSetting) {
    var self = this;
    self.systemHealthSetting = systemHealthSetting;
    self.systemHealthSetting.warningEmailsString = self.systemHealthSetting.warningEmails.mergeNotBlankValuesToString();
};
HealthCheckService.prototype.showInfoMessage = function (msg) {
    var self = this;
    self.infoMessage = msg;
    self.successMessage = null;
    self.errorMessage = null;
    self.errorDetails = null;
};
HealthCheckService.prototype.showSuccessMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = msg;
    self.errorMessage = null;
    self.errorDetails = null;
};
HealthCheckService.prototype.showErrorMessage = function (msg, errorDetails) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = null;
    self.errorMessage = hasValue(msg) ? self.$sce.trustAsHtml(msg) : null;
    self.errorDetails = errorDetails;
    console.log(msg);
};


