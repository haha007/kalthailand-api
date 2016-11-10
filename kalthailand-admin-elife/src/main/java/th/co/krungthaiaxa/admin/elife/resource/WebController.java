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
import th.co.krungthaiaxa.api.common.exeption.UnauthenticatedException;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;

/**
 * @author khoi.tran on 11/8/16.
 */

@Controller
public class WebController {
    public static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);

    private final AuthenticationClient authenticateService;

    @Autowired
    public WebController(AuthenticationClient authenticateService) {this.authenticateService = authenticateService;}

    @RequestMapping("/")
    public String index(Model model) {
        return login(model);
    }

    @RequestMapping("/login")
    public String login(Model model) {
        LoginFormData loginFormData = new LoginFormData();
        model.addAttribute("loginFormData", loginFormData);
        return "login";
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
    public String authentication(@ModelAttribute LoginFormData loginFormData, Model model, BindingResult bindingResult) {
        try {
            AuthenticatedFeaturesUser authenticatedFeaturesUser = authenticateService.authenticatedUserWithAvailableFeatures(loginFormData);
            model.addAttribute("user", authenticatedFeaturesUser);
            return "index";
        } catch (UnauthenticatedException ex) {
            bindingResult.reject(ex.getErrorCode(), ex.getErrorMessage());
            return "login";
        } catch (Exception ex) {
            bindingResult.reject(ErrorCode.ERROR_CODE_UNKNOWN_ERROR, ex.getMessage());
            return "login";
        }
    }

}