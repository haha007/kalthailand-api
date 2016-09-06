function CommissionService($http, ProductCriteria) {
    this.$http = $http;
    this.commissionPlans = undefined;//Will be loaded from server
    this.commissionGroupTypes = ['FY', 'OV'];//View {@link CommissionTargetGroupType}.
    this.commissionTargetEntityTypes = ['AFFILIATE', 'COMPANY', 'TSR', 'MKR', 'DISTRIBUTION'];//View {@link CommissionTargetEntityType}.
    this.customerCategories = [
        {value: "EXISTING", label: "Existing"}
        , {value: "NEW", label: "New"}
    ];

    this.products = ProductCriteria.query();
    this.findAllCommissionPlans();
};
CommissionService.prototype.isCommissionPlanReadonly = function (commissionPlan) {
    return hasValue(commissionPlan.id);
};
CommissionService.prototype.findAllCommissionPlans = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/commissions/plans', {}).then(
        function (successResponse) {
            self.commissionPlans = successResponse.data;
            self.showSuccessMessage("Loaded commission plans");
            self.validateCommissionPlans();
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );
};
CommissionService.prototype.showSuccessMessage = function (msg) {
    var self = this;
    self.successMessage = msg;
    self.errorMessage = null;
};
CommissionService.prototype.showErrorMessage = function (msg) {
    var self = this;
    self.successMessage = null;
    self.errorMessage = msg;
    console.log(msg);
};
CommissionService.prototype.saveAllCommissionPlans = function () {
    var self = this;
    if (!self.validateCommissionPlans()) {
        return;
    }
    self.$http.post(window.location.origin + '/api-elife/commissions/plans', self.commissionPlans).then(
        function (successResponse) {
            self.commissionPlans = successResponse.data;
            self.successMessage = "Saved commission plans";
        },
        function (errorResponse) {
            self.errorMessage = errorResponse.data;
            console.log(errorResponse);
        }
    );
};
CommissionService.prototype.addCommissionPlan = function () {
    var self = this;
    var newCommissionPlan = self.constructCommissionPlan();
    self.commissionPlans.push(newCommissionPlan);
};
CommissionService.prototype.removeCommissionPlan = function (commissionPlan) {
    var self = this;
    self.commissionPlans.remove(commissionPlan);
};
CommissionService.prototype.validateCommissionPlans = function () {
    var self = this;
    var isSuccess = true;
    for (var i = 0; i < self.commissionPlans.length; i++) {
        var commissionPlan = self.commissionPlans[i];
        isSuccess = self.validateCommissionPlan(commissionPlan);
        if (!isSuccess) {
            break;
        }
    }
    if (isSuccess) {
        self.showSuccessMessage("Input numbers are OK!")
    }
    return isSuccess;
};
CommissionService.prototype.validateCommissionPlan = function (commissionPlan) {
    var self = this;
    var isSuccess = true;
    isSuccess = self.checkHasRequiredInputsInCommissionPlan(commissionPlan);
    if (!isSuccess) {
        self.showErrorMessage("Some commission plans are missing some inputs.");
        self.statusStyleCommissionPlan(commissionPlan, isSuccess);
        return isSuccess;
    }

    var commissionTargetGroups = commissionPlan.targetGroups;
    for (var i = 0; i < commissionTargetGroups.length; i++) {
        var commissionTargetGroup = commissionTargetGroups[i];
        var sumPercentage = self.sumPercentageInCommissionGroup(commissionTargetGroup);
        if (sumPercentage != 100) {
            var msg = "Invalid commission: " + commissionPlan.unitCode + "-" + commissionPlan.planCode + "-" + commissionPlan.customerCategory + ": Group '" + commissionTargetGroup.targetGroupType + "'. Sum percentage: " + sumPercentage;
            self.showErrorMessage(msg);
            isSuccess = false;
            break;
        }
    }
    self.statusStyleCommissionPlan(commissionPlan, isSuccess);
    return isSuccess;
};
CommissionService.prototype.checkHasRequiredInputsInCommissionPlan = function (commissionPlan) {
    return (isNotBlank(commissionPlan.unitCode) && isNotBlank(commissionPlan.planCode) && isNotBlank(commissionPlan.customerCategory));
};

CommissionService.prototype.statusStyleCommissionPlan = function (commissionPlan, validateResult) {
    var self = this;
    if (validateResult) {
        commissionPlan.styleClass = "";
    } else {
        commissionPlan.styleClass = "has-error";
    }
};
CommissionService.prototype.sumPercentageInCommissionGroup = function (commissionTargetGroup) {
    var commissionTargetEntities = commissionTargetGroup.targetEntities;
    var sumPercentage = 0;
    for (var i = 0; i < commissionTargetEntities.length; i++) {
        var commissionTargetEntity = commissionTargetEntities[i];
        sumPercentage += +commissionTargetEntity.percentage;
    }
    return (sumPercentage);
}
//CONSTRUCT COMMISSION PLANS ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
CommissionService.prototype.constructCommissionPlan = function () {
    var self = this;

    var groupTypes = self.commissionGroupTypes;
    var commissionTargetGroups = [];
    for (var i = 0; i < groupTypes.length; i++) {
        var groupType = groupTypes[i];
        commissionTargetGroups.push(self.constructCommissionGroups(groupType));
    }

    return {
        unitCode: null
        , planCode: null
        , customerCategory: null
        , targetGroups: commissionTargetGroups
    };
};
CommissionService.prototype.constructCommissionGroups = function (groupType) {
    var self = this;
    return {
        targetGroupType: groupType
        , targetEntities: self.constructCommissionTargetEntities()
    };
};
CommissionService.prototype.constructCommissionTargetEntities = function () {
    var self = this;
    var targetEntityTypes = self.commissionTargetEntityTypes;
    var targetEntities = [];
    for (var i = 0; i < targetEntityTypes.length; i++) {
        var targetEntityType = targetEntityTypes[i];
        var targetEntity = {
            targetEntityType: targetEntityType
            , percentage: 0.0
        };
        targetEntities.push(targetEntity);
    }
    return targetEntities;
};
//HELPER METHODS TO SHOW COMMISSION TARGET ENTITIES /////////////////////////////////////////////////////////////////////////
CommissionService.prototype.findCommissionGroupInPlan = function (commissionPlan, groupType) {
    var self = this;
    var targetGroups = commissionPlan.targetGroups;
    for (var i = 0; i < targetGroups.length; i++) {
        var targetGroup = targetGroups[i];
        if (targetGroup.targetGroupType == groupType) {
            return targetGroup;
        }
    }
    return null;
};
CommissionService.prototype.findCommissionTargetEntityInGroup = function (commissionGroup, entityType) {
    var self = this;
    var targetEntities = commissionGroup.targetEntities;
    for (var i = 0; i < targetEntities.length; i++) {
        var targetEntity = targetEntities[i];
        if (targetEntity.targetEntityType == entityType) {
            return targetEntity;
        }
    }
    return null;
};
CommissionService.prototype.findCommissionTargetEntityInPlan = function (commissionPlan, groupType, entityType) {
    var self = this;
    var targetGroup = self.findCommissionGroupInPlan(commissionPlan, groupType);
    if (targetGroup == null) return null;
    return self.findCommissionTargetEntityInGroup(targetGroup, entityType);
};
