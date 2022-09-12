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
                if (id == coordinatorId) {
                    byte[] buffer = "hi".getBytes();
                    DatagramPacket message = new DatagramPacket(buffer, buffer.length, multicastGroupAddress, multicastGroupPort);
                    multicastSocket.send(message);
                    System.out.println("Sending Hi");
                    Thread.sleep(t1 * 1000L);
                } else {
                    System.out.print("Creating listeners - ");
                    // Create listeners to receive messages
                    MulticastListener multicastListener = new MulticastListener(multicastSocket);
                    UnicastListener unicastListener = new UnicastListener(unicastSocket);

                    // Waits T1 for messages of hi or election
                    Thread.sleep(t1 * 1000L);

                    // Gets the messages stored in the listeners
                    DatagramPacket unicastMessageIn = unicastListener.getMessageUp();
                    String multicastMessageIn = multicastListener.getMessageToReturn();

                    // Stops the running threads
                    unicastListener.stop();
                    multicastListener.stop();

                    if (unicastMessageIn != null) {
                        // Checks if received unicast message and if it is an election message
                        System.out.println("Unicast in:" + new String(unicastMessageIn.getData()));
                        // If so, answers the process that send it that the message was received
                        if (new String(unicastMessageIn.getData()).equals("election")) {
                            byte[] m = "response".getBytes();
                            DatagramPacket request = new DatagramPacket(m, "election".length(), unicastMessageIn.getAddress(), unicastMessageIn.getPort());
                            unicastSocket.send(request);
                        }
                    } else if (multicastMessageIn != null) {
                        // Checks if received multicast message
                        System.out.println("Multicast in:" + multicastMessageIn);
                        if (isNumeric(multicastMessageIn)) {
                            // If multicast message is an id, sets new coordinator
                            setCoordinatorId(Integer.parseInt(multicastMessageIn));
                        }
                    } else {
                        // If no message, asks user permission to start election
                        Scanner scanner = new Scanner(System.in); // Create input reader
                        System.out.println("\n No coordinator alive, do you want to start a election? (y/n)"); // Ask input
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
            // Creates unicast listener and awaits for response
            UnicastListener unicastListener = new UnicastListener(unicastSocket);
            Thread.sleep(t2 * 1000L);

            DatagramPacket message = unicastListener.getMessageUp(); // Retrieves any message received in unicast
            unicastListener.stop(); // Kills thread

            System.out.println("Unicast response of election message: " + message);
            if (message != null && new String(message.getData()).equals("response")) {
                // If received a response message
                System.out.println("Response received");
                MulticastListener multicastListener = new MulticastListener(multicastSocket); // Creates multicast listener
                Thread.sleep(t3 * 1000L); // Waits coordinator message
                String messageIn = multicastListener.getMessageToReturn(); // Retrieves any message
                multicastListener.stop(); // Kills thread

                // If received a coordinator id, sets new coordinator
                if (messageIn != null && isNumeric(messageIn)) {
                    setCoordinatorId(Integer.parseInt(messageIn));
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
            DatagramPacket request = new DatagramPacket(m, "election".length(), aHost, targetPort);
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
            DatagramPacket m = new DatagramPacket(b, String.valueOf(id).length(), multicastGroupAddress, multicastGroupPort);
            multicastSocket.send(m);
            setCoordinatorId(id); // Sets self id has coordinator
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCoordinatorId(int coordinatorId) {
        System.out.println("Setting new coordinator:" + coordinatorId);
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
