import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import spread.*;

public class Program implements AdvancedMessageListener {
    static final String UNIQUE_ID = "V13isLmyl";
    static final String IFI_SERVER_IP = "127.0.0.1";
    static final int IFI_SERVER_PORT = 8764;
    static final String GROUP_NAME = "GROUP7";

    private SpreadConnection connection;
    private SpreadGroup group;
    private Account theAccount = new Account();
    private List<Transaction> outstandingTransactions = new ArrayList<Transaction>();
    private List<Transaction> txHistory = new ArrayList<Transaction>();

    public Program() throws Exception {
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

        group = new SpreadGroup();
        group.join(connection, GROUP_NAME);

        connection.add(this); // add myself as listener for spread messages
    }

    public static void main(String[] args) {
        try {
            Program thisInstance = new Program();

            // interactive mode now, must be changed to reading command from file if <filename> is provided in args
            thisInstance.repl();

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }

    void sleepSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /*
     * REPL
     */

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
        //theAccount.getBalance();
        System.out.println("getQuickBalance: " + theAccount.getBalance());
    }

    void handleGetSyncedBalance() {
        // ensures we have no pendign tx's before returning balance
        try {
            final int SLEEP_TIME = 1000;
            while ( outstandingTransactions.size() > 0 )
                Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
        }
        finally {
            System.out.println("getSyncedBalance: " + theAccount.getBalance());
        }
    }

    void handleDeposit(double amount) {
        System.out.println("Handling deposit: " + amount);

        // create new transaction, post the tx to all replicas...
        Transaction tx = new Transaction(String.format("deposit %f", amount));
        outstandingTransactions.add(tx);
        sendMessage(tx.toString());
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

    void sendMessage(String string) {
        try {
            SpreadMessage message = new SpreadMessage();
            message.addGroup(group);
            message.setFifo();

            byte[] bytes = string.getBytes("US-ASCII");
            message.setData(bytes);

            this.connection.multicast(message);
        }
        catch (Exception e) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }

    /*
     * Message handling
     */

    public void regularMessageReceived(SpreadMessage message) {
        System.out.println("Regular message received: " + getMessageString(message));
        Transaction tx = Transaction.fromString(getMessageString(message));
        txHistory.add(tx);
        // apply the transaction
        theAccount.deposit(5);
        // remove it from the outstanding transaction list
        for (Transaction t : outstandingTransactions) {
            if (t.getUniqueId().equals(tx.getUniqueId())) {
                outstandingTransactions.remove(t);
                break;
            }
        }
    }

    public void membershipMessageReceived(SpreadMessage message) {
        System.out.println("Membership message received: " +  getMessageString(message));
    }

    String getMessageString(SpreadMessage message) {
        byte[] data = message.getData();
        return new String(data);
    }
}
