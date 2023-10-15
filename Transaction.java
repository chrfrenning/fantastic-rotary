import java.util.UUID;

public class Transaction {
    private String command;
    private String uniqueId;

    public Transaction(String command) {
        this.command = command;
        this.uniqueId = UUID.randomUUID().toString().split("-")[0];
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

    public String toString() {
        return uniqueId + " " + command;
    }

    public static Transaction fromString(String str) {
        String[] parts = str.split(" ", 2);
        Transaction t = new Transaction(parts[1]);
        t.setUniqueId(parts[0]);
        
        return t;
    }
}
