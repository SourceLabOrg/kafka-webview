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

package org.sourcelab.kafka.webview.ui.controller.configuration.cluster;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.FileTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ClusterConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private SaslUtility saslUtility;

    /**
     * Where JKS files are uploaded to.
     */
    private String keyStoreUploadPath;

    @Before
    public void setupUploadPath() {
        keyStoreUploadPath = uploadPath + "/keyStores/";
    }

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/cluster", false);
        testUrlWithOutAdminRole("/configuration/filter/create", false);
        testUrlWithOutAdminRole("/configuration/filter/edit/1", false);
        testUrlWithOutAdminRole("/configuration/filter/update", true);
        testUrlWithOutAdminRole("/configuration/filter/delete/1", true);
    }

    /**
     * Smoke test the Cluster Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some dummy clusters
        final Cluster cluster1 = clusterTestTools.createCluster("My Test Cluster 1 2 3");
        final Cluster cluster2 = clusterTestTools.createCluster("Some other Cluster");

        // Hit index.
        mockMvc
            .perform(get("/configuration/cluster").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate cluster 1
            .andExpect(content().string(containsString(cluster1.getName())))
            .andExpect(content().string(containsString(cluster1.getBrokerHosts())))

            // Validate cluster 2
            .andExpect(content().string(containsString(cluster2.getName())))
            .andExpect(content().string(containsString(cluster2.getBrokerHosts())));
    }

    /**
     * Smoke test the Cluster create page.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/cluster/create")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk());

    }

    /**
     * Test creating new non-ssl cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_newCluster() throws Exception {
        final String expectedClusterName = "My New Cluster Name";
        final String expectedBrokerHosts = "localhost:9092";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findByName(expectedClusterName);
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        validateNonSaslCluster(cluster);
    }

    /**
     * Test updating non-ssl cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingNonSslCluster() throws Exception {
        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        clusterRepository.save(originalCluster);

        final String expectedClusterName = "My New Cluster Name" + System.currentTimeMillis();
        final String expectedBrokerHosts = "newHost:9092";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should be set back to NOT valid", false, cluster.isValid());
        validateNonSaslCluster(cluster);
    }

    /**
     * Test creating new ssl cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_newSslCluster() throws Exception {
        final String expectedClusterName = "My New Cluster Name" + System.currentTimeMillis();
        final String expectedBrokerHosts = "localhost:9092";
        final String expectedKeystorePassword = "KeyStorePassword";
        final String expectedTruststorePassword = "TrustStorePassword";

        final MockMultipartFile trustStoreUpload = new MockMultipartFile("trustStoreFile", "TrustStoreFile".getBytes(Charsets.UTF_8));
        final MockMultipartFile keyStoreUpload = new MockMultipartFile("keyStoreFile", "KeyStoreFile".getBytes(Charsets.UTF_8));

        // Hit create page.
        mockMvc
            .perform(fileUpload("/configuration/cluster/update")
                .file(trustStoreUpload)
                .file(keyStoreUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("ssl", "true")
                .param("trustStorePassword", expectedTruststorePassword)
                .param("keyStorePassword", expectedKeystorePassword)
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findByName(expectedClusterName);
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertNotNull("Should have passwords set", cluster.getTrustStorePassword());
        assertNotNull("Should have passwords set", cluster.getKeyStorePassword());
        assertNotNull("Should have trust store file path", cluster.getTrustStoreFile());
        assertNotNull("Should have key store file path", cluster.getKeyStoreFile());
        validateNonSaslCluster(cluster);

        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been uploaded", doesKeystoreFileExist);
        final String keyStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have correct contents", "KeyStoreFile", keyStoreContents);

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("trustStore file should have been uploaded", doesTruststoreFileExist);
        final String trustStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
        assertEquals("TrustStore file should have correct contents", "TrustStoreFile", trustStoreContents);

        // Cleanup
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
    }

    /**
     * Test updating existing ssl cluster, but not uploading new JKS files or updating passwords.
     * This should update the cluster, but leave the existing files and passwords in place.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingSslCluster() throws Exception {
        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        originalCluster.setSslEnabled(true);
        originalCluster.setKeyStorePassword("DummyKeyStorePassword");
        originalCluster.setKeyStoreFile("Cluster.KeyStore.jks");
        originalCluster.setTrustStorePassword("DummyTrustStorePassword");
        originalCluster.setTrustStoreFile("Cluster.TrustStore.jks");
        clusterRepository.save(originalCluster);

        // Create dummy JKS files
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

        // Only update cluster name, brokers, keep SSL enabled.
        final String expectedClusterName = "My Updated Cluster Name" + System.currentTimeMillis();
        final String expectedBrokerHosts = "updatedHost:9092";

        // Hit create page.
        mockMvc
            .perform(fileUpload("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("ssl", "true")
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Password should be unchanged", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have trust store file path", "Cluster.TrustStore.jks", cluster.getTrustStoreFile());
        assertEquals("Should have key store file path", "Cluster.KeyStore.jks", cluster.getKeyStoreFile());
        validateNonSaslCluster(cluster);

        // Validate file exists
        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been left untouched", doesKeystoreFileExist);
        final String keyStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have remained untouched", "KeyStoreFile", keyStoreContents);

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("trustStore file should have been left untouched", doesTruststoreFileExist);
        final String trustStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
        assertEquals("TrustStore file should have remained untouched", "TrustStoreFile", trustStoreContents);

        // Cleanup
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
    }

    /**
     * Test updating existing ssl cluster, updating the TrustStore file only.
     * This should leave the keystore file/password as is.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingSslClusterUpdateTrustStore() throws Exception {
        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        originalCluster.setSslEnabled(true);
        originalCluster.setKeyStorePassword("DummyKeyStorePassword");
        originalCluster.setKeyStoreFile("Cluster.KeyStore.jks");
        originalCluster.setTrustStorePassword("DummyTrustStorePassword");
        originalCluster.setTrustStoreFile("Cluster.TrustStore.jks");
        clusterRepository.save(originalCluster);

        // Create dummy JKS files
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

        // Only update cluster name, brokers, keep SSL enabled, and TrustStore file + password
        final String expectedClusterName = "UpdatedClusterName" + System.currentTimeMillis();
        final String expectedBrokerHosts = "updatedHost:9092";

        final MockMultipartFile trustStoreUpload = new MockMultipartFile("trustStoreFile", "UpdatedTrustStoreFile".getBytes(Charsets.UTF_8));

        // Hit create page.
        mockMvc
            .perform(fileUpload("/configuration/cluster/update")
                .file(trustStoreUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("ssl", "true")
                .param("trustStorePassword", "NewPassword")
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have key store file path", "Cluster.KeyStore.jks", cluster.getKeyStoreFile());
        validateNonSaslCluster(cluster);

        // Keystore should remain
        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been left untouched", doesKeystoreFileExist);
        final String keyStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have remained untouched", "KeyStoreFile", keyStoreContents);

        // TrustStore was updated
        assertNotEquals("Password should be changed", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Should have trust store file path", expectedClusterName + ".truststore.jks", cluster.getTrustStoreFile());

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        assertTrue("trustStore file should exist", doesTruststoreFileExist);
        final String trustStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
        assertEquals("TrustStore file should have been updated", "UpdatedTrustStoreFile", trustStoreContents);

        // Cleanup
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
    }

    /**
     * Test updating existing ssl cluster, updating the KeyStore file only.
     * This should leave the truststore file/password as is.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingSslClusterUpdateKeyStore() throws Exception {
        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        originalCluster.setSslEnabled(true);
        originalCluster.setKeyStorePassword("DummyKeyStorePassword");
        originalCluster.setKeyStoreFile("Cluster.KeyStore.jks");
        originalCluster.setTrustStorePassword("DummyTrustStorePassword");
        originalCluster.setTrustStoreFile("Cluster.TrustStore.jks");
        clusterRepository.save(originalCluster);

        // Create dummy JKS files
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

        // Only update cluster name, brokers, keep SSL enabled, and KeyStore file + password
        final String expectedClusterName = "UpdatedClusterName" + System.currentTimeMillis();
        final String expectedBrokerHosts = "updatedHost:9092";

        final MockMultipartFile keyStoreUpload = new MockMultipartFile("keyStoreFile", "UpdatedKeyStoreFile".getBytes(Charsets.UTF_8));

        // Hit create page.
        mockMvc
            .perform(fileUpload("/configuration/cluster/update")
                .file(keyStoreUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("ssl", "true")
                .param("keyStorePassword", "NewPassword")
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Should have trust store file path", "Cluster.TrustStore.jks", cluster.getTrustStoreFile());
        validateNonSaslCluster(cluster);

        // trust store should remain
        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        assertTrue("TrustStore file should have been left untouched", doesTruststoreFileExist);
        final String trustStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
        assertEquals("TrustStore file should have remained untouched", "TrustStoreFile", trustStoreContents);

        // KeyStore was updated
        assertNotEquals("Password should be changed", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have key store file path", expectedClusterName + ".keystore.jks", cluster.getKeyStoreFile());

        final boolean doesKeyStoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("keyStore file should exist", doesKeyStoreFileExist);
        final String keyStoreContents = FileTestTools.readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have been updated", "UpdatedKeyStoreFile", keyStoreContents);

        // Cleanup
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        Files.deleteIfExists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
    }

    /**
     * Test updating existing ssl cluster making it non-ssl.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingSslClusterUpdateToNonSsl() throws Exception {
        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        originalCluster.setSslEnabled(true);
        originalCluster.setKeyStorePassword("DummyKeyStorePassword");
        originalCluster.setKeyStoreFile("Cluster.KeyStore.jks");
        originalCluster.setTrustStorePassword("DummyTrustStorePassword");
        originalCluster.setTrustStoreFile("Cluster.TrustStore.jks");
        clusterRepository.save(originalCluster);

        // Create dummy JKS files
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        FileTestTools.createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

        final Path keyStoreFilePath = Paths.get(keyStoreUploadPath + originalCluster.getKeyStoreFile());
        final Path trustStoreFilePath = Paths.get(keyStoreUploadPath + originalCluster.getTrustStoreFile());

        // Only update cluster name, brokers, disable SSL enabled
        final String expectedClusterName = "UpdatedClusterName" + System.currentTimeMillis();
        final String expectedBrokerHosts = "updatedHost:9092";

        // Hit create page.
        mockMvc
            .perform(fileUpload("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl disabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertNull("Password should be null", cluster.getTrustStorePassword());
        assertNull("Password should be null", cluster.getKeyStorePassword());
        assertNull("File reference should be null", cluster.getTrustStoreFile());
        assertNull("File reference should be null", cluster.getKeyStorePassword());
        validateNonSaslCluster(cluster);

        // trust store file should not exist
        final boolean doesTruststoreFileExist = Files.exists(trustStoreFilePath);
        assertFalse("TrustStore file should have been removed", doesTruststoreFileExist);

        // KeyStore file should not exist
        final boolean doesKeyStoreFileExist = Files.exists(keyStoreFilePath);
        assertFalse("keyStore file should have been removed", doesKeyStoreFileExist);
    }

    /**
     * Test creating new sasl PLAIN cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_newSaslPlain_Cluster() throws Exception {
        final String expectedClusterName = "My New Cluster Name";
        final String expectedBrokerHosts = "localhost:9092";
        final String expectedSaslMechanism = "PLAIN";
        final String expectedSaslUsername = "USERname";
        final String expectedSaslPassword = "PASSword";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("sasl", "true")
                .param("saslMechanism", expectedSaslMechanism)
                .param("saslUsername", expectedSaslUsername)
                .param("saslPassword", expectedSaslPassword)
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findByName(expectedClusterName);
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());

        // Validate sasl properties
        assertEquals("Should have sasl enabled", true, cluster.isSaslEnabled());
        assertEquals("Should have correct mechanism", expectedSaslMechanism, cluster.getSaslMechanism());
        assertNotNull("Should have non-null sasl config", cluster.getSaslConfig());

        // Attempt to decode encrypted config to validate
        final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);
        assertNotNull("Should be non-null", saslProperties);
        assertEquals("Should have expected mechanism", expectedSaslMechanism, saslProperties.getMechanism());
        assertEquals("Should have expected username", expectedSaslUsername, saslProperties.getPlainUsername());
        assertEquals("Should have expected password", expectedSaslPassword, saslProperties.getPlainPassword());
        assertEquals("Should have empty custom jaas", "", saslProperties.getJaas());
    }

    /**
     * Test creating new sasl GSSAPI cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_newSaslGSSAPI_Cluster() throws Exception {
        final String expectedClusterName = "My New Cluster Name";
        final String expectedBrokerHosts = "localhost:9092";
        final String expectedSaslMechanism = "GSSAPI";
        final String expectedSaslUsername = "";
        final String expectedSaslPassword = "";
        final String expectedSaslJaas = "This is my custom jaas \n and another line";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("sasl", "true")
                .param("saslMechanism", expectedSaslMechanism)
                .param("saslCustomJaas", expectedSaslJaas)
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findByName(expectedClusterName);
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());

        // Validate sasl properties
        assertEquals("Should have sasl enabled", true, cluster.isSaslEnabled());
        assertEquals("Should have correct mechanism", expectedSaslMechanism, cluster.getSaslMechanism());
        assertNotNull("Should have non-null sasl config", cluster.getSaslConfig());

        // Attempt to decode encrypted config to validate
        final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);
        assertNotNull("Should be non-null", saslProperties);
        assertEquals("Should have expected mechanism", expectedSaslMechanism, saslProperties.getMechanism());
        assertEquals("Should have expected username", expectedSaslUsername, saslProperties.getPlainUsername());
        assertEquals("Should have expected password", expectedSaslPassword, saslProperties.getPlainPassword());
        assertEquals("Should have expected custom jaas", expectedSaslJaas, saslProperties.getJaas());
    }

    /**
     * Test creating new sasl custom cluster.
     */
    @Test
    @Transactional
    public void testPostUpdate_newSaslCustom_Cluster() throws Exception {
        final String expectedClusterName = "My New Cluster Name";
        final String expectedBrokerHosts = "localhost:9092";
        final String expectedSaslMechanism = "CustomThing";
        final String expectedSaslUsername = "";
        final String expectedSaslPassword = "";
        final String expectedSaslJaas = "This is my custom jaas \n and another line";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts)
                .param("sasl", "true")
                .param("saslMechanism", "custom")
                .param("saslCustomMechanism", expectedSaslMechanism)
                .param("saslCustomJaas", expectedSaslJaas)
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findByName(expectedClusterName);
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());

        // Validate sasl properties
        assertEquals("Should have sasl enabled", true, cluster.isSaslEnabled());
        assertEquals("Should have correct mechanism", expectedSaslMechanism, cluster.getSaslMechanism());
        assertNotNull("Should have non-null sasl config", cluster.getSaslConfig());

        // Attempt to decode encrypted config to validate
        final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);
        assertNotNull("Should be non-null", saslProperties);
        assertEquals("Should have expected mechanism", expectedSaslMechanism, saslProperties.getMechanism());
        assertEquals("Should have expected username", expectedSaslUsername, saslProperties.getPlainUsername());
        assertEquals("Should have expected password", expectedSaslPassword, saslProperties.getPlainPassword());
        assertEquals("Should have expected custom jaas", expectedSaslJaas, saslProperties.getJaas());
    }

    /**
     * Test updating SASL cluster and make it non-sasl clears out properties relating to SASL.
     */
    @Test
    @Transactional
    public void testPostUpdate_existingSaslCluster() throws Exception {
        final SaslProperties saslProperties = SaslProperties.newBuilder()
            .withJaas("Custom Jaas")
            .withMechanism("PLAIN")
            .withPlainUsername("USERNAME")
            .withPlainPassword("PASSWORD")
            .build();

        // Create an existing cluster
        final Cluster originalCluster = clusterTestTools.createCluster("My New Cluster");
        originalCluster.setValid(true);
        originalCluster.setSaslEnabled(true);
        originalCluster.setSaslMechanism("PLAIN");
        originalCluster.setSaslConfig(
            saslUtility.encryptProperties(saslProperties)
        );
        clusterRepository.save(originalCluster);

        final String expectedClusterName = "My New Cluster Name" + System.currentTimeMillis();
        final String expectedBrokerHosts = "newHost:9092";

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/cluster/update")
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(originalCluster.getId()))
                .param("name", expectedClusterName)
                .param("brokerHosts", expectedBrokerHosts))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/cluster"));

        // Lookup Cluster
        final Cluster cluster = clusterRepository.findById(originalCluster.getId()).get();
        assertNotNull("Should have cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should be set back to NOT valid", false, cluster.isValid());
        validateNonSaslCluster(cluster);
    }

    /**
     * Utility method for validating a cluster is not configured with SASL.
     * @param cluster cluster to validate.
     */
    private void validateNonSaslCluster(final Cluster cluster) {
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should be set back to NOT valid", false, cluster.isValid());
        assertEquals("Should not be sasl enabled", false, cluster.isSaslEnabled());
    }
}