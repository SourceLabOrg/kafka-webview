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
    private final String consumerId;
    private final boolean isSimple;
    private final String partitionAssignor;
    private final String state;
    private final List<Member> members;
    private final NodeDetails coordinator;

    /**
     * Constructor.
     * @param consumerId consumer group id.
     * @param isSimple if its a simple consumer group
     * @param partitionAssignor How partitions are assigned
     * @param state state of consumer
     * @param members members in the group.
     * @param coordinator
     */
    public ConsumerGroupDetails(
        final String consumerId,
        final boolean isSimple,
        final String partitionAssignor,
        final String state,
        final List<Member> members,
        final NodeDetails coordinator) {
        this.consumerId = consumerId;
        this.isSimple = isSimple;
        this.partitionAssignor = partitionAssignor;
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.state = state;
        this.coordinator = coordinator;
    }

    public String getConsumerId() {
        return consumerId;
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
            + "consumerId='" + consumerId + '\''
            + ", isSimple=" + isSimple
            + ", partitionAssignor='" + partitionAssignor + '\''
            + ", state='" + state + '\''
            + ", members=" + members
            + ", coordinator=" + coordinator
            + '}';
    }
}
