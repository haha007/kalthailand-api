package th.co.krungthaiaxa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import th.co.krungthaiaxa.swaggermerge.SwaggerMerger;

@SpringBootApplication
public class SwaggerMergeApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(SwaggerMergeApplication.class, args);
		SwaggerMerger swaggerMerger = ctx.getBean(SwaggerMerger.class);
		swaggerMerger.start();
	}
}
