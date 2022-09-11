import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Timer;

public class multicastListener extends Thread {
    InetAddress group = null; // Address of multicast group
    MulticastSocket s = null; // Socket for multicast communication
    int port; // Multicast group port
    byte[] m = null; // Byte buffer for receiving and sending messages
    Timer timer = new Timer();

    DatagramPacket messageIn = null;

    public multicastListener(profile p, unicastListener u) {
        try {
            group = InetAddress.getByName(p.getMulticastAddress());
            port = p.getMulticastSocket();
            s = new MulticastSocket(p.getMulticastSocket());


            s.joinGroup(group);
            System.out.println("Lets go boys");
            m = "hello world.\n".getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, port);
            s.send(messageOut);

            while (true) {
                m = new byte[1000]; // Clear buffer
                this.start(); // Starts listening for message
                this.timer.wait(profile.getT1() * 1000L); // Wait for message

                if (messageIn.getLength() > 0) {
                    // If message isn't hi, its new bully id
                    if (!new String(m).equals("olá")) {
                        p.setBullyId(Integer.parseInt(new String(m)));
                    }
                } else {
                    Scanner scanner = new Scanner(System.in); // Create input reader
                    System.out.println("No coordinator, do you want to start a election? (y/n)"); // Read input
                    String option = scanner.nextLine();  // Assign profile

                    if (option.equals("y")) {
                        u.startElection();
                    } else if (option.equals("n")) {
                        System.out.println("No election");
                    } else {
                        System.out.println("Invalid input");
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            messageIn = new DatagramPacket(m, m.length);
            s.receive(messageIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try {
//            byte[] buffer = new byte[1000];
//            for(int i=0; i< 3;i++) {		// get messages from others in group
//                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
//                    s.receive(messageIn);
//                System.out.println("Received:" + new String(messageIn.getData()));
//            }
//            System.out.println("I'm done with this");
//                s.leaveGroup(group);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }finally {if(s != null) s.close();}
    }
}
