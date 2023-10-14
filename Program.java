import java.net.InetAddress;

import spread.*;

public class Program implements AdvancedMessageListener {
    public static void main(String[] args) {
        final String UNIQUE_ID = "V13isLmyl";
        final String IFI_SERVER_IP = "129.240.65.59";
        final int IFI_SERVER_PORT = 4803;
        final String GROUP_NAME = "GROUP7";

        try {

            SpreadConnection connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

            SpreadGroup group = new SpreadGroup();
            group.join(connection, GROUP_NAME);

            AdvancedMessageListener listener = new Program();
            connection.add(listener);

            SpreadMessage message = new SpreadMessage();
            message.addGroup(group);
            message.setFifo();
            message.setObject("MESSAGE1");
            connection.multicast(message);

            Thread.sleep(5000);

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
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
