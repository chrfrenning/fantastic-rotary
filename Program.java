import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

import spread.*;

public class Program implements AdvancedMessageListener {
    static final int SECONDS_BETWEEN_SYNC = 2;

    static String serverIp = "127.0.0.1";
    static int serverPort = 8764;
    static String accountName = "GROUP7-V13isLmyx";
    static int minimumReplicas = 3;
    static String replicaName = UUID.randomUUID().toString().split("-")[0];

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
            parseCommandLineArguments(args);
            printSettings();
            Program thisInstance = new Program();

            // interactive or batch?
            Optional<String> commandFileName = parseArguments(args, "file", "f");
            if ( commandFileName.isPresent() ) {
                thisInstance.process(commandFileName.get());
            } else {
                thisInstance.repl();
            }

            // we're done, clean up
            thisInstance.connection.remove(thisInstance);
            thisInstance.connection.disconnect();

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n--- Stack Trace:\n");
            e.printStackTrace();
        }

    }

    private static void parseCommandLineArguments(String[] args) {
        Optional<String> serverIpPrm = parseArguments(args, "server", "s");
        serverIpPrm.ifPresent(s -> serverIp = s);

        Optional<String> portPrm = parseArguments(args, "port", "p");
        portPrm.ifPresent(s -> serverPort = Integer.parseInt(s));

        Optional<String> accountNamePrm = parseArguments(args, "account", "a");
        accountNamePrm.ifPresent(s -> accountName = s);

        Optional<String> minimumReplicasPrm = parseArguments(args, "replicas", "m");
        minimumReplicasPrm.ifPresent(s -> minimumReplicas = Integer.parseInt(s));

        Optional<String> instanceIdPrm = parseArguments(args, "name", "n");
        instanceIdPrm.ifPresent(s -> replicaName = s);
    }

    private static void printSettings() {
        System.out.println("Settings:\n" +
                "Server: " + serverIp + ":" + serverPort + "\n" +
                "Account: " + accountName + "\n" +
                "Instance: " + replicaName + "\n" +
                "Replicas: " + minimumReplicas + "\n");
    }

    private static Optional<String> parseArguments(String[] args, String command, String shortcut) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--" + command) || args[i].equals("-" + shortcut)) {
                if (i + 1 < args.length) {
                    return Optional.of(args[i + 1]);
                } else {
                    // Command exists but no argument follows.
                    return Optional.empty();
                }
            }
        }
        // Command does not exist in the arguments.
        return Optional.empty();
    }

    void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Program() throws Exception {

        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName(serverIp), serverPort, replicaName, true, true);

        group = new SpreadGroup();
        group.join(connection, accountName);

        connection.add(this); // add myself as listener for spread messages

        // create a background thread for syncOutstandingTransactions
        syncThread = new Thread(() -> {
            while( run ) {
                sleep(SECONDS_BETWEEN_SYNC);

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
     * Batch processing
     */

    void process(String commandFileName) throws Exception {
        System.out.println("Processing commands from file: " + commandFileName);

        // Waiting for all replicas to join
        while (groupMembers.size() < minimumReplicas) {
            sleep(5);
        }

        // We're ready to roll
        System.out.println("We have enough bank offices open, lets crunch those numbers!");

        // Process all transactions from the input file
        File file = new File(commandFileName);
        Scanner scanner = new Scanner(file);
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                System.out.println("Exiting...");
                run = false;
                joinBackgroundThread();
                break;
            }
            processCommand(command);
        }
        scanner.close();
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
            case "qbal":
            case "getQuickBalance":
                handleGetQuickBalance();
                break;
            case "sbal":
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
            case "stop":
                Transaction tx = new Transaction("stop");
                sendMessage(tx.toString());
            default:
                System.out.println("Invalid command.");
        }
    }

    boolean isOpen() {
        return groupMembers.size() >= minimumReplicas;
    }

    void handleGetQuickBalance() {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }

        // print the balance as we know it in this replica
        System.out.println("getQuickBalance: " + theAccount.getBalance());
    }

    void handleGetSyncedBalance() {
        handleGetSyncedBalanceOrdered();
    }

    void handleGetSyncedBalanceNaive() {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }

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

    void handleGetSyncedBalanceOrdered() {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }

        // order this transaction via spread
        System.out.println("Pending getSyncedBalance");

        Transaction tx = new Transaction(String.format("getBalance %s", this.replicaName));
        outstandingTransactions.add(tx);
    }

    void handleDeposit(double amount) {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }
        
        // order this transaction via spread
        System.out.println("Pending deposit: " + amount);

        Transaction tx = new Transaction(String.format("deposit %f", amount));
        outstandingTransactions.add(tx);
    }

    void handleWithdraw(double amount) {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }
        
        // order this transaction via spread
        System.out.println("Pending withdrawal: " + amount);

        Transaction tx = new Transaction(String.format("withdraw %f", amount));
        outstandingTransactions.add(tx);
    }

    void handleAddInterest(double rate) {
        // check that we're open
        if ( !isOpen() ) {
            System.out.println("Not enough bank offices open, try again later.");
            return;
        }

        // order this transaction via spread
        System.out.println("Pending add interest: " + rate);

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
        // TODO
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
        sleep(duration);
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
        System.err.println("Regular message received: " + getMessageString(message));

        Transaction tx = Transaction.fromString(getMessageString(message));
        if (tx.getCommand().equals("stop")) {
            System.exit(2);
        }

        // handle the transaction
        boolean registerTransactionInHistory = handleTransaction( tx );
        
        // remove it from the outstanding transaction list
        for (Transaction t : outstandingTransactions) {
            if (t.getUniqueId().equals(tx.getUniqueId())) {
                outstandingTransactions.remove(t);
                return; // important, not execute rest of function
            }
        }

        // add it to the history if not a getBalance transaction
        if ( registerTransactionInHistory ) {
            txHistory.add(tx);
        }
    }

    boolean handleTransaction(Transaction tx) { // returns true if transaction should be added to history
        String[] tokens = tx.getCommand().split(" ");
        switch (tokens[0]) {

            // Modifying transactions, keep these in history (returns true)

            case "deposit":
                this.theAccount.deposit(Double.parseDouble(tokens[1]));
                return( true );

            case "withdraw":
                this.theAccount.withdraw(Double.parseDouble(tokens[1]));
                return( true );

            case "addInterest":
                this.theAccount.addInterest(Double.parseDouble(tokens[1]));
                return( true );


            // Non-modifying or syncup transactions, don't keep these in history (returns false)

            case "getBalance":
                // only execute if from myself
                if (tokens[1].startsWith(this.replicaName)) {
                    System.out.println("Synced Balance: " + this.theAccount.getBalance());
                }
                break;

            case "ping":
                // ignore if from myself
                if (!tokens[1].startsWith(this.replicaName)) {
                    // register new member in the cluster
                    System.out.println("Pong to " + tokens[1]);
                    handleNewReplicaMember(tokens[1], true);
                }
                break;

            case "syncBalance":
                // only execute if from myself
                if (tokens[1].startsWith(this.replicaName)) {
                    // send my balance to the new member
                    Transaction balanceTx = new Transaction(String.format("balanceUpdate %s %f", this.replicaName, this.theAccount.getBalance()));
                    sendMessage(balanceTx.toString());
                }
                break;

            case "balanceUpdate":
                // only execute if not from myself
                if (!tokens[1].startsWith(this.replicaName)) {
                    // update my balance
                    this.theAccount.resetBalance(Double.parseDouble(tokens[2]));
                }
                break;

            default:
                System.out.println("Invalid transaction, ignoring.");
        }

        return( false );
    }

    public void membershipMessageReceived(SpreadMessage message) {
        System.err.println("Membership message received: " +  getMessageString(message));
        
        // get the id of the replica, split token on # and take two last
        String[] tokens = getMessageString(message).split("#");
        String replicaName = String.format("%s-%s", tokens[tokens.length - 2], tokens[tokens.length - 1]);

        // handle the new member
        handleNewReplicaMember(replicaName, false);
    }

    void handleNewReplicaMember(String replicaName, boolean fromPing) {
        // check if this exist in the members list, if so it is a deregistration
        // unless, add it to the list
        if (groupMembers.contains(replicaName) && !fromPing) {
            groupMembers.remove(replicaName);

            System.err.println("Member left: " + replicaName);

            // If we have less than MINIMUM_REPLICAS online, we're not ready for business
            if (groupMembers.size() == minimumReplicas - 1) { // -1 to only show message once
                System.out.println("The bank is now closed!");
            }
        }
        else {
            groupMembers.add(replicaName);
            System.err.println("New member: " + replicaName);

            // make sure the new member gets to know the balance
            if ( !fromPing ) {
                if ( !replicaName.startsWith(this.replicaName) ) {
                    initiateSyncNewMember(); 
                }
            }

            // If we have MINIMUM_REPLICAS online, we're ready for business
            if (groupMembers.size() == minimumReplicas) {
                System.out.println("We have enough bank offices open, we're ready for business!");
            }
        }
    }

    void initiateSyncNewMember() {
        Transaction ping = new Transaction(String.format("ping %s", this.replicaName));
        outstandingTransactions.add(ping);

        Transaction sendBalanceTx = new Transaction(String.format("syncBalance %s", this.replicaName));
        outstandingTransactions.add(sendBalanceTx);
    }

    String getMessageString(SpreadMessage message) {
        byte[] data = message.getData();
        return new String(data);
    }
}
