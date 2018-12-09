package org.sourcelab.kafka.webview.ui.manager.sasl;

/**
 * Value class for Sasl properties.
 */
public class SaslProperties {
    private final String plainUsername;
    private final String plainPassword;
    private final String mechanism;
    private final String jaas;

    /**
     * Constructor.
     * @param plainUsername username used in Plain.
     * @param plainPassword password used in Plain.
     * @param mechanism Name of mechanism to use.
     * @param jaas Jaas configuration.
     */
    public SaslProperties(final String plainUsername, final String plainPassword, final String mechanism, final String jaas) {
        this.plainUsername = plainUsername;
        this.plainPassword = plainPassword;
        this.mechanism = mechanism;
        this.jaas = jaas;
    }

    public String getPlainUsername() {
        return plainUsername;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public String getMechanism() {
        return mechanism;
    }

    public String getJaas() {
        return jaas;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder class for SaslProperties.
     */
    public static final class Builder {
        private String plainUsername = "";
        private String plainPassword = "";
        private String mechanism = "";
        private String jaas = "";

        public Builder withPlainUsername(final String plainUsername) {
            this.plainUsername = plainUsername;
            return this;
        }

        public Builder withPlainPassword(final String plainPassword) {
            this.plainPassword = plainPassword;
            return this;
        }

        public Builder withMechanism(final String mechanism) {
            this.mechanism = mechanism;
            return this;
        }

        public Builder withJaas(final String jaas) {
            this.jaas = jaas;
            return this;
        }

        public SaslProperties build() {
            return new SaslProperties(plainUsername, plainPassword, mechanism, jaas);
        }
    }
}
