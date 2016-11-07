package th.co.krungthaiaxa.api.elife.test.utils;

import com.icegreen.greenmail.junit.GreenMailRule;
import org.apache.commons.io.FileUtils;
import th.co.krungthaiaxa.api.common.exeption.UnexpectedException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khoi.tran on 9/19/16.
 */
public class GreenMailUtil {
    public static String toStringReceiveMessages(GreenMailRule greenMail) {
        MimeMessage[] mailMessages = greenMail.getReceivedMessages();
        List<String> msgs = new ArrayList<>();
        for (MimeMessage mailMessage : mailMessages) {
            try {
                MimeMultipart mimeMultipart = (MimeMultipart) mailMessage.getContent();
                StringBuilder content = new StringBuilder();
                for (int i = 0; i < mimeMultipart.getCount(); i++) {
                    BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                    String partContentType = bodyPart.getContentType();
                    if (partContentType.contains("text/html")) {
//                        content.append(String.format("\t[%s] %s%n", i, partContentType));
                        content.append(bodyPart.getContent());
                    } else if (partContentType.contains("pdf")) {
                        content.append(bodyPart.getFileName());
                    }
                }

                String msg = String.format("-------------------------------%nSubject: '%s'%nContent: '%n%s%n'", mailMessage.getSubject(), content);
                msgs.add(msg);
            } catch (MessagingException | IOException e) {
                throw new UnexpectedException("Cannot write green mail message: " + e.getMessage(), e);
            }
        }
        return msgs.stream().collect(Collectors.joining("%n"));
    }

    /**
     * @param greenMail
     * @param parentFolder example: "target/testresult/emails"
     */
    public static void writeReceiveMessagesToFiles(GreenMailRule greenMail, String parentFolder) {
        MimeMessage[] mailMessages = greenMail.getReceivedMessages();
        List<String> msgs = new ArrayList<>();
        for (MimeMessage mailMessage : mailMessages) {
            writeReceiveMesssageToFile(mailMessage, parentFolder);
        }
    }

    private static void writeReceiveMesssageToFile(MimeMessage mailMessage, String parentFolderPath) {
        try {
            File parentFolder = IOUtil.createFolderIfNecessary(parentFolderPath);
            String fileCoreName = parentFolder + "/" + System.currentTimeMillis() + "_" + mailMessage.getSubject();
            File file = new File(fileCoreName + ".html");
            MimeMultipart mimeMultipart = (MimeMultipart) mailMessage.getContent();
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                String partContentType = bodyPart.getContentType();
                if (partContentType.contains("text/html")) {
                    FileUtils.write(file, "" + bodyPart.getContent());
                    content.append(bodyPart.getContent());
                } else if (partContentType.contains("pdf")) {
                    File attachment = new File(fileCoreName + "_" + bodyPart.getFileName());
                    IOUtil.writeInputStream(attachment, bodyPart.getInputStream());
                }
            }
        } catch (MessagingException | IOException e) {
            throw new UnexpectedException("Cannot write green mail message: " + e.getMessage(), e);
        }
    }
}
