import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class processo {
        public static void main(String args[]){
            MulticastSocket s = null;
            try {
                InetAddress group = InetAddress.getByName("228.5.6.7");
                s = new MulticastSocket(6789);
                s.joinGroup(group);
                System.out.println("Lets go boys");
                byte [] m = "hello world.\n".getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
                s.send(messageOut);
                byte[] buffer = new byte[1000];
                for(int i=0; i< 3;i++) {		// get messages from others in group
                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    s.receive(messageIn);
                    System.out.println("Received:" + new String(messageIn.getData()));
                }
                System.out.println("I'm done with this");
                s.leaveGroup(group);
            }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
            }catch (IOException e){System.out.println("IO: " + e.getMessage());
            }finally {if(s != null) s.close();}


    }
}
