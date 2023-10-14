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

    SpreadConnection connection;
    SpreadGroup group;
    private Account theAccount = new Account();
    private List<Transaction> allTransactions = new ArrayList<Transaction>();
    private List<Transaction> outstandingTransactions = new ArrayList<Transaction>();

    public Program() throws Exception {
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

        SpreadGroup group = new SpreadGroup();
        group.join(connection, GROUP_NAME);
    }

    public static void main(String[] args) {
        try {
            Program thisInstance = new Program();

            // SpreadMessage message = new SpreadMessage();
            // message.addGroup(group);
            // message.setFifo();
            // message.setObject("MESSAGE1");
            // connection.multicast(message);

            // interactive mode now, must be changed to reading command from file if <filename> is provided in args
            thisInstance.repl();

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }

    void sendDummyMessage() {
        try {
            SpreadMessage message = new SpreadMessage();
            message.addGroup(group);
            message.setFifo();
            message.setObject("MESSAGE1");
            this.connection.multicast(message);
        }
        catch (Exception e) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }

    void repl() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            }
            processCommand(command);
        }
    }

    void processCommand(String command) {
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
                handleCheckTransactionStatus(transactionId);
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

    void handleGetQuickBalance() {
        System.out.println("getQuickBalance");
    }

    void handleGetSyncedBalance() {
        System.out.println("getSyncedBalance");
    }

    void handleDeposit(double amount) {
        System.out.println("handleDeposit: " + amount);
    }

    void handleAddInterest(double rate) {
        System.out.println("handleAddInterest: " + rate);
    }

    void handleGetHistory() {
        System.out.println("handleGetHistory");
    }

    void handleCheckTransactionStatus(String transactionId) {
        System.out.println("handleCheckTransactionStatus: " + transactionId);
    }

    void handleCleanHistory() {
        System.out.println("handleCleanHistory");
    }

    void handleMemberInfo() {
        System.out.println("handleMemberInfo");
    }

    void handleSleep(int duration) {
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
