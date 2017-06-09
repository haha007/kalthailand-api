package th.co.krungthaiaxa.api.auth.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import static javax.mail.Transport.send;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

@Component
public class EmailSender {
    public final static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);
    @Value("${email.smtp.server}")
    private String smtpHost;
    @Value("${email.smtp.port}")
    private String smtpPort;

    public void sendEmail(final String fromEmailAddress,
                          final String toEmailAddress,
                          final String emailSubject,
                          final String emailContent) {
        String maskFromMail = StringUtil.maskEmail(fromEmailAddress);
        String maskToMail = StringUtil.maskEmail(toEmailAddress);
        LOGGER.info("Send email [start]: fromEmail: '{}', toEmail: '{}'", maskFromMail, maskToMail);

        hasText(smtpHost, "smtpHost is a mandatory value and cannot be null/blank");
        hasText(smtpPort, "smtpPort is a mandatory value and cannot be null/blank");
        try {
            send(generateMessage(fromEmailAddress, toEmailAddress, emailSubject, emailContent));
            LOGGER.info("Send email [success]: fromEmail: '{}', toEmail: '{}'", maskFromMail, maskToMail);
        } catch (MessagingException | IOException e) {
            LOGGER.error("Send email [error]: fromEmail: {}, toEmail: {}, emailSubject: {}",
                    maskFromMail, maskToMail, emailSubject, e);
        }
    }

    private MimeMessage generateMessage(final String fromEmailAddress,
                                        final String toEmailAddress,
                                        final String emailSubject,
                                        final String emailContent)
            throws MessagingException, IOException {

        notNull(fromEmailAddress, "email from is required");
        notNull(toEmailAddress, "At least one 'to' email address required");
        hasText(emailSubject, "emailSubject is required");
        hasText(emailContent, "emailContent is required");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(getContentBodyPart(emailContent, "text/html;charset=UTF-8"));

        // Get an SMTP Session Object based on the properties defined
        final Session session = getSession(fromEmailAddress);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(fromEmailAddress));
        message.setHeader("Content-Type", "text/html; charset=UTF-8");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");
        message.setRecipients(Message.RecipientType.TO, toEmailAddress);

        // Set Subject: header field
        message.setSubject(MimeUtility.encodeText(emailSubject, "UTF-8", "Q"));

        message.setContent(multipart);
        message.setSentDate(new Date());
        return message;
    }

    private BodyPart getContentBodyPart(final String content, final String encoding) throws MessagingException, IOException {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(content, encoding)));
        return messageBodyPart;
    }

    private Session getSession(final String smtpFrom) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "false");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.from", smtpFrom);

        return Session.getInstance(properties);
    }

}
