import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class unicastListener extends Thread{
    DatagramSocket unicastSocket = null; // Socket for unicast communication
    InetAddress targetAddress = null; // Target inet address to communicate
    int targetPort; // Target port to communicate
    byte [] m = null; // Byte buffer for receiving and sending messages

    profile p = null;

    public unicastListener(profile p) {
        this.p = p;
        try {
            unicastSocket = new DatagramSocket(p.getUnicastSocket());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void startElection() {

        while (true) {

            this.start();
        }
    }

    public void run() {

    }


//        DatagramSocket aSocket = null;
//        try {
//            aSocket = new DatagramSocket();
//            byte [] m = args[0].getBytes();
//            InetAddress aHost = InetAddress.getByName(args[1]);
//            int serverPort = 6789;
//            DatagramPacket request =
//                    new DatagramPacket(m,  args[0].length(), aHost, serverPort);
//            aSocket.send(request);
//            byte[] buffer = new byte[1000];
//            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
//            aSocket.receive(reply);
//            System.out.println("Reply: " + new String(reply.getData()));
//        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
//        }catch (IOException e){System.out.println("IO: " + e.getMessage());
//        }finally {if(aSocket != null) aSocket.close();}

}
