function HealthCheckService($http, $sce) {
    this.$http = $http;
    this.$sce = $sce;
    this.metrics = undefined;//Will be loaded from server
    this.usedMemPercentage = undefined;
    this.showHealth();
};
HealthCheckService.prototype.showHealth = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/system-health', {}).then(
        function (successResponse) {
            self.metrics = successResponse.data;
            //var totalMem = self.metrics.totalMemory;
            //var maxMem = self.metrics.maxMemory;
            //var freeMem = self.metrics.freeMemory;
            //self.usedMemoryPercentage = self.metrics.usedMemoryPercentage;
            //self.usedSpacePercentage = self.metrics.usedSpacePercentage;
            self.showInfoMessage("Health status!");
            //self.validateCommissionPlans();
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );

};
HealthCheckService.prototype.showInfoMessage = function (msg) {
    var self = this;
    self.infoMessage = msg;
    self.successMessage = null;
    self.errorMessage = null;
};
HealthCheckService.prototype.showSuccessMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = msg;
    self.errorMessage = null;
};
HealthCheckService.prototype.showErrorMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = null;
    self.errorMessage = hasValue(msg) ? self.$sce.trustAsHtml(msg) : null;
    console.log(msg);
};


