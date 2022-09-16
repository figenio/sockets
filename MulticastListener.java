import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;

public class MulticastListener extends Thread {
    MulticastSocket multicastSocket;
    Queue<String> messageToReturn = new LinkedList<>();

    public MulticastListener(MulticastSocket multicastSocket) {
        // Salves multicast socket and starts listening for messages
        this.multicastSocket = multicastSocket;
        this.start();
    }

    public void run() {
        try {
            while (true) {
                // Multicast is always listening and saving what it receives in a queue
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(messageIn);
                messageToReturn.add(new String(messageIn.getData()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMessageToReturn() {
        // Checks if message queue is empty
        if (!messageToReturn.isEmpty())
            // If not, removes and returns the first message on top
            return messageToReturn.remove();
        else
            // If it is, returns null
            return null;
    }
}
