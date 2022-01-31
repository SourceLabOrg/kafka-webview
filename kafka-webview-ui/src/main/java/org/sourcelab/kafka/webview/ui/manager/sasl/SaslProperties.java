/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
