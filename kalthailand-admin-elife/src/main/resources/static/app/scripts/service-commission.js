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
            self.successMessage = "Loaded commission plans";
        },
        function (errorResponse) {
            self.errorMessage = errorResponse.data;
            console.log(errorResponse);
        }
    );
};

CommissionService.prototype.saveAllCommissionPlans = function () {
    var self = this;
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
//HELPER METHODS TO SHOW COMMISSION TARGET ENTITIES ####################################################
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
