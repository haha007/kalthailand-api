package th.co.krungthaiaxa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import th.co.krungthaiaxa.pdfmaker.PDFMaker;

@SpringBootApplication
public class PDFMakerApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(PDFMakerApplication.class, args);
		PDFMaker pdfMaker = ctx.getBean(PDFMaker.class);
		pdfMaker.start();
	}
}
