/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.autoconf;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.MessageFormatType;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "app.deserializerPath=target/test-classes/testDeserializer"
})
@Transactional
public class AutoconfTest {
    final static String messageFromatName = "FooDeserializer";
    
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();
    
    @Autowired private MessageFormatRepository messageFormatRepository;
    @Autowired private ViewRepository viewRepository;
    @Autowired private WebKafkaConsumerFactory webKafkaConsumerFactory;
    
    @Autowired private ClusterTestTools clusterTestTools;
    
    @AfterClass
    @BeforeClass
    public static void cleanDatabase() {
        
    }
    
    @Test
    public void test_configurationSetup() {
        final String jarFilename = "autoconfPlugin.jar";
        final String classpath = "test.DeserializerDiscoveryServiceTest$FooDeserializer";
        final MessageFormatType messageFormatType = MessageFormatType.AUTOCONF;
        final String optionParameters = "{\"key\":\"value\"}";
        
        MessageFormat messageFormat = messageFormatRepository.findByName(messageFromatName);
        
        assertNotNull(messageFormat);
        assertEquals(classpath, messageFormat.getClasspath());
        assertEquals(jarFilename, messageFormat.getJar());
        assertEquals(messageFormatType, messageFormat.getMessageFormatType());
        assertEquals(optionParameters, messageFormat.getOptionParameters());
    }
    
    @Test
    public void test_checkDeserializer() throws Exception {
        final String topic = "MyTopic";
        final String fooKey = "KEY";
        final int fooValue = 5;
        
        // Create topics and send a data on it
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic, 1, (short) 1);
        
        Map<byte[], byte[]> keysAndValues = new HashMap<>();
        byte[] recordKey = new StringSerializer().serialize(topic, fooKey);
        byte[] recordValue = ByteBuffer.allocate(Integer.BYTES).putInt(fooValue).array();
        keysAndValues.put(recordKey, recordValue);
        sharedKafkaTestResource.getKafkaTestUtils().produceRecords(keysAndValues, topic, 0);
        
        // Create cluster and view.
        final Cluster cluster = clusterTestTools.createCluster("A Cluster", 
            sharedKafkaTestResource.getKafkaConnectString());
        final View view = createDefaultView(topic, cluster);
        
        // Create the WebKafkaConsumer
        final SessionIdentifier sessionId = SessionIdentifier.newWebIdentifier(12L, "MySession");
        WebKafkaConsumer webKafkaConsumer = webKafkaConsumerFactory.createWebClient(view, new ArrayList<>(), sessionId);

        // Consume from WebKafkaConsumer
        webKafkaConsumer.toHead();
        KafkaResults kafkaResults = webKafkaConsumer.consumePerPartition();
        List<KafkaResult> results = kafkaResults.getResults();

        // Check !
        assertEquals(1, results.size());
        KafkaResult kafkaResult = results.get(0);
        Object foo = kafkaResult.getValue();
        Method foo_getValue = foo.getClass().getMethod("getValue", new Class[]{});
        assertEquals(fooKey, kafkaResult.getKey());
        assertEquals(fooValue, foo_getValue.invoke(foo, new Object[]{}));
    }
    
    private View createDefaultView(final String topic, Cluster cluster) {
        // Create MessageFormat for strings
        final MessageFormat keyMessageFormat = messageFormatRepository.findByName("String");
        final MessageFormat valueMessageFormat = messageFormatRepository.findByName(messageFromatName);

        // No filters by default

        // Create a View
        final View view = new View();
        view.setName("A View");
        view.setTopic(topic);
        view.setCluster(cluster);
        view.setKeyMessageFormat(keyMessageFormat);
        view.setValueMessageFormat(valueMessageFormat);
        view.setPartitions("");
        view.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        view.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        viewRepository.save(view);

        return view;
    }
}
