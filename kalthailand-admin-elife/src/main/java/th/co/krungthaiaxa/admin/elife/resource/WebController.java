package th.co.krungthaiaxa.admin.elife.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import th.co.krungthaiaxa.api.common.model.authentication.Token;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

/**
 * @author khoi.tran on 11/8/16.
 */

@Controller
public class WebController {
    public static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);
    @Value("${kal.api.auth.contextpath}")
    private String authContextPath;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Autowired
    public WebController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
        String accessToken = authenticate(loginFormData);
        if (StringUtils.isBlank(accessToken)) {
            bindingResult.reject("account", "Authentication fail!");
            return "login";
        }
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("username", loginFormData.getUserName());
        return "index";
    }

    private String authenticate(LoginFormData loginFormData) {
        String accessToken = null;
        try {
            String url = authContextPath + "/auth";
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginFormData, String.class);
            if (HttpStatus.OK == responseEntity.getStatusCode()) {
                String responseJson = responseEntity.getBody();
                Token responseObject = ObjectMapperUtil.toObject(objectMapper, responseJson, Token.class);
                accessToken = responseObject.getToken();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return accessToken;
    }

    public static class LoginFormData {
        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}