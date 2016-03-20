package th.co.krungthaiaxa.elife.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@ApiIgnore
@Controller
public class AdminController {

    @RequestMapping(value = "/admin", method = GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/admin/collectionFile", method = GET)
    public String collectionFile() {
        return "collectionFile";
    }
}
