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
    private List<String> groupMembers = new ArrayList<String>();
    private boolean run = true;
    private Thread syncThread;

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

    void sleepTenSeconds() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Program() throws Exception {
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

        group = new SpreadGroup();
        group.join(connection, GROUP_NAME);

        connection.add(this); // add myself as listener for spread messages

        // create a background thread for syncOutstandingTransactions
        syncThread = new Thread(() -> {
            while( run ) {
                sleepTenSeconds();

                // send all outstanding transactions
                for (Transaction tx : outstandingTransactions) {
                    sendMessage(tx.toString());
                }
            }
        });
        syncThread.setDaemon(true);
        syncThread.start();
    }



    /*
     * REPL
     */

    void repl() {
        System.out.println("Welcome to the bank, type a command, 'help' for instructions, or 'exit' to quit.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                run = false;
                joinBackgroundThread();
                break;
            }
            processCommand(command);
        }
    }

    void joinBackgroundThread() {
        try {
            syncThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            case "withdraw":
                amount = Double.parseDouble(tokens[1]);
                handleWithdraw(amount);
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
            case "help":
                System.out.println("Available commands:\n" +
                        "getQuickBalance\n" +
                        "getSyncedBalance\n" +
                        "deposit <amount>\n" +
                        "withdraw <amount>\n" +
                        "addInterest <rate>\n" +
                        "getHistory\n" +
                        "checkTxStatus <transactionId>\n" +
                        "cleanHistory\n" +
                        "memberInfo\n" +
                        "sleep <duration>\n" +
                        "help\n" +
                        "exit");
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

        Transaction tx = new Transaction(String.format("deposit %f", amount));
        outstandingTransactions.add(tx);
    }

    void handleWithdraw(double amount) {
        System.out.println("handleWithdraw: " + amount);

        Transaction tx = new Transaction(String.format("withdraw %f", amount));
        outstandingTransactions.add(tx);
    }

    void handleAddInterest(double rate) {
        System.out.println("handleAddInterest: " + rate);

        Transaction tx = new Transaction(String.format("addInterest %f", rate));
        outstandingTransactions.add(tx);
    }

    void handleGetHistory() {
        System.out.println("Transaction History\n-------------------");
        // print the transaction history
        for (Transaction t : txHistory) {
            System.out.println(t.toString());
        }
    }

    void handleCheckTransactionStatus(String transactionId) {
        System.out.println("handleCheckTransactionStatus: " + transactionId);
        // check if transactionId is in outstandingTransactions
        for (Transaction t : outstandingTransactions) {
            if (t.getUniqueId().equals(transactionId)) {
                System.out.println("Transaction is outstanding");
                return;
            }
        }

        System.out.println("Transaction is not in outstanding");

        // should now be in history
        for (Transaction t : txHistory) {
            if (t.getUniqueId().equals(transactionId)) {
                System.out.println("Transaction is in history");
                return;
            }
        }
    }

    void handleCleanHistory() {
        System.out.println("handleCleanHistory");
    }

    void handleMemberInfo() {
        // get group participants from spread and print to console
        String memstr = "Group Members\n-------------\n";
        for (String member : this.groupMembers) {
            memstr += member.toString() + "\n";
        }
        System.out.println(memstr);
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
        
        // remove it from the outstanding transaction list
        for (Transaction t : outstandingTransactions) {
            if (t.getUniqueId().equals(tx.getUniqueId())) {
                outstandingTransactions.remove(t);
                // apply the transaction
                theAccount.deposit(5);
                // add it to the history
                txHistory.add(tx);
                return; // important, not execute rest of function
            }
        }

        // tx was not found, we append it to the list
        outstandingTransactions.add(tx);
    }

    void handleTransaction(Transaction tx) {
        String[] tokens = tx.getCommand().split(" ");
        switch (tokens[0]) {
            case "deposit":
                this.theAccount.deposit(Double.parseDouble(tokens[1]));
                break;
            case "withdraw":
                this.theAccount.withdraw(Double.parseDouble(tokens[1]));
                break;
            case "addInterest":
                this.theAccount.addInterest(Double.parseDouble(tokens[1]));
                break;
            default:
                System.out.println("Invalid transaction, ignoring.");
        }
    }

    public void membershipMessageReceived(SpreadMessage message) {
        System.out.println("Membership message received: " +  getMessageString(message));
        // add or remove from groupMembers list
    }

    String getMessageString(SpreadMessage message) {
        byte[] data = message.getData();
        return new String(data);
    }
}
