package th.co.krungthaiaxa.api.elife.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.utils.EmailElifeUtil;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author khoi.tran on 10/17/16.
 *         This class will send email from Elife department of Krungthai AXA.
 *         This class will replace {@link EmailService}
 */
@Service
public class ElifeEmailService {
    private final EmailSender emailSender;
    @Value("${email.name}")
    private String fromEmail;

    @Autowired
    public ElifeEmailService(EmailSender emailSender) {this.emailSender = emailSender;}

    public void sendEmail(String toEmailAddress, String emailSubject, String emailContent, String attachmentName, byte[] attachmentBytes) {
        emailSender.sendEmail(fromEmail, toEmailAddress, emailSubject, emailContent, Collections.emptyList(), EmailElifeUtil.initAttachment(attachmentName, attachmentBytes));
    }

    public void sendEmail(String toEmailAddress, String emailSubject, String emailContent, List<Pair<byte[], String>> attachments) {
        emailSender.sendEmail(fromEmail, toEmailAddress, emailSubject, emailContent, Collections.emptyList(), attachments);
    }

    public void sendEmail(String toEmailAddress, String emailSubject, String emailContent) {
        emailSender.sendEmail(fromEmail, toEmailAddress, emailSubject, emailContent, Collections.emptyList(), Collections.emptyList());
    }

    public void sendEmails(List<String> toEmails, String emailSubject, String emailContent, List<Pair<byte[], String>> attachments) {
        Set<String> toEmailSet = new HashSet<>(toEmails);
        for (String toEmail : toEmailSet) {
            sendEmail(toEmail, emailSubject, emailContent, attachments);
        }
    }
}
