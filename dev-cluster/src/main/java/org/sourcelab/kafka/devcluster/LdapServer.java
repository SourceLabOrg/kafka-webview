package org.sourcelab.kafka.devcluster;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.LDAPTestUtils;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

/**
 * A test LDAP server.
 */
public class LdapServer {
    private final static Logger logger = LoggerFactory.getLogger(LdapServer.class);

    public static void main(final String[] args) throws LDAPException, GeneralSecurityException, InterruptedException {
        // Create a base configuration for the server.
        final InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=springframework,dc=org");
        //config.setSchema(null);

        // Add Users
//        config.addAdditionalBindCredentials("cn=Admin User", "admin-secret");
//        config.addAdditionalBindCredentials("cn=User", "user-secret");

        // Update the configuration to support LDAP (with StartTLS) and LDAPS
        // listeners.
//        final SSLUtil serverSSLUtil = new SSLUtil(
//            new KeyStoreKeyManager(serverKeyStorePath, serverKeyStorePIN, "JKS", "server-cert"),
//            new TrustStoreTrustManager(serverTrustStorePath)
//        );
//        final SSLUtil clientSSLUtil = new SSLUtil(new TrustStoreTrustManager(clientTrustStorePath));
//        config.setListenerConfigs(
//            InMemoryListenerConfig.createLDAPConfig("LDAP", // Listener name
//                null, // Listen address. (null = listen on all interfaces)
//                0, // Listen port (0 = automatically choose an available port)
//                serverSSLUtil.createSSLSocketFactory()), // StartTLS factory
//            InMemoryListenerConfig.createLDAPSConfig("LDAPS", // Listener name
//                null, // Listen address. (null = listen on all interfaces)
//                0, // Listen port (0 = automatically choose an available port)
//                serverSSLUtil.createSSLServerSocketFactory(), // Server factory
//                clientSSLUtil.createSSLSocketFactory())); // Client factory


        // Create new listener
        config.setListenerConfigs(
            InMemoryListenerConfig.createLDAPConfig("LDAP", 0)
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
