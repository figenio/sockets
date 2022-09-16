import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Process {
    private int id; // Process id
    private int coordinatorId; // Coordinator id
    int multicastGroupPort; // Multicast group port
    private final InetAddress multicastGroupAddress; // Multicast group address
    private InetAddress processAddress;
    private DatagramSocket unicastSocket;
    private MulticastSocket multicastSocket;
    private UnicastListener unicastListener;
    private MulticastListener multicastListener;

    private static int t1 = 8; // Hi message time
    private static int t2 = 10; // Election message time
    private static int t3 = 12; // Coordinator message time

    JSONArray profiles; // Register of other processes

    public Process(int id, String unicastAddress, String multicastAddress, int unicastPort, int multicastGroupPort) {
        try {
            // Sets variables and creates Sockets of process
            this.id = id; // Process id
            this.multicastGroupPort = multicastGroupPort; // Multicast group port to access
            processAddress = InetAddress.getByName(unicastAddress); // Process Address
            multicastGroupAddress = InetAddress.getByName(multicastAddress); // Multicast Group Address to access

            unicastSocket = new DatagramSocket(unicastPort); // Setting socket for unicast communication
            multicastSocket = new MulticastSocket(multicastGroupPort); // Setting socket for multicast communication

            multicastSocket.joinGroup(multicastGroupAddress); // Joining multicast group

            // Creates listeners threads to receive messages
            unicastListener = new UnicastListener(unicastSocket);
            multicastListener = new MulticastListener(multicastSocket);

            // Reads and registers other processes info from JSON config file
            JSONParser parser = new JSONParser();
            profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));

            // Starts process activities
            initiateProcess();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void initiateProcess() {
        try {
            while (true) {
                if (id == coordinatorId) {
                    byte[] buffer = "hi".getBytes();
                    DatagramPacket message = new DatagramPacket(buffer, buffer.length, multicastGroupAddress, multicastGroupPort);
                    multicastSocket.send(message);
                    System.out.println("(t1) Sending Hi");
                    Thread.sleep(t1 * 1000L);
                } else {
                    // Waits T1 for messages of hi or election
                    System.out.println("(t1) Waiting messages");
                    Thread.sleep(t1 * 1000L);

                    // Gets the messages stored in the listeners
                    DatagramPacket unicastMessageIn = unicastListener.getDatagramToReturn();
                    String multicastMessageIn = multicastListener.getMessageToReturn();

                    // Checks if received unicast message and if it is an election message
                    if (unicastMessageIn != null && new String(unicastMessageIn.getData()).startsWith("election")) {
                        System.out.println("Received election message");
                        // And starts own election
                        startElection();
                    } else if (multicastMessageIn != null) {
                        System.out.println("Multicast:" + multicastMessageIn);
                    } else {
                        Scanner scanner = new Scanner(System.in); // Create input reader
                        System.out.println("\nNo coordinator alive, do you want to start a election? (y/n)"); // Ask input
                        String userOption = scanner.nextLine();  // Read input

                        if (userOption.equals("y")) {
                            coordinatorId = 0;
                            startElection();
                        } else if (userOption.equals("n")) {
                            System.out.println("No election");
                        } else {
                            System.out.println("Invalid input");
                        }
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startElection() {
        System.out.println("Starting election");
        try {
            // Sends election messages to all processes bigger than self
            for (int i = 4; i > id; i--) {
                sendElectionMessage(i);
            }

            // Waits for response
            System.out.println("(t2) Waiting for responses");
            Thread.sleep(t2 * 1000L);

            // Clears all the unicast buffer of the expected messages searching for at least one valid response
            boolean hadAnyResponse = false;
            for (int i = 4; i > id; i--) {
                DatagramPacket message = unicastListener.getDatagramToReturn(); // Retrieves any message received in unicast
                if (message != null && new String(message.getData()).startsWith("response")) {
                    hadAnyResponse = true;
                }
            }

            // If it received at least one response, waits for coordinator message
            if (hadAnyResponse) {
                System.out.println("(t3) Response received");
                Thread.sleep(t3 * 1000L); // Waits coordinator message

                // Searches all the messages in the multicast buffer for a new coordinator
                String messageIn = "message reader";
                while (messageIn != null) {
                    messageIn = multicastListener.getMessageToReturn(); // Retrieves any message received in multicast

                    // If received a valid message that isn't a hi, then it's a new coordinator
                    if (messageIn != null && !messageIn.startsWith("hi")) {
                        String newCoordinatorId = Character.toString(messageIn.charAt(0));
                        System.out.println("Received new coordinator: " + newCoordinatorId);
                        setCoordinatorId(Integer.parseInt(newCoordinatorId));
                    }
                }

                // If it doesn't have a coordinator after the election, restarts the election
                if (!(coordinatorId > 0)) {
                    startElection();
                }
            } else {
                System.out.println("No response received");
                becomeCoordinator();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendElectionMessage(int processId) {
        System.out.println("Sending election message to: " + profiles.get(processId - 1));
        try {
            byte[] m = "election".getBytes();
            // Sets address and ports of target process
            InetAddress aHost = InetAddress.getByName(((JSONObject) profiles.get(processId - 1)).get("unicastAddress").toString());
            int targetPort = Integer.parseInt(((JSONObject) profiles.get(processId - 1)).get("unicastSocket").toString());
            // Builds Datagram
            DatagramPacket request = new DatagramPacket(m, m.length, aHost, targetPort);
            // Sends Datagram
            unicastSocket.send(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void becomeCoordinator() {
        System.out.println("Becoming Coordinator");
        try {
            byte[] b = String.valueOf(id).getBytes(); // Sends self id to multicast
            System.out.println("Self id to coordinator: " + new String(b) + ".");
            // Creates Datagram
            DatagramPacket m = new DatagramPacket(b, b.length, multicastGroupAddress, multicastGroupPort);
            // Sends datagram
            multicastSocket.send(m);
            setCoordinatorId(id); // Sets self id has coordinator
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCoordinatorId(int coordinatorId) {
        System.out.println("Setting new coordinator: " + coordinatorId);
        this.coordinatorId = coordinatorId;
    }

    /* --- Functional Methods --- */

    public boolean hasGreaterId() {
        if (id == 4) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
