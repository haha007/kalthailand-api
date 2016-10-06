package th.co.krungthaiaxa.api.signing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import th.co.krungthaiaxa.api.common.utils.ProjectProfileUtil;

public class ServletInitializer extends SpringBootServletInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletInitializer.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.profiles(ProjectProfileUtil.getDefaultProfile()).sources(KALApiSigning.class);
    }
}
