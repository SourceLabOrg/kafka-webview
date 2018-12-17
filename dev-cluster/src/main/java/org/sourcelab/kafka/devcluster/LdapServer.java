/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.devcluster;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test LDAP server for usage during development.
 */
public class LdapServer {
    private static final Logger logger = LoggerFactory.getLogger(LdapServer.class);

    /**
     * Main app entry point.
     * @param args command line arguments (none).
     */
    public static void main(final String[] args) throws LDAPException, InterruptedException {
        // Create a base configuration for the server.
        final InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(
            "dc=springframework,dc=org"
        );

        // Create new listener
        config.setListenerConfigs(
            InMemoryListenerConfig.createLDAPConfig("LDAP", 55555)
        );

        final String ldifFile = LdapServer.class.getClassLoader().getResource("test-server.ldif").getFile();

        // Create and start the server instance and populate it with an initial set
        // of data from an LDIF file.
        InMemoryDirectoryServer server = new InMemoryDirectoryServer(config);
        server.importFromLDIF(true, ldifFile);

        // Start the server so it will accept client connections.
        server.startListening();

        logger.info(
            "Server running at: {}:{}",
            server.getConnection("LDAP").getConnectedAddress(),
            server.getConnection("LDAP").getConnectedPort()
        );

        Thread.currentThread().join();

        // Shut down the server so that it will no longer accept client
        // connections, and close all existing connections.
        server.shutDown(true);
    }
}
