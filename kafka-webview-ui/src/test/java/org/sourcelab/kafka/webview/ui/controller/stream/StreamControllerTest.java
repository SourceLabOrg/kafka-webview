package org.sourcelab.kafka.webview.ui.controller.stream;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test coverage for stream controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StreamControllerTest {
    private final static Logger logger = LoggerFactory.getLogger(StreamControllerTest.class);

    @Value("${local.server.port}")
    private int port;
    private String WEBSOCKET_URL;
    private WebSocketHttpHeaders headers;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private ClusterTestTools clusterTestTools;

    private Cluster cluster;
    private View view;

    /**
     * Setup broker without SSL or SASL support.
     */
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Before
    public void setup() {
        WEBSOCKET_URL = "ws://localhost:" + port + "/websocket";
        headers = new WebSocketHttpHeaders();

        cluster = clusterTestTools.createCluster(
            "TestCluster" + System.currentTimeMillis(),
            sharedKafkaTestResource.getKafkaConnectString()
        );

        final String topicName = "TestTopic" + System.currentTimeMillis();

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);

        view = viewTestTools.createView("MyTestView" + System.currentTimeMillis());
        view.setCluster(cluster);
        view.setTopic(topicName);
        view.setPartitions("0");
        viewTestTools.save(view);
    }

    /**
     * Attempts to make a websocket connection as an anonymous user to verify it works as expected.
     */
    @Test
    public void test_webSocketConnection() throws InterruptedException, ExecutionException, TimeoutException {
        login("admin@example.com", "admin", headers);

        final SockJsClient sockJsClient = new SockJsClient(createTransportClient());
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.connect(WEBSOCKET_URL, this.headers, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(final StompHeaders headers) {
                return super.getPayloadType(headers);
            }

            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.setAutoReceipt(false);
                subscribeToResults(session, view.getId(), 1L, countDownLatch);
                try {
                    requestNewStream(session, view.getId());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, port);
        stompClient.start();
        countDownLatch.await();

        Thread.sleep(5000L);
    }

    private void login(final String user, final String password, final HttpHeaders headersToUpdate) {

        final String url = "http://localhost:" + port + "/login";

        restTemplate.execute(url, HttpMethod.POST,

            request -> {
                request.getHeaders().addAll(getLoginHeaders());
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("email", user);
                map.add("password", password);
                new FormHttpMessageConverter().write(map, MediaType.APPLICATION_FORM_URLENCODED, request);
            },

            response -> {
                headersToUpdate.add("Cookie", response.getHeaders().getFirst("Set-Cookie"));
                return null;
            });
    }

    private HttpHeaders getLoginHeaders() {
        final String url = "http://localhost:" + port + "/login";
        final HttpHeaders headers = new HttpHeaders();
        final ResponseEntity<String> page = restTemplate.getForEntity(url, String.class);

        // Should be 200 OK
        assertEquals(HttpStatus.OK, page.getStatusCode());

        final String cookie = page.getHeaders().getFirst("Set-Cookie");
        headers.set("Cookie", cookie);
        final Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
        final Matcher matcher = pattern.matcher(page.getBody());
        assertTrue(matcher.matches());
        headers.set("X-CSRF-TOKEN", matcher.group(1));
        return headers;
    }

    private void subscribeToResults(final StompSession stompSession, long viewId, long userId, final CountDownLatch countDownLatch) {
        final String consumerUrlTemplate = "/user/topic/view/";

        final String topic = consumerUrlTemplate + viewId + "/" + userId;

        stompSession.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(final StompHeaders stompHeaders) {
                logger.info("Get Payload Type Headers: {}", stompHeaders);
                return String.class;
            }

            @Override
            public void handleFrame(final StompHeaders stompHeaders, final Object obj) {
                logger.info("Headers: {} Object: {}", stompHeaders, obj);
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