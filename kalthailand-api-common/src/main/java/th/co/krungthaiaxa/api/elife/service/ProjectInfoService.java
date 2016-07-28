package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.projectinfo.ProjectInfoProperties;
import th.co.krungthaiaxa.api.elife.utils.ProfileHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

@Service
public class ProjectInfoService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProjectInfoService.class);

    private final ProfileHelper profileHelper;

    @Inject
    public ProjectInfoService(ProfileHelper profileHelper) {this.profileHelper = profileHelper;}

    public ProjectInfoProperties loadProjectInfoProperties() {
        String propertiesFile = "/build.properties";
        ProjectInfoProperties projectInfoProperties = new ProjectInfoProperties();
        projectInfoProperties.setProfiles(profileHelper.getUsingProfiles());
        try {
            Resource resource = new ClassPathResource(propertiesFile);
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            projectInfoProperties.setGitBuildNumber(props.getProperty("git.build.number"));
            projectInfoProperties.setProjectVersion(props.getProperty("project.version"));
            projectInfoProperties.setGitRevision(props.getProperty("git.revision"));
            projectInfoProperties.setProjectBuildTime(props.getProperty("project.build.time"));
        } catch (IOException e) {
            LOGGER.warn("Cannot load properties from {}. But don't worry, the program can start normally. You only need to recheck why the file is not generated automatically (configured by Maven plugin).", propertiesFile);
        }
        return projectInfoProperties;
    }
}
