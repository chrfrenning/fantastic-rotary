import java.net.InetAddress;

import spread.*;

public class Program {
    public static void main(String[] args) {
        final String UNIQUE_ID = "V13isLmyl";
        final String IFI_SERVER_IP = "129.240.65.60";
        final int IFI_SERVER_PORT = 4803;

        try {

            SpreadConnection connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(IFI_SERVER_IP), IFI_SERVER_PORT, UNIQUE_ID, true, true);

        } catch ( Exception e ) {
            System.out.println("Catastrophic failure: " + e.getMessage() + "\n");
        }
    }
}