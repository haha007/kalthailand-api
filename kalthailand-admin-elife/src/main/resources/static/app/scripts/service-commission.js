function CommissionService($http, $sce, ProductCriteria) {
    this.$http = $http;
    this.$sce = $sce;
    this.commissionPlans = undefined;//Will be loaded from server
    this.commissionGroupTypes = [
        {value: 'FY', label: 'FY', isRequired: true},
        {value: 'OV', label: 'OV', isRequired: false}
    ];//View {@link CommissionTargetGroupType}.
    this.commissionTargetEntityTypes = ['AFFILIATE', 'COMPANY', 'TSR', 'MKR', 'DISTRIBUTION'];//View {@link CommissionTargetEntityType}.
    this.customerCategories = [
        {value: "EXISTING", label: "Existing"}
        , {value: "NEW", label: "New"}
    ];
    //this.products = ProductCriteria.query();
    this.findAllCommissionPlans();
    this.isValidateSuccess = undefined;
};
CommissionService.prototype.isCommissionPlanReadonly = function (commissionPlan) {
    return hasValue(commissionPlan.id);
};
CommissionService.prototype.findAllCommissionPlans = function () {
    var self = this;
    self.$http.get(window.location.origin + '/api-elife/commissions/plans', {}).then(
        function (successResponse) {
            self.commissionPlans = successResponse.data;
            self.showInfoMessage("Loaded commission plans");
            self.validateCommissionPlans();
        },
        function (errorResponse) {
            self.showErrorMessage(errorResponse.data);
        }
    );
};
CommissionService.prototype.showInfoMessage = function (msg) {
    var self = this;
    self.infoMessage = msg;
    self.successMessage = null;
    self.errorMessage = null;
};
CommissionService.prototype.showSuccessMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = msg;
    self.errorMessage = null;
};
CommissionService.prototype.showErrorMessage = function (msg) {
    var self = this;
    self.infoMessage = null;
    self.successMessage = null;
    self.errorMessage = hasValue(msg) ? self.$sce.trustAsHtml(msg) : null;
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
    //Need to show alert message for new commission
    self.validateCommissionPlan(newCommissionPlan);
};
CommissionService.prototype.removeCommissionPlan = function (commissionPlan) {
    var self = this;
    //if (self.isCommissionPlanReadonly(commissionPlan)) {
    //    return;
    //}
    self.commissionPlans.remove(commissionPlan);
    self.validateCommissionPlans();
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
        isSuccess = self.validateDuplicatePKCommissionPlan();
    }
    if (isSuccess) {
        if (self.commissionPlans.length > 0) {
            self.showSuccessMessage("All input values are correct!")
        }
    }
    self.isValidateSuccess = isSuccess;
    return isSuccess;
};
CommissionService.prototype.validateDuplicatePKCommissionPlan = function () {
    var self = this;

    var duplicates = self.commissionPlans.getDuplicatesByFields(["unitCode", "planCode", "customerCategory"])
    if (duplicates.length == 0) {
        return true;
    } else {
        var duplicateMessage = "Duplicate settings: <ul> ";
        for (var i = 0; i < duplicates.length; i++) {
            var duplicate = duplicates[i];
            duplicateMessage += "<li>[" + (duplicate.indexA + 1) + " & " + (duplicate.indexB + 1) + "] " + duplicate.itemA.unitCode + " - " + duplicate.itemA.planCode + " - " + duplicate.itemA.customerCategory + "</li>";
        }
        duplicateMessage += "</ul>"
        self.showErrorMessage(duplicateMessage);
        return false;
    }
};
CommissionService.prototype.validateCommissionPlan = function (commissionPlan) {
    var self = this;
    var isSuccess = true;
    isSuccess = self.checkHasRequiredInputsInCommissionPlan(commissionPlan);
    if (!isSuccess) {
        self.showErrorMessage("Some commission plans don't have enough input values.");
        self.statusStyleCommissionPlan(commissionPlan, isSuccess);
    } else {
        var commissionTargetGroups = commissionPlan.targetGroups;
        for (var i = 0; i < commissionTargetGroups.length; i++) {
            var commissionTargetGroup = commissionTargetGroups[i];
            var commissionGroupType = self.findCommissionGroupTypeByValue(commissionTargetGroup.targetGroupType);
            if (!hasValue(commissionGroupType)) {
                self.showErrorMessage("Not found commissionGroupType " + commissionTargetGroup);
                break;
            }
            var sumPercentage = self.sumPercentageInCommissionGroup(commissionTargetGroup);
            if ((commissionGroupType.isRequired || self.hasTargetEntitiesPercentages(commissionTargetGroup)) && (Math.abs(sumPercentage - 100) >= 0.0001)) {
                var msg = "Invalid commission: " + commissionPlan.unitCode + "-" + commissionPlan.planCode + "-" + commissionPlan.customerCategory + ": Group '" + commissionTargetGroup.targetGroupType + "' has totally " + sumPercentage + "%";
                self.showErrorMessage(msg);
                isSuccess = false;
                break;
            }
        }
        self.statusStyleCommissionPlan(commissionPlan, isSuccess);
    }
    if (!isSuccess) {
        self.isValidateSuccess = isSuccess;
    }
    return isSuccess;
};
CommissionService.prototype.hasTargetEntitiesPercentages = function (commissionTargetGroup) {
    var commissionTargetEntities = commissionTargetGroup.targetEntities;
    for (var i = 0; i < commissionTargetEntities.length; i++) {
        var commissionTargetEntity = commissionTargetEntities[i];
        var hasValueAndNotZero = isNotBlank(commissionTargetEntity.percentage) && (+commissionTargetEntity.percentage != 0);
        if (hasValueAndNotZero) {
            return true;
        }
    }
    return false;
};
CommissionService.prototype.findCommissionGroupTypeByValue = function (commissionGroupTypeValue) {
    var self = this;
    for (var i = 0; i < self.commissionGroupTypes.length; i++) {
        var commissionGroupType = self.commissionGroupTypes[i];
        if (commissionGroupType.value == commissionGroupTypeValue) {
            return commissionGroupType;
        }
    }
    return null;
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
};
//CONSTRUCT COMMISSION PLANS ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
CommissionService.prototype.constructCommissionPlan = function () {
    var self = this;

    var groupTypes = self.commissionGroupTypes;
    var commissionTargetGroups = [];
    for (var i = 0; i < groupTypes.length; i++) {
        var groupType = groupTypes[i];
        commissionTargetGroups.push(self.constructCommissionGroups(groupType.value));
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
        if (targetGroup.targetGroupType == groupType.value) {
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
CommissionService.prototype.sortCommissionPlans = function (fieldNames) {
    var self = this;

    if (hasValue(self.commissionPlansOrder) && self.commissionPlansOrder == 1) {
        self.commissionPlansOrder = -1;
    } else {
        self.commissionPlansOrder = 1;
    }
    var fieldSorts = [];
    for (var i = 0; i < fieldNames.length; i++) {
        fieldSorts.push(new FieldSort(fieldNames[i], self.commissionPlansOrder));
    }
    self.commissionPlans.sortByFields(fieldSorts);
};

