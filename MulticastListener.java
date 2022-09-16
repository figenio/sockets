import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;

public class MulticastListener extends Thread {
    MulticastSocket multicastSocket;
    Queue<String> messageToReturn = new LinkedList<>();

    public MulticastListener(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
        this.start();
    }

    public void run() {
        try {
            while (true) {
                // Unicast is always listening and saving what it receives
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
        if (!messageToReturn.isEmpty())
            return messageToReturn.remove();
        else
            return null;
    }
}
