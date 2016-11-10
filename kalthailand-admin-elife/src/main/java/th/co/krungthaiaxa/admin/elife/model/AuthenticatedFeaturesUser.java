package th.co.krungthaiaxa.admin.elife.model;

import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUser;

import java.util.List;
import java.util.Map;

/**
 * @author khoi.tran on 11/10/16.
 */
public class AuthenticatedFeaturesUser extends AuthenticatedUser {
    private List<String> availableFeatures;
    private Map<String, Boolean> featuresAvailabilities;

    public List<String> getAvailableFeatures() {
        return availableFeatures;
    }

    public void setAvailableFeatures(List<String> availableFeatures) {
        this.availableFeatures = availableFeatures;
    }

    public Map<String, Boolean> getFeaturesAvailabilities() {
        return featuresAvailabilities;
    }

    public void setFeaturesAvailabilities(Map<String, Boolean> featuresAvailabilities) {
        this.featuresAvailabilities = featuresAvailabilities;
    }
}
