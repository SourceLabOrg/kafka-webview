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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Details about a specific consumer group.
 */
public class ConsumerGroupDetails {
    private final String groupId;
    private final boolean isSimple;
    private final String partitionAssignor;
    private final String state;
    private final List<Member> members;
    private final NodeDetails coordinator;

    /**
     * Constructor.
     * @param groupId consumer group id.
     * @param isSimple if its a simple consumer group
     * @param partitionAssignor How partitions are assigned
     * @param state state of consumer
     * @param members members in the group.
     * @param coordinator node that is acting as the coordinator for this group.
     */
    public ConsumerGroupDetails(
        final String groupId,
        final boolean isSimple,
        final String partitionAssignor,
        final String state,
        final List<Member> members,
        final NodeDetails coordinator) {
        this.groupId = groupId;
        this.isSimple = isSimple;
        this.partitionAssignor = partitionAssignor;
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.state = state;
        this.coordinator = coordinator;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isSimple() {
        return isSimple;
    }

    public String getPartitionAssignor() {
        return partitionAssignor;
    }

    public String getState() {
        return state;
    }

    public List<Member> getMembers() {
        return members;
    }

    public NodeDetails getCoordinator() {
        return coordinator;
    }

    /**
     * Represents a consumer group members details.
     */
    public static class Member {
        private final String memberId;
        private final String clientId;
        private final String host;
        private final Set<TopicPartition> assignment;

        /**
         * Constructor.
         * @param memberId Consumer group id.
         * @param clientId Id of the individual member of the group.
         * @param host host of consumer.
         * @param assignment what partitions the consumer is assigned.
         */
        public Member(final String memberId, final String clientId, final String host, final Set<TopicPartition> assignment) {
            this.memberId = memberId;
            this.clientId = clientId;
            this.host = host;
            this.assignment = Collections.unmodifiableSet(new HashSet<>(assignment));
        }

        public String getMemberId() {
            return memberId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getHost() {
            return host;
        }

        public Set<TopicPartition> getAssignment() {
            return assignment;
        }

        @Override
        public String toString() {
            return "Member{"
                + "memberId='" + memberId + '\''
                + ", clientId='" + clientId + '\''
                + ", host='" + host + '\''
                + ", assignment=" + assignment
                + '}';
        }
    }

    @Override
    public String toString() {
        return "ConsumerGroupDetails{"
            + "groupId='" + groupId + '\''
            + ", isSimple=" + isSimple
            + ", partitionAssignor='" + partitionAssignor + '\''
            + ", state='" + state + '\''
            + ", members=" + members
            + ", coordinator=" + coordinator
            + '}';
    }
}
