package th.co.krungthaiaxa.admin.elife;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import th.co.krungthaiaxa.api.common.utils.ProjectProfileUtil;

public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.profiles(ProjectProfileUtil.getDefaultProfile()).sources(KalAdminApplication.class);
    }
}
