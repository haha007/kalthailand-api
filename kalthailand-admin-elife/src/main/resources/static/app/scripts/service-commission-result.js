function CommissionResultService($http) {
    this.$http = $http;
    //
    this.commissionResultAll();
};

CommissionResultService.prototype.commissionResultAll = function () {
    var self = this;
    
    self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
        function (successResponse) {
            self.commissionList = successResponse.data;
            self.showInfoMessage("Loaded commission lists");
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );
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

CommissionResultService.prototype.generateCommission = function (msg) {
    var self = this;
    // API Service
    self.$http.post(window.location.origin + '/api-elife/commissions/calculation', {}).then(
    	function (successResponse) {
    		
    		// Load after generate
    		self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
		        function (successResponse) {
		            self.commissionList = successResponse.data;
		            self.showInfoMessage("Loaded commission lists");
		        },
		        function (errorResponse) {
		            self.showErrorMessage(errorResponse.data);
		        }
		    );
    	},
    	function (errorResponse) {
    		self.showErrorMessage(errorResponse.data);
    	}
    );
    
    // Load first after click
//    self.$http.get(window.location.origin + '/api-elife/commissions/calculation/lists', {}).then(
//        function (successResponse) {
//            self.commissionList = successResponse.data;
//            self.showInfoMessage("Loaded commission lists");
//        },
//        function (errorResponse) {
//            self.showErrorMessage(errorResponse.data);
//        }
//    );
    
};