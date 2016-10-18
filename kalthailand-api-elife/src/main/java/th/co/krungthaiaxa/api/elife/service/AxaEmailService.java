package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.Periodicity;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author khoi.tran on 10/17/16.
 *         This class will send email from AXA email.
 *         This class will replace {@link EmailService}
 */
@Service
public class AxaEmailService {
    private final EmailSender emailSender;
    @Value("${email.name}")
    private String emailName;

    @Autowired
    public AxaEmailService(EmailSender emailSender) {this.emailSender = emailSender;}

    public void sendEmail(String toEmailAddress, String emailSubject, String emailContent,
            List<Pair<byte[], String>> images, List<Pair<byte[], String>> attachments) {
        emailSender.sendEmail(emailName, toEmailAddress, emailSubject, emailContent, images, attachments);
    }

}
