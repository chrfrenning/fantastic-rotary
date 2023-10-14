import java.util.UUID;

public class Transaction {
    private String command;
    private String uniqueId;

    public Transaction(String command) {
        this.command = command;
        this.uniqueId = UUID.randomUUID().toString();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
