import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;

public class UnicastListener extends Thread {
    DatagramSocket unicastSocket; // Socket for unicast communication
    Queue<DatagramPacket> datagramToReturn = new LinkedList<>();

    public UnicastListener(DatagramSocket unicastSocket) {
        this.unicastSocket = unicastSocket;
        this.start();
    }

    public void run() {
        try {
            while (true) {
                // Unicast is always listening and saving what it receives
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                unicastSocket.receive(messageIn);
                datagramToReturn.add(messageIn);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public DatagramPacket getDatagramToReturn() {
        if (!datagramToReturn.isEmpty())
            return datagramToReturn.remove();
        else
            return null;
    }
}
