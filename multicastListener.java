import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Timer;

public class multicastListener extends Thread {
    InetAddress group = null; // Address of multicast group
    MulticastSocket s = null; // Socket for multicast communication
    int port; // Multicast group port
    byte[] buffer = null; // Byte buffer for receiving and sending messages
    Timer timer = new Timer();

    boolean amCoordinator = false;

    DatagramPacket message = null;
    profile p;

    public multicastListener(profile p) {
        try {
            group = InetAddress.getByName(p.getMulticastAddress());
            port = p.getMulticastSocket();
            s = new MulticastSocket(p.getMulticastSocket());
            this.p = p;

            s.joinGroup(group);
            System.out.println("Lets go boys");
//            m = "hello world.\n".getBytes();
//            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, port);
//            s.send(messageOut);

            while (true) {
                this.start(); // Starts listening for message
                Thread.sleep(profile.getT1() * 1000L); // Wait for message

                if (this.isAlive()) {
                    this.interrupt();
                }

                if (!amCoordinator) {
                    if (message.getLength() > 0) {
                        // If message isn't hi, it should be the new bully id
                        if (isNumeric(new String(message.getData()))) {
                            this.p.setBullyId(Integer.parseInt(new String(message.getData())));
                        }
                    } else {
                        // If no message was received, new elections are needed
                        Scanner scanner = new Scanner(System.in); // Create input reader
                        System.out.println("No coordinator, do you want to start a election? (y/n)"); // Read input
                        String option = scanner.nextLine();  // Assess user option

                        if (option.equals("y")) {
                            this.p.startElection();
                        } else if (option.equals("n")) {
                            System.out.println("No election");
                        } else {
                            System.out.println("Invalid input");
                        }
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
            if (amCoordinator) {
                buffer = "hi".getBytes();
                message = new DatagramPacket(buffer, buffer.length, group, port);
                s.send(message);
            } else {
                buffer = new byte[1000]; // Clear buffer
                message = new DatagramPacket(buffer, buffer.length);
                s.receive(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitCoordinator() {
        try {
            this.start();
            Thread.sleep(profile.getT3() * 1000L); // Wait for message
            if (message.getLength() > 0 && isNumeric(new String(message.getData()))) {
                p.setBullyId(Integer.parseInt(new String(message.getData())));
            } else {
                this.p.startElection();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void becomeCoordinator() {
        try {
            amCoordinator = true;
            byte[] b = String.valueOf(this.p.getId()).getBytes(); // sends self id to multicast
            DatagramPacket m = new DatagramPacket(b, b.length, group, port);
            s.send(m);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
