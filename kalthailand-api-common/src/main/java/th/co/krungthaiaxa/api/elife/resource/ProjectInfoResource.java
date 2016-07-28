package th.co.krungthaiaxa.api.elife.resource;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.elife.ProjectInfoProperties;

import javax.inject.Inject;

@RestController
public class ProjectInfoResource {
    @Inject
    ProjectInfoProperties projectInfoProperties;

    @RequestMapping(value = "/project-info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectInfoProperties getProjectInfoProperties() {
        return this.projectInfoProperties;
    }

}
