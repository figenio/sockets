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
    private InetAddress multicastGroupAddress;
    private InetAddress processAddress;
    private DatagramSocket unicastSocket;
    private MulticastSocket multicastSocket;
    private UnicastListener unicastListener;
    private MulticastListener multicastListener;

    private static int t1 = 5; // Hi message time
    private static int t2 = 7; // Election message time
    private static int t3 = 10; // Coordinator message time

    JSONArray profiles; // Register of other processes

    public Process(int id, String unicastAddress, String multicastAddress, int unicastPort, int multicastGroupPort) {
        // Sets variables and creates Sockets
        try {
            this.id = id;
            this.multicastGroupPort = multicastGroupPort;
            processAddress = InetAddress.getByName(unicastAddress); // Process self Address
            multicastGroupAddress = InetAddress.getByName(multicastAddress); // Multicast Group Address

            unicastSocket = new DatagramSocket(unicastPort); // Setting socket for unicast communication
            multicastSocket = new MulticastSocket(multicastGroupPort); // Socket for multicast communication

            multicastSocket.joinGroup(multicastGroupAddress); // Joining multicast group

            // Create listeners to receive messages
            unicastListener = new UnicastListener(unicastSocket);
            multicastListener = new MulticastListener(multicastSocket);

            // Reads and registers other processes info
            JSONParser parser = new JSONParser();
            profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));

            initiateProcess(); // Starting process activities
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void initiateProcess() {
        try {
            while (true) {
                System.out.print("Waiting messages - ");
                if (id == coordinatorId) {
                    byte[] buffer = "hi".getBytes();
                    DatagramPacket message = new DatagramPacket(buffer, buffer.length, multicastGroupAddress, multicastGroupPort);
                    multicastSocket.send(message);
                    System.out.println("Sending Hi");
                }

                // Waits T1 for messages of hi or election
                Thread.sleep(t1 * 1000L);

                // Gets the messages stored in the listeners
                DatagramPacket unicastMessageIn = unicastListener.getDatagramToReturn();
                String multicastMessageIn = multicastListener.getMessageToReturn();

                if (unicastMessageIn != null) {
                    // Checks if received unicast message and if it is an election message
                    System.out.println("Unicast in: " + new String(unicastMessageIn.getData()));
                    // If so, answers the process that send it that the message was received
                    if (new String(unicastMessageIn.getData()).startsWith("election")) {
                        System.out.println("Sending election response");
                        byte[] m = "response".getBytes();
                        DatagramPacket request = new DatagramPacket(m, m.length, unicastMessageIn.getAddress(), unicastMessageIn.getPort());
                        unicastSocket.send(request);
                        if (multicastMessageIn != null) {
                            String newCoordinatorId = Character.toString(multicastMessageIn.charAt(0));
                            if (isNumeric(newCoordinatorId)) {
                                // If multicast message is an id, sets new coordinator
                                System.out.println("New coordinator id via multicast:" + newCoordinatorId);
                                setCoordinatorId(Integer.parseInt(newCoordinatorId));
                            } else if (!(id == coordinatorId)) {
                                startElection();
                            }
                        } else if (!(id == coordinatorId)) {
                            startElection();
                        }
                    }
                } else if (multicastMessageIn != null && !(id == coordinatorId)) {
                    // Checks if received multicast message
                    System.out.println("Multicast in:" + multicastMessageIn);
                    String newCoordinatorId = Character.toString(multicastMessageIn.charAt(0));
                    if (isNumeric(newCoordinatorId)) {
                        // If multicast message is an id, sets new coordinator
                        System.out.println("New coordinator id via multicast:" + newCoordinatorId);
                        setCoordinatorId(Integer.parseInt(newCoordinatorId));
                    }
                } else if (!(id == coordinatorId)) {
                    // If no message, asks user permission to start election
                    Scanner scanner = new Scanner(System.in); // Create input reader
                    System.out.println("\nNo coordinator alive, do you want to start a election? (y/n)"); // Ask input
                    String userOption = scanner.nextLine();  // Read input

                    if (userOption.equals("y")) {
                        startElection();
                    } else if (userOption.equals("n")) {
                        System.out.println("No election");
                    } else {
                        System.out.println("Invalid input");
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
            // Sends election messages to all processes bigger than it
            for (int i = 4; i > id; i--) {
                sendElectionMessage(i);
            }
            // Waits for response
            Thread.sleep(t2 * 1000L);
            DatagramPacket message = unicastListener.getDatagramToReturn(); // Retrieves any message received in unicast

            if (message != null && new String(message.getData()).startsWith("response")) {
                // If received a response message
                System.out.println("Response received");
                Thread.sleep(t3 * 1000L); // Waits coordinator message
                String messageIn = multicastListener.getMessageToReturn(); // Retrieves any message received in multicast

                System.out.println("After response, it received: " + messageIn);
                // If received a coordinator id, sets new coordinator
                if (messageIn != null) {
                    String newCoordinatorId = Character.toString(messageIn.charAt(0));
                    if (isNumeric(newCoordinatorId)) {
                        // If multicast message is an id, sets new coordinator
                        System.out.println("Received new coordinator: " + messageIn);
                        setCoordinatorId(Integer.parseInt(newCoordinatorId));
                    }
                } else {
                    startElection();
                }
            } else {
                // If didn't receive any response, becomes coordinator
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
            // Address and port of target process
            InetAddress aHost = InetAddress.getByName("localhost");
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
            DatagramPacket m = new DatagramPacket(b, b.length, multicastGroupAddress, multicastGroupPort);
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
