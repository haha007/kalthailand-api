package th.co.krungthaiaxa.api.common.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.projectinfo.ProjectInfoProperties;

import javax.inject.Inject;

@RestController
public class ProjectInfoResource {
    Logger logger = LoggerFactory.getLogger(ProjectInfoResource.class);
    @Inject
    ProjectInfoProperties projectInfoProperties;

    public ProjectInfoResource() {
        logger.info("INIT PROJECT INFO RESOURCE");
    }

    @RequestMapping(value = "/project-info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectInfoProperties getProjectInfoProperties() {
        return this.projectInfoProperties;
    }

}
