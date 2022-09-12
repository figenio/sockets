public class Profile {
    private int id;
    private String unicastAddress;
    private String multicastAddress;
    private int unicastSocket;
    private int multicastSocket;

    private int coordinatorId; // Id do Coordenador

    private static int t1 = 1; // Tempo do Olá
    private static int t2 = 3; // Tempo de Resposta
    private static int t3 = 5; // Tempo de Coordenador

    public Profile(int id, String unicastAddress, String multicastAddress, int unicastSocket, int multicastSocket) {
        this.id = id;
        this.unicastAddress = unicastAddress;
        this.multicastAddress = multicastAddress;
        this.unicastSocket = unicastSocket;
        this.multicastSocket = multicastSocket;
    }

    public int getUnicastSocket() {
        return unicastSocket;
    }

    public int getMulticastSocket() {
        return multicastSocket;
    }

    public void setCoordinatorId(int coordinatorId) {
        System.out.println("Setting new coordinator:" + coordinatorId);
        this.coordinatorId = coordinatorId;
    }

    public int getId() {
        return id;
    }

    public int getCoordinatorId() {
        return coordinatorId;
    }

    public String getUnicastAddress() {
        return unicastAddress;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public static int getT1() {
        return t1;
    }

    public static int getT2() {
        return t2;
    }

    public static int getT3() {
        return t3;
    }
    }

    public boolean hasGreaterId () {
        if (id == 4) {
            return true;
        } else {
            return false;
        }
    }
}
