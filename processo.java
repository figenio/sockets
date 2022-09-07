import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Iterator;

public class processo {
    private int id;
    private String unicastAddress;
    private String multicastAddress;
    private int socket;

    private int idCoordenador;

    private int t1; // Tempo do Olá
    private int t2; // Tempo de Resposta
    private int t3; // Tempo de Coordenador

        public static void main(String args[]){
            MulticastSocket s = null;
            try {
                JSONParser parser = new JSONParser();
                JSONArray processos = (JSONArray) parser.parse(new FileReader("profiles.json"));

                Iterator<JSONObject> iterator = processos.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }

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
            }catch (ParseException e) { e.printStackTrace();
            }finally {if(s != null) s.close();}
    }
}

// thread multicast pra ouvir coordenador ou novo coordenador

// thread unicast para lidar com eleições
