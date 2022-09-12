import java.io.IOException;
import java.net.*;
import java.util.Timer;

public class MulticastListener extends Thread {
    MulticastSocket multicastSocket;
    String messageToReturn = null;

    public MulticastListener(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
        this.start();
    }

    public void run() {
        try {
            byte[] buffer = new byte[1000];
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            multicastSocket.receive(messageIn);
            messageToReturn = new String(messageIn.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMessageToReturn() {
        return messageToReturn;
    }
}
