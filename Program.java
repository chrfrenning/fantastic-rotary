import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import spread.*;

public class Program implements AdvancedMessageListener {
    private static final String UNIQUE_ID = "V13isLmyl";
    private static final String IFI_SERVER_IP = "127.0.0.1";
    private static final int IFI_SERVER_PORT = 8764;
    private static final String GROUP_NAME = "GROUP7";

    private static Account theAccount = new Account();
    private static List<Transaction> allTransactions = new ArrayList<Transaction>();
    private static List<Transaction> outstandingTransactions = new ArrayList<Transaction>();

    public static void main(String[] args) {
        try {
            SpreadConnection connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

            SpreadGroup group = new SpreadGroup();
            group.join(connection, GROUP_NAME);

            Program thisInstance = new Program();
            connection.add(thisInstance);

            SpreadMessage message = new SpreadMessage();
            message.addGroup(group);
            message.setFifo();
            message.setObject("MESSAGE1");
            connection.multicast(message);

            // interactive mode now, must be changed to reading command from file if <filename> is provided in args
            repl();

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }

    static void repl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            }
            processCommand(command);
        }
    }

    static void processCommand(String command) {
        String[] tokens = command.split(" ");
        switch (tokens[0]) {
            case "getQuickBalance":
                handleGetQuickBalance();
                break;
            case "getSyncedBalance":
                handleGetSyncedBalance();
                break;
            case "deposit":
                double amount = Double.parseDouble(tokens[1]);
                handleDeposit(amount);
                break;
            case "addInterest":
                double rate = Double.parseDouble(tokens[1]);
                handleAddInterest(rate);
                break;
            case "getHistory":
                handleGetHistory();
                break;
            case "checkTxStatus":
                String transactionId = tokens[1];
                handleCheckTxStatus(transactionId);
                break;
            case "cleanHistory":
                handleCleanHistory();
                break;
            case "memberInfo":
                handleMemberInfo();
                break;
            case "sleep":
                int duration = Integer.parseInt(tokens[1]);
                handleSleep(duration);
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    static void handleGetQuickBalance() {
        System.out.println("getQuickBalance");
    }

    static void handleGetSyncedBalance() {
        System.out.println("getSyncedBalance");
    }

    static void handleDeposit(double amount) {
        System.out.println("handleDeposit: " + amount);
    }

    static void handleAddInterest(double rate) {
        System.out.println("handleAddInterest: " + rate);
    }

    static void handleGetHistory() {
        System.out.println("handleGetHistory");
    }

    static void handleCheckTxStatus(String transactionId) {
        System.out.println("handleCheckTxStatus: " + transactionId);
    }

    static void handleCleanHistory() {
        System.out.println("handleCleanHistory");
    }

    static void handleMemberInfo() {
        System.out.println("handleMemberInfo");
    }

    static void handleSleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void regularMessageReceived(SpreadMessage message) {
        System.out.println("Regular message received: " + getMessageString(message));
    }

    public void membershipMessageReceived(SpreadMessage message) {
        System.out.println("Membership message received: " +  getMessageString(message));
    }

    String getMessageString(SpreadMessage message) {
        byte[] data = message.getData();
        return new String(data);
    }
}
