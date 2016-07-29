package th.co.krungthaiaxa.api.elife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import th.co.krungthaiaxa.api.elife.model.projectinfo.ProjectInfoProperties;
import th.co.krungthaiaxa.api.elife.service.ProjectInfoService;

import javax.inject.Inject;

@Configuration
public class ProjectInfoConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProjectInfoConfiguration.class);

    @Inject
    ProjectInfoService projectInfoService;

    @Bean
    public ProjectInfoProperties projectInfoProperties() {
        LOGGER.debug("Load Project Info");
        return projectInfoService.loadProjectInfoProperties();
    }
}
