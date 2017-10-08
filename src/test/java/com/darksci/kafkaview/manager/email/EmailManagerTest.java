package com.darksci.kafkaview.manager.email;

import com.darksci.kafkaview.model.User;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest

//@TestPropertySource({"classpath:config/base.yml", "classpath:config/dev.yml"})
@TestPropertySource(locations = {
    "classpath:config/base.yml",
    "classpath:config/dev.yml"
})
public class EmailManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(EmailManagerTest.class);

    @Autowired
    EmailManager emailManager;

    //@Test
    public void test() {

        // Create test user
        final User myUser = new User();
        myUser.setEmail("stephen.powis@gmail.com");
        myUser.setDisplayName("Test User");

        emailManager.sendWelcomeEmail(myUser);
    }

}