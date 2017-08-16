package com.dsc.wai.configuration;

import com.dsc.wai.manager.email.PardotMailSender;
import com.dsc.wai.manager.email.PardotMailSenderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Sets up Email Sending via Pardot.
 */
@Configuration
public class EmailSenderConfig {
    @Value("${pardot.username}")
    private String pardotUsername;

    @Value("${pardot.password}")
    private String pardotPassword;

    @Value("${pardot.user_token}")
    private String pardotUserToken;

    @Value("${pardot.default_campaign_id}")
    private Long pardotDefaultCampaignId;

    /**
     * Our JavaMailSender Implementation.
     */
    private JavaMailSender javaMailSender;

    /**
     * Inject the Pardot Email Sender Implementation.
     * Comment this out to use standard one.
     */
    @Bean
    @Primary
    public JavaMailSender getJavaMailSender() {
        if (javaMailSender == null) {
            // Create Pardot Config
            com.darksci.pardot.api.Configuration pardotConfig = new com.darksci.pardot.api.Configuration(pardotUsername, pardotPassword, pardotUserToken);

            // Create PardotMailSender Config
            PardotMailSenderConfig config = new PardotMailSenderConfig(pardotConfig, 14885L);

            // Create PardotMailSender implementation
            javaMailSender = new PardotMailSender(config);
        }
        return javaMailSender;
    }
}
