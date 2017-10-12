package com.darksci.kafkaview.manager.email;

import com.darksci.kafkaview.configuration.AppProperties;
import com.darksci.kafkaview.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class MailContentBuilder {

    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Autowired
    public MailContentBuilder(final TemplateEngine templateEngine, final AppProperties appProperties) {
        this.templateEngine = templateEngine;
        this.appProperties = appProperties;
    }

    /**
     * Generate new lostPassword Email content
     * @param resetToken The user's password reset token
     * @param emailSubject The subject of the email.
     * @return Parsed String representation of template.
     */
    public String lostPasswordEmailContent(final String resetToken, final String emailSubject) {
        // Define what template we're using
        final String templateName = "emailTemplates/LostPasswordEmailTemplate";

        // Create variable replacement context
        final Context context = newDefaultContext(emailSubject);

        // Set variables specific to this template
        context.setVariable("RESET_TOKEN", resetToken);

        // Process it
        return templateEngine.process(templateName, context);
    }

    public String newUserEmailContent(final User user, final String emailSubject) {
        // Define what template we're using
        final String templateName = "emailTemplates/NewUserEmailTemplate";

        // Create variable replacement context
        final Context context = newDefaultContext(emailSubject);

        // Set variables specific to this template
        context.setVariable("DISPLAY_NAME", user.getDisplayName());

        // Process it
        return templateEngine.process(templateName, context);
    }

    private Context newDefaultContext(final String emailSubject) {
        // Create context
        Context context = new Context();

        // Populate app properties
        for (Map.Entry<String, String> entry: appProperties.getGlobalProperties().entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        // Populate standard template vars
        context.setVariable("EMAIL_SUBJECT", emailSubject);
        context.setVariable("PREHEADER_CONTENT_LEFT", "");
        context.setVariable("PREHEADER_CONTENT_RIGHT", "");
        context.setVariable("HEADER_IMAGE", "http://gallery.mailchimp.com/2425ea8ad3/images/header_placeholder_600px.png");
        context.setVariable("CURRENT_YEAR", "2017");
        context.setVariable("COMPANY_NAME", appProperties.getCompanyName());
        context.setVariable("COMPANY_ADDRESS", appProperties.getCompanyAddress());

        return context;
    }
}
