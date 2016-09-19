function CommissionResultService($http) {
    this.$http = $http;
    //
    this.commissionResultAll();
};

CommissionResultService.prototype.commissionResultAll = function () {
    var self = this;
    
//    self.$http.get(window.location.origin + '/api-elife/commissions/plans', {}).then(
//        function (successResponse) {
//            self.commissionPlans = successResponse.data;
//            self.showInfoMessage("Loaded commission plans");
//            self.validateCommissionPlans();
//        },
//        function (errorResponse) {
//            self.showErrorMessage(errorResponse.data);
//        }
//    );
    
    var mockData = [
         			{'generateDate':'2016-08-03 12:11:12', 'commisionOfMonth':'201607', 'numberOfPolicy':'11', 'pathLinkDownload':'abc201607.xlsx'}
        			,{'generateDate':'2016-07-03 12:11:12', 'commisionOfMonth':'201606', 'numberOfPolicy':'8', 'pathLinkDownload':'abc201606.xlsx'}
        			,{'generateDate':'2016-06-03 12:11:12', 'commisionOfMonth':'201605', 'numberOfPolicy':'16', 'pathLinkDownload':'abc201605.xlsx'}
        			,{'generateDate':'2016-05-03 12:11:12', 'commisionOfMonth':'201604', 'numberOfPolicy':'9', 'pathLinkDownload':'abc201604.xlsx'}
        			,{'generateDate':'2016-04-03 12:11:12', 'commisionOfMonth':'201603', 'numberOfPolicy':'5', 'pathLinkDownload':'abc201603.xlsx'}
        			,{'generateDate':'2016-03-03 12:11:12', 'commisionOfMonth':'201602', 'numberOfPolicy':'6', 'pathLinkDownload':'abc201602.xlsx'}
        		];
    
    self.allData = mockData;
};