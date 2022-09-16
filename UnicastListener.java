import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;

public class UnicastListener extends Thread {
    DatagramSocket unicastSocket; // Socket for unicast communication
    Queue<DatagramPacket> datagramToReturn = new LinkedList<>(); // Returns a datagram, instead of string, so it can respond to the correct author

    public UnicastListener(DatagramSocket unicastSocket) {
        // Salves unicast socket and starts listening for messages
        this.unicastSocket = unicastSocket;
        this.start();
    }

    public void run() {
        try {
            while (true) {
                // Unicast is always listening and saving what it receives in a queue
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                unicastSocket.receive(messageIn);
                datagramToReturn.add(messageIn);

                // Since the response is a priority message, the listener responds automatically
                if (messageIn != null && new String(messageIn.getData()).startsWith("election")) {
                    // Checks if received unicast message and if it is an election message
                    // If so, answers the process that send it that the message was received
                    System.out.println("Sending election response");
                    byte[] m = "response".getBytes();
                    // Sends response via unicast
                    DatagramPacket request = new DatagramPacket(m, m.length, messageIn.getAddress(), messageIn.getPort());
                    unicastSocket.send(request);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DatagramPacket getDatagramToReturn() {
        // Checks if message queue is empty
        if (!datagramToReturn.isEmpty())
            // If not, removes and returns the first message on top
            return datagramToReturn.remove();
        else
            // If it is, returns null
            return null;
    }
}
