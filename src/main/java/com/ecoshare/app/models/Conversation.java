package com.ecoshare.app.models;

import com.google.firebase.firestore.Exclude;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation {
    private String conversationId;
    private String lastMessage;
    private long lastMessageTimestamp;
    private List<String> participantIds;
    private Map<String, String> participantNames;
    private Map<String, String> participantProfiles;
    private Map<String, Integer> unreadCounts;

    public Conversation() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public Map<String, String> getParticipantNames() {
        return participantNames;
    }

    public void setParticipantNames(Map<String, String> participantNames) {
        this.participantNames = participantNames;
    }

    public Map<String, String> getParticipantProfiles() {
        return participantProfiles;
    }

    public void setParticipantProfiles(Map<String, String> participantProfiles) {
        this.participantProfiles = participantProfiles;
    }

    public Map<String, Integer> getUnreadCounts() {
        return unreadCounts;
    }

    public void setUnreadCounts(Map<String, Integer> unreadCounts) {
        this.unreadCounts = unreadCounts;
    }

    @Exclude
    public String getOtherParticipantId(String currentUserId) {
        if (participantIds == null) return null;
        for (String id : participantIds) {
            if (!id.equals(currentUserId)) {
                return id;
            }
        }
        return null;
    }

    @Exclude
    public String getOtherParticipantName(String currentUserId) {
        String otherId = getOtherParticipantId(currentUserId);
        return participantNames != null ? participantNames.get(otherId) : "User";
    }

    @Exclude
    public String getOtherParticipantProfile(String currentUserId) {
        String otherId = getOtherParticipantId(currentUserId);
        return participantProfiles != null ? participantProfiles.get(otherId) : null;
    }
}
