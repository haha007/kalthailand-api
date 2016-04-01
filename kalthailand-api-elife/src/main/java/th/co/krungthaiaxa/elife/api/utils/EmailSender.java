package th.co.krungthaiaxa.elife.api.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static javax.mail.Transport.send;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

@Component
public class EmailSender {
    private final static Logger logger = LoggerFactory.getLogger(EmailSender.class);
    @Value("${email.smtp.server}")
    private String smtpHost;
    @Value("${email.smtp.port}")
    private String smtpPort;

    public void sendEmail(String fromEmailAddress, String toEmailAddress, String emailSubject, String emailContent,
                          List<Pair<byte[], String>> images, List<Pair<byte[], String>> attachments)
            throws MessagingException, IOException {
        hasText(smtpHost, "smtpHost is a mandatory value and cannot be null/blank");
        hasText(smtpPort, "smtpPort is a mandatory value and cannot be null/blank");

        send(generateMessage(fromEmailAddress, toEmailAddress, emailSubject, emailContent, images, attachments));
        logger.info("Email successfully sent");
    }

    private MimeMessage generateMessage(String fromEmailAddress, String toEmailAddress, String emailSubject,
                                        String emailContent, List<Pair<byte[], String>> images,
                                        List<Pair<byte[], String>> attachments)
            throws MessagingException, IOException {

        notNull(fromEmailAddress, "email from is required");
        notNull(toEmailAddress, "At least one 'to' email address required");
        hasText(emailSubject, "emailSubject is required");
        hasText(emailContent, "emailContent is required");

        Multipart multipart = new MimeMultipart();
        for (Pair<byte[], String> image : images) {
            multipart.addBodyPart(getImageBodyPart(image));
        }
        if (null != attachments) {
            for (Pair<byte[], String> attachment : attachments) {
                multipart.addBodyPart(getAttachmentBodyPart(attachment));
            }
        }
        multipart.addBodyPart(getContentBodyPart(emailContent, "text/html;charset=utf-8"));

        // Get an SMTP Session Object based on the properties defined
        Session session = getSession(fromEmailAddress);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(fromEmailAddress));
        //message.setHeader("Content-Type", "text/html; charset=UTF-8");
        message.setHeader("Content-Type", "text/html; charset=ISO-8859-7");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");
        message.setRecipients(Message.RecipientType.TO, toEmailAddress);

        // Set Subject: header field
        message.setSubject(MimeUtility.encodeText(emailSubject, "UTF-8", "Q"));

        message.setContent(multipart);
        message.setSentDate(new Date());
        return message;
    }

    private BodyPart getContentBodyPart(String content, String encoding) throws MessagingException, IOException {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(content, encoding)));
        return messageBodyPart;
    }

    private BodyPart getImageBodyPart(Pair<byte[], String> image) throws MessagingException {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(image.getLeft(), "application/octet-stream");
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.addHeader("Content-ID", image.getRight());
        return messageBodyPart;
    }

    private BodyPart getAttachmentBodyPart(Pair<byte[], String> attachment) throws MessagingException {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(attachment.getLeft(), "application/pdf");
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(attachment.getRight());
        return messageBodyPart;
    }

    private Session getSession(String smtpFrom) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "false");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.from", smtpFrom);

        return Session.getInstance(properties);
    }

}
