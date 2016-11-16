package th.co.krungthaiaxa.admin.elife.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import th.co.krungthaiaxa.admin.elife.model.AuthenticatedFeaturesUser;
import th.co.krungthaiaxa.admin.elife.model.LoginFormData;
import th.co.krungthaiaxa.admin.elife.service.AuthenticationClient;
import th.co.krungthaiaxa.api.common.exeption.UnauthenticationException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.model.projectinfo.ProjectInfoProperties;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author khoi.tran on 11/8/16.
 */

@Controller
public class WebPageResource {
    public static final Logger LOGGER = LoggerFactory.getLogger(WebPageResource.class);
    public static final String SESSION_ATTR_USER = "user";
    public static final String PAGE_MODEL_ATTR_USER = "user";
    public static final String PAGE_MODEL_ATTR_PROJECT_INFO = "projectInfo";

    private final AuthenticationClient authenticateService;
    private final ProjectInfoProperties projectInfoProperties;

    @Autowired
    public WebPageResource(AuthenticationClient authenticateService, ProjectInfoProperties projectInfoProperties) {
        this.authenticateService = authenticateService;
        this.projectInfoProperties = projectInfoProperties;
    }

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest httpServletRequest) {
        return login(model, httpServletRequest);
    }

    @RequestMapping("/login")
    public String login(Model model, HttpServletRequest httpServletRequest) {
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
    public String authentication(@ModelAttribute LoginFormData loginFormData, Model model, BindingResult bindingResult, HttpServletRequest httpServletRequest) {
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

}