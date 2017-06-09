package th.co.krungthaiaxa.admin.elife.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import th.co.krungthaiaxa.admin.elife.model.ActivationFormData;
import th.co.krungthaiaxa.admin.elife.model.AuthenticatedFeaturesUser;
import th.co.krungthaiaxa.admin.elife.model.LoginFormData;
import th.co.krungthaiaxa.admin.elife.service.AuthenticationClient;
import th.co.krungthaiaxa.api.common.exeption.BeanValidationException;
import th.co.krungthaiaxa.api.common.exeption.UnauthenticationException;
import th.co.krungthaiaxa.api.common.filter.ExceptionTranslator;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.model.projectinfo.ProjectInfoProperties;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;
import th.co.krungthaiaxa.api.common.validator.BeanValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author khoi.tran on 11/8/16.
 */

@Controller
public class WebPageResource {
    public static final Logger LOGGER = LoggerFactory.getLogger(WebPageResource.class);
    private static final String SESSION_ATTR_USER = "user";
    private static final String PAGE_MODEL_ATTR_USER = "user";
    private static final String PAGE_MODEL_ATTR_PROJECT_INFO = "projectInfo";
    private static final String SUCCESS_ATTR = "success";

    private final AuthenticationClient authenticateService;
    private final ProjectInfoProperties projectInfoProperties;
    private final ExceptionTranslator exceptionTranslator;
    private final BeanValidator beanValidator;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebPageResource(AuthenticationClient authenticateService,
                           ProjectInfoProperties projectInfoProperties,
                           ExceptionTranslator exceptionTranslator,
                           BeanValidator beanValidator,
                           ObjectMapper objectMapper) {
        this.authenticateService = authenticateService;
        this.projectInfoProperties = projectInfoProperties;
        this.exceptionTranslator = exceptionTranslator;
        this.beanValidator = beanValidator;
        this.objectMapper = objectMapper;
    }

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest httpServletRequest) {
        return login(model);
    }

    @RequestMapping("/login")
    public String login(Model model) {
        LoginFormData loginFormData = new LoginFormData();
        model.addAttribute("loginFormData", loginFormData);
        model.addAttribute(PAGE_MODEL_ATTR_PROJECT_INFO, projectInfoProperties);
        return "login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(SESSION_ATTR_USER);
        return "redirect:/login";
    }

    /**
     * This actually only a temporary way which migrate from the old code.
     * Actually we should use login of Spring Security.
     *
     * @param loginFormData
     * @param model
     * @return
     */
    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public String authentication(@ModelAttribute LoginFormData loginFormData,
                                 Model model, BindingResult bindingResult,
                                 HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedFeaturesUser authenticatedFeaturesUser = authenticateService.authenticatedUserWithAvailableFeatures(loginFormData);
            //Disabled the old session (which can be login with different account)
            HttpSession session = RequestUtil.createNewSession(httpServletRequest);
            model.addAttribute(PAGE_MODEL_ATTR_USER, authenticatedFeaturesUser);
            model.addAttribute(PAGE_MODEL_ATTR_PROJECT_INFO, projectInfoProperties);
            //TODO session is only the temporary solution because it cannot apply for clustering.
            session.setAttribute(SESSION_ATTR_USER, authenticatedFeaturesUser);
            return "index";
        } catch (UnauthenticationException ex) {
            bindingResult.reject(ex.getErrorCode(), ex.getErrorMessage());
            return "login";
        } catch (Exception ex) {
            bindingResult.reject(ErrorCode.ERROR_CODE_UNKNOWN_ERROR, ex.getMessage());
            return "login";
        }
    }

    /**
     * This method avoids the error when user reinput the address of admin in URL.
     * E.g. http://localhost:8080/admin-elife/admin#/policy-detail?policyID=502-0008308
     *
     * @param model
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String refreshAdmin(Model model, HttpServletRequest httpServletRequest) {
        //TODO session is only the temporary solution because it cannot apply for clustering.
        AuthenticatedFeaturesUser user = (AuthenticatedFeaturesUser) httpServletRequest.getSession().getAttribute(SESSION_ATTR_USER);
        if (user != null) {
//            String accessToken = RequestUtil.getAccessToken(httpServletRequest);
//            if (accessToken != null && accessToken.equals(user.getAccessToken())) {
            model.addAttribute(PAGE_MODEL_ATTR_USER, user);
            model.addAttribute(PAGE_MODEL_ATTR_PROJECT_INFO, projectInfoProperties);
            return "index";
//            }
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/activate", method = RequestMethod.GET)
    public String activateView(Model model,
                               @RequestParam(value = "key", required = false) final String activationKey) {
        final ActivationFormData activationForm = new ActivationFormData();
        activationForm.setActivationKey(activationKey);
        model.addAttribute("activationForm", activationForm);
        return "activation-user";
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public String activateUser(Model model, @ModelAttribute ActivationFormData activationForm) {
        model.addAttribute("activationForm", activationForm);

        try {
            beanValidator.validate(activationForm);

            if (activationForm.getConfirmPassword().equals(activationForm.getPassword())) {
                final Map response = authenticateService.activateUser(activationForm);
                model.addAttribute(SUCCESS_ATTR,
                        response.containsKey(SUCCESS_ATTR) && response.get(SUCCESS_ATTR).equals(true));
            } else {
                model.addAttribute("httpErrorData", ErrorCode.INVALID_CONFIRM_PASSWORD);
            }
        } catch (final BeanValidationException ex) {
            model.addAttribute("validationErrorData", exceptionTranslator.processValidationError(ex).getFieldErrors());
        } catch (final HttpClientErrorException ex) {
            model.addAttribute("httpErrorData",
                    ObjectMapperUtil.toObject(objectMapper, ex.getResponseBodyAsString(), Map.class));
            LOGGER.debug("Bad request, Could not activate user: ", ex);
        }
        return "activation-user";
    }
}