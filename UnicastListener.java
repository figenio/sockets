import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UnicastListener extends Thread {
    DatagramSocket unicastSocket; // Socket for unicast communication
    DatagramPacket datagramToReturn = null;

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
                datagramToReturn = messageIn;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public DatagramPacket getDatagramToReturn() {
        DatagramPacket out = datagramToReturn;
        datagramToReturn = null;
        return out;
    }
}
