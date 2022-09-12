import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UnicastListener extends Thread {
    DatagramSocket unicastSocket; // Socket for unicast communication
    DatagramPacket messageUp = null;

    public UnicastListener(DatagramSocket unicastSocket) {
        this.unicastSocket = unicastSocket;
        this.start();
    }

    public void run() {
        try {
            byte[] buffer = new byte[1000];
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            unicastSocket.receive(messageIn);
            messageUp = messageIn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DatagramPacket getMessageUp() {
        return messageUp;
    }
}
