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

package org.sourcelab.kafka.webview.ui.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.user.RoleManager;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests explicitly defined queries in this repository only.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserRepositoryTest {

    @Autowired
    private RoleManager roleManager;

    @Autowired
    private UserTestTools userTestTools;

    @Autowired
    private UserRepository userRepository;

    /**
     * Test added to ensure this functions as expected when test suite is run against multiple
     * datasource vendors.
     */
    @Test
    @Transactional
    public void testGetRoleCounts() {
        final Role role1 = roleManager.createNewRole("Role 1");
        final Role role2 = roleManager.createNewRole("Role 2");
        final Role role3 = roleManager.createNewRole("Role 3");

        // Create User
        final User user1 = userTestTools.createUser();
        final User user2 = userTestTools.createUser();
        final User user3 = userTestTools.createUser();
        final User user4 = userTestTools.createUser();
        final User user5 = userTestTools.createUser();

        // Update roles
        user1.setRoleId(role1.getId());
        user2.setRoleId(role1.getId());

        user3.setRoleId(role2.getId());
        user4.setRoleId(role2.getId());
        user5.setRoleId(role2.getId());

        // Retrieve results
        final List<Object[]> results = userRepository.getRoleCounts();

        assertNotNull(results);
        assertFalse(results.isEmpty());

        // Verify, skipping any entries we didn't create in this test case.
        boolean foundRole1 = false;
        boolean foundRole2 = false;


        for (final Object[] result : results) {
            // Index 0 should be the roleId.
            // Index 1 should be the count.
            final Long roleId = (Long) result[0];
            final Long count = (Long) result[1];

            if (roleId == null) {
                continue;
            } else if (roleId.equals(role1.getId())) {
                assertEquals("Role1 has incorrect value", Long.valueOf(2), count);
                foundRole1 = true;
            } else if (roleId.equals(role2.getId())) {
                assertEquals("Role2 has incorrect value", Long.valueOf(3), count);
                foundRole2 = true;
            } else if (roleId.equals(role3.getId())) {
                // Fail.
                assertFalse("Should not have found role3!", true);
            }
        }

        assertTrue("Should have found role 1", foundRole1);
        assertTrue("should have found role 2", foundRole2);
    }
}