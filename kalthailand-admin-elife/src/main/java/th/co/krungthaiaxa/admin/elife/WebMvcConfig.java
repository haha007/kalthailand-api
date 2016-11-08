package th.co.krungthaiaxa.admin.elife;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

/**
 * @author khoi.tran on 11/8/16.
 */
//@Configuration
//@ComponentScan
public class WebMvcConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

    @Autowired
    public SpringTemplateEngine templateEngine;

    //    @Bean
//    public ThymeleafTilesConfigurer tilesConfigurer() {
//        final ThymeleafTilesConfigurer configurer = new ThymeleafTilesConfigurer();
//        configurer.setDefinitions(ThymeleafAutoConfiguration.DEFAULT_PREFIX + "**/views.xml");
//        return configurer;
//    }
//
//    @Bean
//    public ThymeleafViewResolver thymeleafViewResolver() {
//        final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
//        resolver.setViewClass(ThymeleafTilesView.class);
//        resolver.setTemplateEngine(templateEngine);
//        resolver.setCharacterEncoding(UTF_8);
//        return resolver;
//    }
//
//    @Bean
//    public TilesDialect tilesDialect() {
//        return new TilesDialect();
//    }
    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
//
//    private TemplateEngine templateEngine() {
//        SpringTemplateEngine engine = new SpringTemplateEngine();
//        engine.setTemplateResolver(templateResolver());
//        return engine;
//    }
//
//    private ITemplateResolver templateResolver() {
//        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
//        resolver.setApplicationContext(applicationContext);
//        resolver.setPrefix("/WEB-INF/views/");
//        resolver.setSuffix(".html");
//        resolver.setCacheable(false); // On production , turn TRUE
//        resolver.setTemplateMode(TemplateMode.HTML);
//        return resolver;
//    }

    @Value("${server.session-timeout}")
    private Long sessionTimeOut;

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(sessionTimeOut * 1000L);
        configurer.registerCallableInterceptors(timeoutInterceptor());
    }

    @Bean
    public TimeoutCallableProcessingInterceptor timeoutInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }

}