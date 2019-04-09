package org.sourcelab.kafka.webview.ui.manager.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for RoleManager functionality.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserManagerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTestTools userTestTools;

    @Autowired
    private UserManager userManager;

    /**
     * Test deleting a user.
     */
    @Test
    @Transactional
    public void testDeleteRole_inUseByDeletedUser() {
        // Create a user
        final User user = userTestTools.createUser();

        // Now delete our user
        final boolean userDeleteResult = userManager.deleteUser(user);
        assertTrue("Should have a result of true", userDeleteResult);
        assertFalse("Should not still exist", userRepository.findById(user.getId()).isPresent());
    }

}