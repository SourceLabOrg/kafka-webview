package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.List;

/**
 * Details about a specific consumer group.
 */
public class ConsumerGroupDetails {
    final String consumerId;
    final boolean isSimple;
    final String partitionAssignor;
    final List<State> states;
    final List<Member> members;

    public ConsumerGroupDetails(
        final String consumerId,
        final boolean isSimple,
        final String partitionAssignor,
        final List<State> states,
        final List<Member> members
    ) {
        this.consumerId = consumerId;
        this.isSimple = isSimple;
        this.partitionAssignor = partitionAssignor;
        this.members = members;
        this.states = states;
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

    public List<State> getStates() {
        return states;
    }

    public List<Member> getMembers() {
        return members;
    }

    public static class State {

    }

    public static class Member {

    }

    @Override
    public String toString() {
        return "ConsumerGroupDetails{"
            + "consumerId='" + consumerId + '\''
            + ", isSimple=" + isSimple
            + ", partitionAssignor='" + partitionAssignor + '\''
            + ", states=" + states
            + ", members=" + members
            + '}';
    }
}
