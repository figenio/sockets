import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class unicastListener extends Thread {
    DatagramSocket unicastSocket = null; // Socket for unicast communication
    InetAddress targetAddress = null; // Target inet address to communicate
    int targetPort; // Target port to communicate
    byte[] buffer = null; // Byte buffer for receiving and sending messages

    profile p = null;
    JSONArray profiles;
    DatagramPacket message = null;
    boolean responseReceived = false;
//    int[] profiles[] = [1,2,3,4];

    public unicastListener(profile p) {
        try {
            // Read JSON with profile options
            JSONParser parser = new JSONParser();
            JSONArray profiles = null;
            this.profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));
            unicastSocket = new DatagramSocket(p.getUnicastSocket());
            this.start();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void startElection() throws InterruptedException {
        // Validate if it has greater id
        if (p.hasGreaterId()) {
            p.becomeCoordinator();
        } else {
            // Sends election message to all bigger than it
            for (int i = 4; i > p.getId(); i--) {
                this.sendElectionMessage(i);
            }
            this.start();
            // Waits for response
            Thread.sleep(profile.getT2() * 1000L);
            if (responseReceived) {
                p.waitCoordinator(); // if received response, waits for coordinator
            } else {
                p.becomeCoordinator(); // if not, becomes coordinator
            }
            responseReceived = false; // resets the received flag
        }
    }

    public void run() {
        try {
            buffer = new byte[1000];
            message = new DatagramPacket(buffer, buffer.length);
            unicastSocket.receive(message);

            if (new String(message.getData()).equals("election")) {
                this.startElection();
            } else if (new String(message.getData()).equals("response")) {
                responseReceived = true;
            }
            this.start(); // after receiving and interpreting message, listens again
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendElectionMessage(int processId) {
        try {
            byte[] m = "election".getBytes();
            InetAddress aHost = InetAddress.getByName(this.p.getUnicastAddress());
            int receivingPort = Integer.parseInt(((JSONObject) profiles.get(processId - 1)).get("unicastSocket").toString());
            DatagramPacket request = new DatagramPacket(m, "election".length(), aHost, receivingPort);
            unicastSocket.send(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
