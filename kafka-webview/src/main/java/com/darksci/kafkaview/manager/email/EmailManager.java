package com.darksci.kafkaview.manager.email;

import com.darksci.kafkaview.configuration.AppProperties;
import com.darksci.kafkaview.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Component
public class EmailManager {
    private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);

    private JavaMailSender emailSender;
    private MailContentBuilder mailContentBuilder;
    private AppProperties appProperties;

    @Autowired
    public EmailManager(final JavaMailSender javaMailSender, final MailContentBuilder mailContentBuilder, final AppProperties appProperties) {
        this.emailSender = javaMailSender;
        this.mailContentBuilder = mailContentBuilder;
        this.appProperties = appProperties;
    }

    /**
     * Send a User a lost password Email, which includes a link to reset their password.
     * @param user The user to email.
     * @return true or false if the sending was successful.
     */
    public boolean sendLostPasswordEmail(final User user) {
        final String emailSubject = appProperties.getName() + " Password Reset";

        // Generate template
        final String content = mailContentBuilder.lostPasswordEmailContent(
            user.getResetPasswordHash(),
            emailSubject
        );

        // Send the email
        return sendEmail(user.getEmail(), emailSubject, content);
    }

    /**
     * Send a Welcome email when a new user signs up!
     * @param user The user to email.
     * @return
     */
    public boolean sendWelcomeEmail(final User user) {
        final String emailSubject = "Welcome to " + appProperties.getName() + "!";

        // Generate template
        final String content = mailContentBuilder.newUserEmailContent(
            user,
            emailSubject
        );

        // Send the email
        return sendEmail(user.getEmail(), emailSubject, content);
    }

    private boolean sendEmail(final String toEmail, final String subject, final String content) {
        // Generate message prep
        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(appProperties.getSystemEmail(), appProperties.getName());
            messageHelper.setTo(toEmail);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
        };
        try {
            emailSender.send(messagePreparator);
            return true;
        } catch (MailException e) {
            logger.error("Error Sending Email: {}", e.getMessage(), e);
            return false;
        }
    }
}