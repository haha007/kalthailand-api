package th.co.krungthaiaxa.api.elife.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.ELifeTest;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntityType;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroupType;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionPlanService;
import th.co.krungthaiaxa.api.elife.products.ProductType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CommissionServiceTest extends ELifeTest {
    @Inject
    CommissionPlanService commissionPlanService;

    @Test
    public void test_create_commission_with_good_data() {
        List<CommissionPlan> commissionPlans = new ArrayList<>();
        CommissionPlan commissionPlan = constructCommissionPlan("40001", ProductType.PRODUCT_IPROTECT, new double[][] { { 25, 15, 20, 15, 25 }, { 10, 20, 30, 40, 0 } });
        commissionPlans.add(commissionPlan);
        commissionPlanService.saveCommissions(commissionPlans);
    }

    /**
     * @param unitCode View more at {@link CommissionPlan#unitCode}
     * @return
     */
    public static CommissionPlan constructCommissionPlan(String unitCode, ProductType productType, double[][] commissionValues) {
        CommissionPlan commissionPlan = new CommissionPlan();
        commissionPlan.setPlanCode(productType.getName());
        commissionPlan.setUnitCode(unitCode);

        List<CommissionTargetGroup> targetGroups = new ArrayList<>();
        constructAddingTargetGroup(targetGroups, CommissionTargetGroupType.FY, commissionValues[0][0], commissionValues[0][1], commissionValues[0][2], commissionValues[0][3], commissionValues[0][4]);
        constructAddingTargetGroup(targetGroups, CommissionTargetGroupType.OV, commissionValues[1][0], commissionValues[1][1], commissionValues[1][2], commissionValues[1][3], commissionValues[1][4]);
        commissionPlan.setTargetGroups(targetGroups);
        return commissionPlan;
    }

    public static void constructAddingTargetGroup(List<CommissionTargetGroup> targetGroups, CommissionTargetGroupType targetGroupType, double affiliate, double company, double distribution, double mkr, double tsr) {
        targetGroups.add(constructCommissionTargetGroup(targetGroupType, affiliate, company, distribution, mkr, tsr));
    }

    public static CommissionTargetGroup constructCommissionTargetGroup(CommissionTargetGroupType commissionTargetGroupType, double affiliate, double company, double distribution, double mkr, double tsr) {

        List<CommissionTargetEntity> targetEntities = new ArrayList<>();
        constructAddingCommissionTarget(targetEntities, CommissionTargetEntityType.AFFILIATE, affiliate);
        constructAddingCommissionTarget(targetEntities, CommissionTargetEntityType.COMPANY, company);
        constructAddingCommissionTarget(targetEntities, CommissionTargetEntityType.DISTRIBUTION, distribution);
        constructAddingCommissionTarget(targetEntities, CommissionTargetEntityType.MKR, mkr);
        constructAddingCommissionTarget(targetEntities, CommissionTargetEntityType.TSR, tsr);

        CommissionTargetGroup targetGroup = new CommissionTargetGroup();
        targetGroup.setTargetGroupType(commissionTargetGroupType);
        targetGroup.setTargetEntities(targetEntities);

        return targetGroup;
    }

    public static void constructAddingCommissionTarget(List<CommissionTargetEntity> commissionTargetEntities, CommissionTargetEntityType commissionTargetEntityType, double commissionPercentage) {
        CommissionTargetEntity commissionTargetEntity = new CommissionTargetEntity();
        commissionTargetEntity.setTargetEntityType(commissionTargetEntityType);
        commissionTargetEntity.setPercentage(commissionPercentage);
        commissionTargetEntities.add(commissionTargetEntity);
    }

}
