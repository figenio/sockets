import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class process {

    public static void main(String args[]) {
        // Profile setup by user
        Scanner scanner = new Scanner(System.in); // Create input reader
        System.out.println("Enter profile to use (1-4):"); // Read input
        int option = scanner.nextInt();  // Assign profile

        try {
            // Read JSON with profile options
            JSONParser parser = new JSONParser();
            JSONArray profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));
            System.out.println("Chosen profile" + ((JSONObject) profiles.get(option - 1)));

            Profile p = new Profile(
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("id").toString()),
                    ((JSONObject) profiles.get(option - 1)).get("unicastAddress").toString(),
                    ((JSONObject) profiles.get(option - 1)).get("multicastAddress").toString(),
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("unicastSocket").toString()),
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("multicastSocket").toString())
            );

            DatagramSocket unicastSocket = new DatagramSocket(p.getUnicastSocket());

            InetAddress multicastGroup = InetAddress.getByName(p.getMulticastAddress()); // Multicast Group Address
            int multicastPort = p.getMulticastSocket(); // Port of multicast group
            MulticastSocket multicastSocket = new MulticastSocket(p.getMulticastSocket()); // Socket for multicast communication

            multicastSocket.joinGroup(multicastGroup);

            System.out.println("Starting participation in multicast group");
            while (true) {
                if (p.getId() == p.getCoordinatorId()) {
                    byte[] buffer = "hi".getBytes();
                    DatagramPacket message = new DatagramPacket(buffer, buffer.length, multicastGroup, multicastPort);
                    multicastSocket.send(message);
                    System.out.println("Sending Hi");
                    Thread.sleep(Profile.getT1() * 1000L);
                } else {
                    MulticastListener multicastListener = new MulticastListener(multicastSocket);
                    UnicastListener unicastListener = new UnicastListener(unicastSocket);

                    Thread.sleep(Profile.getT1() * 1000L);

                    DatagramPacket unicastMessageIn = unicastListener.getMessageUp();
                    String multicastMessageIn = multicastListener.getMessageToReturn();

                    unicastListener.stop();
                    multicastListener.stop();

                    if (unicastMessageIn != null) {
                        System.out.println("Received unicast");
                        if (new String(unicastMessageIn.getData()).equals("election")) {
                            System.out.println("Responding election");
                            byte[] m = "response".getBytes();
                            DatagramPacket request = new DatagramPacket(m, "election".length(), unicastMessageIn.getAddress(), unicastMessageIn.getPort());
                            unicastSocket.send(request);
                        }
                    }

                    System.out.println("Receiving:" + multicastMessageIn);
                    if (multicastMessageIn != null) {
                        if (isNumeric(multicastMessageIn)) {
                            p.setCoordinatorId(Integer.parseInt(multicastMessageIn));
                        }
                    } else {
                        System.out.println("No coordinator, do you want to start a election? (y/n)"); // Read input
                        String userOption = scanner.nextLine();  // Assess user option

                        if (userOption.equals("y")) {
                            startElection(p, profiles, unicastSocket, multicastSocket);
                        } else if (userOption.equals("n")) {
                            System.out.println("No election");
                        } else {
                            System.out.println("Invalid input");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startElection(Profile p, JSONArray profiles, DatagramSocket unicastSocket, MulticastSocket multicastSocket) {
        System.out.println("Starting election");
        try {
            if (p.hasGreaterId()) {
                System.out.println("Greater id");
                becomeCoordinator(p, multicastSocket);
            } else {
                // Sends election message to all bigger than it
                for (int i = 4; i > p.getId(); i--) {
                    sendElectionMessage(i, p, profiles, unicastSocket);
                }
                UnicastListener unicastListener = new UnicastListener(unicastSocket);
                Thread.sleep(Profile.getT2() * 1000L);
                DatagramPacket message = unicastListener.getMessageUp();
                unicastListener.stop();

                if (message != null && new String(message.getData()).equals("response")) {
                    System.out.println("Response received");
                    MulticastListener multicastListener = new MulticastListener(multicastSocket);
                    Thread.sleep(Profile.getT3() * 1000L);
                    String messageIn = multicastListener.getMessageToReturn();
                    multicastListener.stop();

                    if (messageIn.length() > 0 && isNumeric(messageIn)) {
                        p.setCoordinatorId(Integer.parseInt(messageIn));
                    }
                } else {
                    becomeCoordinator(p, multicastSocket); // if not, becomes coordinator
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void becomeCoordinator(Profile p, MulticastSocket multicastSocket) {
        System.out.println("Becoming Coordinator");
        try {
            byte[] b = String.valueOf(p.getId()).getBytes(); // sends self id to multicast
            DatagramPacket m = new DatagramPacket(b, b.length, InetAddress.getByName(p.getMulticastAddress()), p.getMulticastSocket());
            multicastSocket.send(m);
            p.setCoordinatorId(p.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendElectionMessage(int processId, Profile p, JSONArray profiles, DatagramSocket unicastSocket) {
        System.out.println("Sending election message");
        try {
            byte[] m = "election".getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int receivingPort = Integer.parseInt(((JSONObject) profiles.get(processId - 1)).get("unicastSocket").toString());
            DatagramPacket request = new DatagramPacket(m, "election".length(), aHost, receivingPort);
            unicastSocket.send(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

// thread multicast pra ouvir coordenador ou novo coordenador
// Create process p with chosen profile of JSON
//profile p = new profile(
//        Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("id").toString()),
//        ((JSONObject) profiles.get(option - 1)).get("unicastAddress").toString(),
//        ((JSONObject) profiles.get(option - 1)).get("multicastAddress").toString(),
//        Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("unicastSocket").toString()),
//        Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("multicastSocket").toString())
//);
//            p.setM(new multicastListener(p));
//                    p.setU(new unicastListener(p));
// thread unicast para lidar com eleições
