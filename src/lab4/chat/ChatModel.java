package lab4.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by domien on 25/03/2016.
 */
public class ChatModel extends Event {
    private String starter; // degene die de chatsessie startte.
    private List<ChatMessage> messages = new ArrayList<>();

    public String getStarter() {
        return starter;
    }

    public void setStarter(String starter) {a
        this.starter = starter;
    }

    public void addMessage(ChatMessage msg) {
        messages
    }
}