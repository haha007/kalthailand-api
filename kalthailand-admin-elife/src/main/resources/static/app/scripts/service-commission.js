function CommissionService($http) {
    this.$http = $http;

    this.commissionPlans = undefined;
    this.findAllCommissionPlans();
}
CommissionService.prototype.findAllCommissionPlans = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/commissions/plans', {}).then(
        function (successResponse) {
            self.commissionPlans = successResponse.data;
            self.successMessage = "Loaded commission plans";
        },
        function (errorResponse) {
            self.errorMessage = errorResponse.data;
            console.log(errorResponse);
        }
    );
}
