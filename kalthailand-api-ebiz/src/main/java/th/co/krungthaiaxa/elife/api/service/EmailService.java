package th.co.krungthaiaxa.elife.api.service;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.elife.api.model.Quote;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;


/**
 * Created by santilik on 2/10/2016.
 */

@Service
public class EmailService {

    private String benefitPoint64 = "iVBORw0KGgoAAAANSUhEUgAAAEsAAAAbCAYAAAA0wHIdAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsSAAALEgHS3X78AAADYklEQVRYw+1Zz0sqURT+RsbGRgl0Wog/UKk2zkIqGjcRUeC6Nu3b9Sf1F7Ro06pNMEHQaiJFSJAIZiTdJKLEJI0zg28RnTfyfO9pcMfXw291ztX5zrmf9945nssNh8MhZgjHcdBsNtFoNNDtdtHv92FZFgRBgCiKiEajyGQySKVS4Hl+lqmCm5VYg8EA1WoVtVoNjuP89fs8z0OWZRQKBSwsLMwi5dmIZRgGbm9v8f7+PvWzoVAI29vbyGazfqftv1iVSgXlchnesJIkIZfLIZlMIhKJQBAEWJYF0zTRarWg6zo6nc7PpDkOGxsbWF9f/3/FqlQquL+/J39paQmKoky0SgzDgKZpeH19pbHNzU1fBfNNLMMwoKoqrah0Oo29vT0Eg8GJOWzbxvX1NZ6fnz+S5zjs7+/7tiV9EWswGOD8/JzOqHQ6jVKpBI7jAACu66Jer0PXdfR6PTiOA1EUkUgkkM/nEYvFiGs4HOLq6ooEC4VCODo68uXQ90Wsu7s7VKtVAB9b7/DwkFbUy8sLVFXF29vb+AQ5DrIso1gskri2bePi4oK2ZKFQwNbWFnOxAqwDOI6DWq1GvqIoI0JdXl7+VijgYyU9PDzg5uaGxoLBIBRFIX/S8uOfF6vZbNJEJEmi88V1XaiqCtd1J+J5enrC4+Mj+dlsFpIkAfhZ2H57sRqNBtm5XI7ser3+xxU1DuVyecT38nnjsAJzsbrdLtnJZJJsXden5jJNE+12eyyfNw4rMBer3++THYlEyO71el/i8z7n5fPGYQX+9PSUaQBBEMbaXz2Qvc95+SzLAuu58CcnJ0wDnJ2d0a9uWRYWFxcBAKIojlTjkyIcDo8I5BXu+PiY6VyYb0NRFMk2TZPsRCIxfbKBAOLx+Fg+bxxWYC5WNBolu9VqkZ3P56nInBQrKysjlbqXzxuHFZiLlclkyPa+AWOxGGRZnphHFMVfqnQvnzcOKzAXy9vh7HQ6MAyDPisWi1hdXZ1IqFKpNLLVDMOgtg3P80ilUt9frM8O5yc0TYNt2wA+/vft7u5iZ2dnpAyg5AIBrK2t4eDgAMvLyzRu2zY0TSNflmVfWs7/RNfhE+12m7oO4XAY8Xj8l27Cf991AOb9rKkx75R+QbB5D34KzG93psT83vAL+E430j8AyNv7woOREhQAAAAASUVORK5CYII=";

    private String benefitRed64 = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAjCAYAAACpZEt+AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsSAAALEgHS3X78AAAAJUlEQVQ4y2NkWHj/PwMewMRAAIwqGFUwqmBUAS7AOPPWZ7wlDABFBAYpPHX0EgAAAABJRU5ErkJggg==";

    private String benefitGreen64 = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAjCAYAAACpZEt+AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsSAAALEgHS3X78AAAAYklEQVQ4y+3UsQ2AMAxE0e/EEjTsxmoMxQLU9IxAEcJRJBIgMQHKNZatd61tWkZhmcAJAAo8894+0kADDfwV+JAyu0MKXblY+RNGIgpcEhJIqp17SmDrNisHkPraPIjVxOxczrsifgKtD0EAAAAASUVORK5CYII=";

    public void sendEmail(Quote quote, String base64Image, String smtp, String emailName, String subject, String lineURL) throws Exception {
        try {
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", smtp);
            Session session = Session.getDefaultInstance(properties);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailName));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(quote.getInsureds().get(0).getPerson().getEmail()));

            message.setSubject(subject);

            Multipart multipart = new MimeMultipart("related");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(getEmailContent(quote, lineURL), "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource fds = new ByteArrayDataSource(java.util.Base64.getDecoder().decode(base64Image), "application/octet-stream");
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.addHeader("Content-ID", "<imageEbiz2>");
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource benefitPoint = new ByteArrayDataSource(java.util.Base64.getDecoder().decode(benefitPoint64), "application/octet-stream");
            messageBodyPart.setDataHandler(new DataHandler(benefitPoint));
            messageBodyPart.addHeader("Content-ID", "<benefitPoint>");
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource benefitGreen = new ByteArrayDataSource(java.util.Base64.getDecoder().decode(benefitGreen64), "application/octet-stream");
            messageBodyPart.setDataHandler(new DataHandler(benefitGreen));
            messageBodyPart.addHeader("Content-ID", "<benefitGreen>");
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource benefitRed = new ByteArrayDataSource(java.util.Base64.getDecoder().decode(benefitRed64), "application/octet-stream");
            messageBodyPart.setDataHandler(new DataHandler(benefitRed));
            messageBodyPart.addHeader("Content-ID", "<benefitRed>");
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (RuntimeException e) {
            throw new Exception(e);
        }
    }

    private String getEmailContent(Quote quote, String lineURL) {
        String decimalFormat = "#,##0.00";
        File file = new File((getClass().getClassLoader()).getResource("email-content.txt").getFile());
        String emailContent = "";
        try {
            List lines = Files.readAllLines(Paths.get(file.getAbsolutePath()),
                    Charset.defaultCharset());
            for (Object line : lines) {
                emailContent += (String) line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emailContent.replace("%1$s", "test1")
                .replace("%2$s", "test2")
                .replace("%3$s", quote.getInsureds().get(0).getAgeAtSubscription().toString())
                .replace("%4$s", quote.getPremiumsData().getFinancialScheduler().getPeriodicity().getCode().toString())
                .replace("%5$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsuranceSumInsured()))
                .replace("%6$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getFinancialScheduler().getModalAmount().getValue()))
                .replace("%7$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsuranceMinimumYearlyReturns()))
                .replace("%8$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsuranceAverageYearlyReturns()))
                .replace("%9$s", (new DecimalFormat(decimalFormat)).format(quote.getPremiumsData().getLifeInsuranceMaximumYearlyReturns()))
                .replace("%10$s", "/" + lineURL + "/")
                .replace("%11$s", "/" + lineURL + "fatca-questions/" + quote.getQuoteId() + "/")
                .replace("%12$s", "/" + lineURL + "quote-product/line-10-ec" + "/");
    }

}
