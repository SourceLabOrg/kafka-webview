package com.darksci.kafkaview.manager.email;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.email.EmailSendOneToOneRequest;
import com.darksci.pardot.api.request.prospect.ProspectUpsertRequest;
import com.darksci.pardot.api.response.prospect.Prospect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.Assert;

import javax.activation.FileTypeMap;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation that sends email via Pardot.
 */
public class PardotMailSender implements JavaMailSender {
    private static final Logger logger = LoggerFactory.getLogger(PardotMailSender.class);

    private Properties javaMailProperties = new Properties();

    private Session session;

    private String protocol;

    private String host;

    private String username;

    private String password;

    private String defaultEncoding;

    private FileTypeMap defaultFileTypeMap;

    private final PardotMailSenderConfig configuration;

    public PardotMailSender(final PardotMailSenderConfig configuration) {
        this.configuration = configuration;
    }

    /**
     * Set the JavaMail {@code Session}, possibly pulled from JNDI.
     * <p>Default is a new {@code Session} without defaults, that is
     * completely configured via this instance's properties.
     * <p>If using a pre-configured {@code Session}, non-default properties
     * in this instance will override the settings in the {@code Session}.
     */
    public synchronized void setSession(Session session) {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    /**
     * Return the JavaMail {@code Session},
     * lazily initializing it if hasn't been specified explicitly.
     */
    public synchronized Session getSession() {
        if (this.session == null) {
            this.session = Session.getInstance(this.javaMailProperties);
        }
        return this.session;
    }

    //---------------------------------------------------------------------
    // Implementation of MailSender
    //---------------------------------------------------------------------

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        send(new SimpleMailMessage[] {simpleMessage});
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>(simpleMessages.length);
        for (SimpleMailMessage simpleMessage : simpleMessages) {
            MimeMailMessage message = new MimeMailMessage(createMimeMessage());
            simpleMessage.copyTo(message);
            mimeMessages.add(message.getMimeMessage());
        }
        doSend(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]), simpleMessages);
    }


    //---------------------------------------------------------------------
    // Implementation of JavaMailSender
    //---------------------------------------------------------------------

    /**
     * This implementation creates a SmartMimeMessage, holding the specified
     * default encoding and default FileTypeMap. This special defaults-carrying
     * message will be autodetected by {@link MimeMessageHelper}, which will use
     * the carried encoding and FileTypeMap unless explicitly overridden.
     */
    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(getSession());
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        try {
            return new MimeMessage(getSession(), contentStream);
        } catch (Exception ex) {
            throw new MailParseException("Could not parse raw MIME content", ex);
        }
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        send(new MimeMessage[] {mimeMessage});
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        doSend(mimeMessages, null);
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        send(new MimeMessagePreparator[] {mimeMessagePreparator});
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        try {
            List<MimeMessage> mimeMessages = new ArrayList<>(mimeMessagePreparators.length);
            for (MimeMessagePreparator preparator : mimeMessagePreparators) {
                MimeMessage mimeMessage = createMimeMessage();
                preparator.prepare(mimeMessage);
                mimeMessages.add(mimeMessage);
            }
            send(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]));
        } catch (MailException ex) {
            throw ex;
        } catch (MessagingException ex) {
            throw new MailParseException(ex);
        } catch (Exception ex) {
            throw new MailPreparationException(ex);
        }
    }

    /**
     * Actually send the given array of MimeMessages via JavaMail.
     * @param mimeMessages MimeMessage objects to send
     * @param originalMessages corresponding original message objects
     * that the MimeMessages have been created from (with same array
     * length and indices as the "mimeMessages" array), if any
     * @throws org.springframework.mail.MailAuthenticationException
     * in case of authentication failure
     * @throws org.springframework.mail.MailSendException
     * in case of failure when sending a message
     */
    protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
        // Create new pardot client instance
        final PardotClient pardotClient = getNewPardotClient();

        try {
            // Loop over mime messages
            for (final MimeMessage mimeMessage : mimeMessages) {
                try {
                    // Create new Send Email request
                    final String messageContent = (String) mimeMessage.getContent();
                    final String fromAddress = ((InternetAddress) mimeMessage.getFrom()[0]).getAddress();
                    final String fromName = ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal();
                    final String replyToAddress = ((InternetAddress) mimeMessage.getReplyTo()[0]).getAddress();
                    final String mailTo = ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress();
                    final String subject = mimeMessage.getSubject();

                    // First create prospect
                    final Prospect prospect = new Prospect();
                    prospect.setEmail(mailTo);
                    prospect.setCampaignId(configuration.getCampaignId());

                    // Attempt upsert prospect to ensure it exists first
                    final ProspectUpsertRequest upsertRequest = new ProspectUpsertRequest()
                        .withProspect(prospect);
                    pardotClient.prospectUpsert(upsertRequest);

                    // Now Define email
                    final EmailSendOneToOneRequest request = new EmailSendOneToOneRequest()
                        .withFromNameAndEmail(fromName, fromAddress)
                        .withOperationalEmail(true)
                        .withName(subject + " : " + System.currentTimeMillis())
                        .withSubject(subject)
                        .withReplyToEmail(replyToAddress)
                        .withHtmlContent(messageContent)
                        .withTextContent("no-text-content")
                        .withProspectEmail(mailTo)
                        .withCampaignId(configuration.getCampaignId());

                    // Send email
                    pardotClient.emailSendOneToOne(request);
                } catch (Exception e) {
                    logger.error("Caught exception: {}", e.getMessage(), e);
                }
            }
        } finally {
            pardotClient.close();
        }
    }

    private PardotClient getNewPardotClient() {
        return new PardotClient(configuration.getPardotConfig());
    }
}
