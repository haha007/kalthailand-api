package th.co.krungthaiaxa.api.elife.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionCalculationSession;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionPlan;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntity;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetEntityType;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroup;
import th.co.krungthaiaxa.api.elife.commission.data.CommissionTargetGroupType;
import th.co.krungthaiaxa.api.elife.commission.data.CustomerCategory;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionCalculationSessionExportService;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionCalculationSessionService;
import th.co.krungthaiaxa.api.elife.commission.service.CommissionPlanService;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.repository.cdb.CDBRepository;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CommissionServiceTest extends ELifeTest {
    @Inject
    CommissionPlanService commissionPlanService;
    @Inject
    CommissionCalculationSessionService commissionCalculationSessionService;
    @Inject
    CommissionCalculationSessionExportService commissionCalculationSessionExportService;

    @Autowired
    private CDBRepository cdbRepository;

    @Test
    public void test_create_commission_with_good_data() {
        List<CommissionPlan> commissionPlans = new ArrayList<>();
        commissionPlans.add(constructCommissionPlan("40001", ProductType.PRODUCT_IPROTECT, CustomerCategory.NEW, new double[][] { { 25, 15, 20, 15, 25 }, { 10, 20, 30, 40, 0 } }));
        commissionPlans.add(constructCommissionPlan("40002", ProductType.PRODUCT_IPROTECT, CustomerCategory.EXISTING, new double[][] { { 25, 15, 20, 15, 25 }, { 10, 20, 30, 40, 0 } }));
        commissionPlanService.saveCommissionPlans(commissionPlans);
    }

    //santi : for start trigger to process
    @Test
    public void should_calculate_commission() {
        commissionCalculationSessionService.calculateCommissionForPolicies();
    }

//    @Test
//    public void testPerformance() {
//        List<CommissionPlan> commissionPlans = commissionPlanService.findAll();
//        List<String> channelIds = commissionPlans.stream().map(sc -> sc.getUnitCode()).collect(Collectors.toList());//list of UnitCode
//        List<String> planCodes = commissionPlans.stream().map(sc -> sc.getPlanCode()).collect(Collectors.toList());
//        List<String> channelIdsNoDup = channelIds.stream().distinct().collect(Collectors.toList());
//        List<String> planCodesNoDup = planCodes.stream().distinct().collect(Collectors.toList());
//
//        //Warmup
//        for (int i = 0; i < 5; i++) {
//            cdbRepository.findPoliciesByChannelIdsAndPaymentModeIds(channelIdsNoDup, planCodesNoDup);
//            cdbRepository.findPoliciesByChannelIdsAndPaymentModeIdsMap(channelIdsNoDup, planCodesNoDup);
//        }
//
//        //Test performance.
//        Instant start = Instant.now();
//        for (int i = 0; i < 300; i++) {
//            cdbRepository.findPoliciesByChannelIdsAndPaymentModeIds(channelIdsNoDup, planCodesNoDup);
//        }
//        start = LogUtil.logRuntime(start, "[BeanMapping] finish");
//
//        for (int i = 0; i < 300; i++) {
//            cdbRepository.findPoliciesByChannelIdsAndPaymentModeIdsMap(channelIdsNoDup, planCodesNoDup);
//        }
//        start = LogUtil.logRuntime(start, "[Map] finish");
//    }

    //santi : for get list of all commission result
    @Test
    public void shold_get_list_of_calculated_commission_result() {
        List<CommissionCalculationSession> list = commissionCalculationSessionService.findAllCommissionCalculationSessions();
        Assert.assertNotNull(list);
    }

    //santi : for download commission excel file

    /**
     * This unit test will fail if we cannot connect to CDB database.
     */
//    @Test
    public void should_get_excel_commission() {
        List<CommissionCalculationSession> commissionList = commissionCalculationSessionService.findAllCommissionCalculationSessions();
        if (commissionList.size() != 0) {
            byte[] content = commissionCalculationSessionExportService.exportToExcel(commissionList.get(0).getId().toString());
            File excelFile = new File("target/commissionExtract.xlsx");
            try {
                writeByteArrayToFile(excelFile, content);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            assertThat(excelFile.exists()).isTrue();
        }
    }

    /**
     * @param unitCode View more at {@link CommissionPlan#unitCode}
     * @return
     */
    public static CommissionPlan constructCommissionPlan(String unitCode, ProductType productType, CustomerCategory customerCategory, double[][] commissionValues) {
        CommissionPlan commissionPlan = new CommissionPlan();
        commissionPlan.setPlanCode(productType.getLogicName());
        commissionPlan.setUnitCode(unitCode);
        commissionPlan.setCustomerCategory(customerCategory);

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
