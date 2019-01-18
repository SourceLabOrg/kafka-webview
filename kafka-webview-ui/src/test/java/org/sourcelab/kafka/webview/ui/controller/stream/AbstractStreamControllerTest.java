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

package org.sourcelab.kafka.webview.ui.controller.stream;

import com.salesforce.kafka.test.ProducedKafkaRecord;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.sourcelab.kafka.webview.ui.tools.integration.UserLoginUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Abstract StreamController tests.
 */
public abstract class AbstractStreamControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractStreamControllerTest.class);

    /**
     * Setup broker without SSL or SASL support.
     */
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * The port our instance is running on.
     */
    @Value("${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private ClusterTestTools clusterTestTools;

    /**
     * Utility class for dealing with logging in.
     */
    UserLoginUtility userLoginUtility;

    /**
     * Websocket Url.
     */
    private String WEBSOCKET_URL;

    /**
     * The cluster setup for each test method.
     */
    private Cluster cluster;

    /**
     * View setup for each test method.
     */
    private View view;

    /**
     * Records that were produced into the kafka topic.
     */
    private List<ProducedKafkaRecord<byte[], byte[]>> kafkaRecords;

    /**
     * Handle logging into the app.
     * @param username username to login with.
     * @param password password to login with.
     * @return HttpHeaders from login process.
     */
    public abstract HttpHeaders login(final String username, final String password);

    @Before
    public void setup() {
        // Url of websocket.
        WEBSOCKET_URL = "ws://localhost:" + port + "/websocket";

        // Create a cluster instance.
        cluster = clusterTestTools.createCluster(
            "TestCluster" + System.currentTimeMillis(),
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a topic
        final String topicName = "TestTopic" + System.currentTimeMillis();
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Produce 10 records into that topic.
        kafkaRecords = sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);

        // Create view for our topic.
        view = viewTestTools.createView("MyTestView" + System.currentTimeMillis());
        view.setCluster(cluster);
        view.setTopic(topicName);
        view.setPartitions("0");
        viewTestTools.save(view);

        // Create user login utility instance.
        userLoginUtility = new UserLoginUtility("http://localhost:" + port, "/login", restTemplate);
    }

    /**
     * Attempts to make a websocket connection as an anonymous user to verify it works as expected.
     */
    @Test
    public void test_webSocketConnection() throws InterruptedException {
        // Create a count down latch to know when we have consumed all of our records.
        final CountDownLatch countDownLatch = new CountDownLatch(kafkaRecords.size());

        // Create a list we can add our consumed records to
        final List<Map> consumedRecords = new ArrayList<>();

        // Login to instance.
        final WebSocketHttpHeaders socketHttpHeaders = new WebSocketHttpHeaders(
            login("admin@example.com", "admin")
        );

        // Create websocket client
        final SockJsClient sockJsClient = new SockJsClient(createTransportClient());
        final WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Connect to websocket
        stompClient.connect(WEBSOCKET_URL, socketHttpHeaders, new StompSessionHandlerAdapter() {
            /**
             * After we connect, subscribe to our view.
             */
            @Override
            public void afterConnected(final StompSession session, final StompHeaders connectedHeaders) {
                session.setAutoReceipt(false);
                subscribeToResults(session, view.getId(), 1L, countDownLatch, consumedRecords);
                try {
                    requestNewStream(session, view.getId());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, port);

        // Start the client.
        stompClient.start();

        // Define a max time of 15 seconds
        Duration testTimeout = Duration.ofSeconds(15);

        while (countDownLatch.getCount() > 0) {
            // Sleep for a period and recheck.
            Thread.sleep(1000L);
            testTimeout = testTimeout.minusMillis(1000);

            if (testTimeout.isNegative()) {
                fail("Test timed out!");
            }
        }

        // Success!
        assertEquals("Found all messages!", consumedRecords.size(), kafkaRecords.size());
    }

    private void subscribeToResults(final StompSession stompSession, long viewId, long userId, final CountDownLatch countDownLatch, final List<Map> consumedRecords) {
        final String consumerUrlTemplate = "/user/topic/view/";
        final String topic = consumerUrlTemplate + viewId + "/" + userId;

        stompSession.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(final StompHeaders stompHeaders) {
                logger.info("Get Payload Type Headers: {}", stompHeaders);
                return Map.class;
            }

            @Override
            public void handleFrame(final StompHeaders stompHeaders, final Object obj) {
                logger.info("Headers: {} Object: {}", stompHeaders, obj);
                consumedRecords.add((Map) obj);
                countDownLatch.countDown();
            }
        });
    }

    private void requestNewStream(final StompSession stompSession, long viewId) throws InterruptedException {
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("head");
        consumeRequest.setPartitions("0");

        stompSession.send("/websocket/consume/" + viewId, consumeRequest);
    }

    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport(new RestTemplate()));
        return transports;
    }
}
