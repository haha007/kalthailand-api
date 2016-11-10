package th.co.krungthaiaxa.admin.elife.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.admin.elife.model.AuthenticatedFeaturesUser;
import th.co.krungthaiaxa.admin.elife.model.LoginFormData;
import th.co.krungthaiaxa.api.common.exeption.JsonConverterException;
import th.co.krungthaiaxa.api.common.exeption.UnauthenticatedException;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.model.authentication.AuthenticatedUser;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author khoi.tran on 11/10/16.
 */
@Service
public class AuthenticationClient {
    private static final String UI_ROLE_FILTER_CONFIG_FILE = "/static/uiRoleFilter.properties";
    public static final Properties UI_ROLE_CONFIG = loadUiRoleConfiguration();
    @Value("${kal.api.auth.contextpath}")
    private String authContextPath;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public AuthenticationClient(ObjectMapper objectMapper) {this.objectMapper = objectMapper;}

    private AuthenticatedUser authenticate(LoginFormData loginFormData) {
        try {
            String url = authContextPath + "/auth/user";
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginFormData, String.class);
            String responseJson = responseEntity.getBody();
            return ObjectMapperUtil.toObject(objectMapper, responseJson, AuthenticatedUser.class);
        } catch (HttpServerErrorException ex) {
            String responseJson = ex.getResponseBodyAsString();
            try {
                Error error = ObjectMapperUtil.toObject(objectMapper, responseJson, Error.class);
                if (ErrorCode.ERROR_CODE_AUTHENTICATION.equals(error.getCode())) {
                    throw new UnauthenticatedException("Invalid email/password combination. Please try again.", ex);
                } else {
                    throw new UnauthenticatedException("Error from authentication service: " + error.getUserMessage(), ex);
                }
            } catch (JsonConverterException jsonException) {
                throw new UnauthenticatedException("Error from authentication service: " + ex.getMessage(), jsonException);
            }
        }
    }

    public AuthenticatedFeaturesUser authenticatedUserWithAvailableFeatures(LoginFormData loginFormData) {
        AuthenticatedUser authenticatedUser = authenticate(loginFormData);
        AuthenticatedFeaturesUser authenticatedFeaturesUser = new AuthenticatedFeaturesUser();
        BeanUtils.copyProperties(authenticatedUser, authenticatedFeaturesUser);
        setAvailableFunctions(authenticatedFeaturesUser);
        return authenticatedFeaturesUser;
    }

    private void setAvailableFunctions(AuthenticatedFeaturesUser authenticatedUser) {
        List<String> availableFeatures = new ArrayList<>();
        Map<String, Boolean> featuresAvailabilities = new HashMap<>();
        authenticatedUser.setFeaturesAvailabilities(featuresAvailabilities);
        authenticatedUser.setAvailableFeatures(availableFeatures);
        List<String> availableRoles = authenticatedUser.getRoles();
        Properties properties = UI_ROLE_CONFIG;
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String featureName = (String) propertyNames.nextElement();
            String requiredRolesString = properties.getProperty(featureName);
            List<String> requiredRoles = StringUtil.splitToNotNullStrings(requiredRolesString);
            boolean featureAvail = matchRequiredRoles(availableRoles, requiredRoles);
            featuresAvailabilities.put(featureName, featureAvail);
            if (featureAvail) {
                availableFeatures.add(featureName);
            }
        }
    }

    private boolean matchRequiredRoles(List<String> availableRoles, List<String> requiredRoles) {
        for (String availableRole : availableRoles) {
            if (requiredRoles.contains(availableRole)) {
                return true;
            }
        }
        return false;
    }

    private static Properties loadUiRoleConfiguration() {

        try (InputStream inputStream = IOUtil.loadInputStreamFromClassPath(UI_ROLE_FILTER_CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new UnexpectedException("Cannot load uiRoleFilter " + UI_ROLE_FILTER_CONFIG_FILE + ": " + e.getMessage(), e);
        }
    }
}
