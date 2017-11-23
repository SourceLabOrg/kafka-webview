package org.sourcelab.kafka.webview.ui.controller.cluster;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
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
public class ClusterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestTools userTestTools;

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private AppProperties appProperties;

    /**
     * Authentication details.
     */
    private UserDetails adminUserDetails;
    private UserDetails userDetails;

    /**
     * Where JKS files are uploaded to.
     */
    private String keyStoreUploadPath;

    @Before
    public void setupAuth() {
        // Create user details for logged in user.
        adminUserDetails = customUserDetailsService.loadUserByUsername(userTestTools.createAdminUser().getEmail());
        userDetails = customUserDetailsService.loadUserByUsername(userTestTools.createUser().getEmail());

        // Define where JKS files are uploaded to.
        final String baseUploadPath = appProperties.getUploadPath();
        keyStoreUploadPath = baseUploadPath + "/keyStores/";
    }

    /**
     * Test cannot load index page w/o admin role.
     */
    @Test
    @Transactional
    public void testIndex_withoutAdminRole() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/cluster").with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    /**
     * Test cannot create page w/o admin role.
     */
    @Test
    @Transactional
    public void testCreate_withoutAdminRole() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/create").with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    /**
     * Test cannot update w/o admin role.
     */
    @Test
    @Transactional
    public void testUpdate_withoutAdminRole() throws Exception {
        // Hit index.
        mockMvc
            .perform(post("/configuration/update").with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isForbidden());
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
        final Cluster cluster = clusterRepository.findOne(originalCluster.getId());
        assertNotNull("Should have cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should not be ssl enabled", false, cluster.isSslEnabled());
        assertEquals("Should be set back to NOT valid", false, cluster.isValid());
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

        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been uploaded", doesKeystoreFileExist);
        final String keyStoreContents = readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have correct contents", "KeyStoreFile", keyStoreContents);

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("trustStore file should have been uploaded", doesTruststoreFileExist);
        final String trustStoreContents = readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
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
        createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

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
        final Cluster cluster = clusterRepository.findOne(originalCluster.getId());
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Password should be unchanged", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have trust store file path", "Cluster.TrustStore.jks", cluster.getTrustStoreFile());
        assertEquals("Should have key store file path", "Cluster.KeyStore.jks", cluster.getKeyStoreFile());

        // Validate file exists
        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been left untouched", doesKeystoreFileExist);
        final String keyStoreContents = readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have remained untouched", "KeyStoreFile", keyStoreContents);

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("trustStore file should have been left untouched", doesTruststoreFileExist);
        final String trustStoreContents = readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
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
        createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

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
        final Cluster cluster = clusterRepository.findOne(originalCluster.getId());
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have key store file path", "Cluster.KeyStore.jks", cluster.getKeyStoreFile());

        // Keystore should remain
        final boolean doesKeystoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("KeyStore file should have been left untouched", doesKeystoreFileExist);
        final String keyStoreContents = readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
        assertEquals("KeyStore file should have remained untouched", "KeyStoreFile", keyStoreContents);

        // TrustStore was updated
        assertNotEquals("Password should be changed", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Should have trust store file path", expectedClusterName + ".truststore.jks", cluster.getTrustStoreFile());

        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        assertTrue("trustStore file should exist", doesTruststoreFileExist);
        final String trustStoreContents = readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
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
        createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

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
        final Cluster cluster = clusterRepository.findOne(originalCluster.getId());
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl enabled", true, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertEquals("Password should be unchanged", "DummyTrustStorePassword", cluster.getTrustStorePassword());
        assertEquals("Should have trust store file path", "Cluster.TrustStore.jks", cluster.getTrustStoreFile());

        // trust store should remain
        final boolean doesTruststoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getTrustStoreFile()));
        assertTrue("TrustStore file should have been left untouched", doesTruststoreFileExist);
        final String trustStoreContents = readFile(keyStoreUploadPath + cluster.getTrustStoreFile());
        assertEquals("TrustStore file should have remained untouched", "TrustStoreFile", trustStoreContents);

        // KeyStore was updated
        assertNotEquals("Password should be changed", "DummyKeyStorePassword", cluster.getKeyStorePassword());
        assertEquals("Should have key store file path", expectedClusterName + ".keystore.jks", cluster.getKeyStoreFile());

        final boolean doesKeyStoreFileExist = Files.exists(Paths.get(keyStoreUploadPath, cluster.getKeyStoreFile()));
        assertTrue("keyStore file should exist", doesKeyStoreFileExist);
        final String keyStoreContents = readFile(keyStoreUploadPath + cluster.getKeyStoreFile());
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
        createDummyFile(keyStoreUploadPath + originalCluster.getKeyStoreFile(), "KeyStoreFile");
        createDummyFile(keyStoreUploadPath + originalCluster.getTrustStoreFile(), "TrustStoreFile");

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
        final Cluster cluster = clusterRepository.findOne(originalCluster.getId());
        assertNotNull("Should have new cluster", cluster);
        assertEquals("Has correct name", expectedClusterName, cluster.getName());
        assertEquals("Has correct brokerHosts", expectedBrokerHosts, cluster.getBrokerHosts());
        assertEquals("Should be ssl disabled", false, cluster.isSslEnabled());
        assertEquals("Should not be valid by default", false, cluster.isValid());
        assertNull("Password should be null", cluster.getTrustStorePassword());
        assertNull("Password should be null", cluster.getKeyStorePassword());
        assertNull("File reference should be null", cluster.getTrustStoreFile());
        assertNull("File reference should be null", cluster.getKeyStorePassword());

        // trust store file should not exist
        final boolean doesTruststoreFileExist = Files.exists(trustStoreFilePath);
        assertFalse("TrustStore file should have been removed", doesTruststoreFileExist);

        // KeyStore file should not exist
        final boolean doesKeyStoreFileExist = Files.exists(keyStoreFilePath);
        assertFalse("keyStore file should have been removed", doesKeyStoreFileExist);
    }

    /**
     * Helper for writing a dummy file.
     * @param filename Filename to write
     * @param contents Contents of the file
     */
    private void createDummyFile(final String filename, final String contents) throws IOException {
        try (final FileOutputStream outputStream = new FileOutputStream(filename)) {
            outputStream.write(contents.getBytes(Charsets.UTF_8));
        }
    }

    /**
     * Utility method to read the contents of a file.
     * @param filename Filename to read
     * @return Contents of the file.
     * @throws IOException
     */
    private String readFile(final String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)), Charsets.UTF_8);
    }
}