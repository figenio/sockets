import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class multicastListener extends Thread{
    MulticastSocket s = null;
    InetAddress group = null;

    public multicastListener (String groupAddress, int multicastSocket) {

        try {
            group = InetAddress.getByName(groupAddress);
            s = new MulticastSocket(multicastSocket);
            s.joinGroup(group);
            System.out.println("Lets go boys");
            byte [] m = "hello world.\n".getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, multicastSocket);
            s.send(messageOut);
            this.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run () {
        try {
            byte[] buffer = new byte[1000];
            for(int i=0; i< 3;i++) {		// get messages from others in group
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    s.receive(messageIn);
                System.out.println("Received:" + new String(messageIn.getData()));
            }
            System.out.println("I'm done with this");
                s.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
