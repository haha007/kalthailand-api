package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.exeption.EmailException;
import th.co.krungthaiaxa.api.common.utils.StringUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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
import java.util.List;
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

    public void sendEmail(String fromEmailAddress, String toEmailAddress, String emailSubject, String emailContent,
            List<Pair<byte[], String>> images, List<Pair<byte[], String>> attachments) {
        String maskFromMail = StringUtil.maskEmail(fromEmailAddress);
        String maskToMail = StringUtil.maskEmail(toEmailAddress);
        String msg = String.format("Send email [start]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
        LOGGER.info(msg);

        hasText(smtpHost, "smtpHost is a mandatory value and cannot be null/blank");
        hasText(smtpPort, "smtpPort is a mandatory value and cannot be null/blank");
        try {
            send(generateMessage(fromEmailAddress, toEmailAddress, emailSubject, emailContent, images, attachments));
            msg = String.format("Send email [success]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
            LOGGER.info(msg);
        } catch (MessagingException | IOException e) {
            msg = String.format("Send email [error]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
            throw new EmailException(msg, e);
        }
    }

    public void sendEmailCC(String fromEmailAddress, String toEmailAddress, String ccEmailAddress, 
            String emailSubject, String emailContent) {
        String maskFromMail = StringUtil.maskEmail(fromEmailAddress);
        String maskToMail = StringUtil.maskEmail(toEmailAddress);
        String msg = String.format("Send email [start]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
        LOGGER.info(msg);

        hasText(smtpHost, "smtpHost is a mandatory value and cannot be null/blank");
        hasText(smtpPort, "smtpPort is a mandatory value and cannot be null/blank");
        try {
            send(generateMessageCC(fromEmailAddress, toEmailAddress, ccEmailAddress, emailSubject, emailContent));
            msg = String.format("Send email [success]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
            LOGGER.info(msg);
        } catch (MessagingException | IOException e) {
            msg = String.format("Send email [error]: fromEmail: '%s', toEmail: '%s'", maskFromMail, maskToMail);
            throw new EmailException(msg, e);
        }
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
        multipart.addBodyPart(getContentBodyPart(emailContent, "text/html;charset=UTF-8"));

        // Get an SMTP Session Object based on the properties defined
        Session session = getSession(fromEmailAddress);

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

    private MimeMessage generateMessageCC(String fromEmailAddress, String toEmailAddress, String ccEmailAddress, 
            String emailSubject, String emailContent)
            throws MessagingException, IOException {

        notNull(fromEmailAddress, "email from is required");
        notNull(toEmailAddress, "At least one 'to' email address required");
        hasText(emailSubject, "emailSubject is required");
        hasText(emailContent, "emailContent is required");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(getContentBodyPart(emailContent, "text/html;charset=UTF-8"));

        // Get an SMTP Session Object based on the properties defined
        Session session = getSession(fromEmailAddress);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(fromEmailAddress));
        message.setHeader("Content-Type", "text/html; charset=UTF-8");
        message.setHeader("Content-Transfer-Encoding", "quoted-printable");
        message.setRecipients(Message.RecipientType.TO, toEmailAddress);
        message.setRecipients(Message.RecipientType.CC, ccEmailAddress);

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
        DataSource source = new ByteArrayDataSource(image.getLeft(), "image/jpeg");
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
