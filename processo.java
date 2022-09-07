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

public class processo {
    private int id;
    private String unicastAddress;
    private String multicastAddress;
    private int socket;

    private int bullyId; // Id do Coordenador

    private int t1; // Tempo do Olá
    private int t2; // Tempo de Resposta
    private int t3; // Tempo de Coordenador

        public static void main(String args[]){
            try {
                JSONParser parser = new JSONParser();
                JSONArray profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));

                System.out.println(((JSONObject) profiles.get(1)).get("socket"));

                multicastListener m = new multicastListener("228.5.6.7", 6789);
            }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
            }catch (IOException e){System.out.println("IO: " + e.getMessage());
            }catch (ParseException e) { e.printStackTrace();
            }finally {if(s != null) s.close();}
    }
}

// thread multicast pra ouvir coordenador ou novo coordenador

// thread unicast para lidar com eleições
