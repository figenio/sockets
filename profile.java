public class profile {
    private int id;
    private String unicastAddress;
    private String multicastAddress;
    private int unicastSocket;
    private int multicastSocket;

    private int bullyId; // Id do Coordenador

    private multicastListener m;
    private unicastListener u;

    private static int t1 = 3; // Tempo do Olá
    private static int t2 = 2; // Tempo de Resposta
    private static int t3 = 1; // Tempo de Coordenador

    public profile(int id, String unicastAddress, String multicastAddress, int unicastSocket, int multicastSocket) {
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

    public void setBullyId(int bullyId) {
        this.bullyId = bullyId;
    }

    public int getId() {
        return id;
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

    public void setM(multicastListener m) {
        this.m = m;
    }

    public void setU(unicastListener u) {
        this.u = u;
    }

    public boolean hasGreaterId () {
        if (id == 4) {
            return true;
        } else {
            return false;
        }
    }

    public void startElection() throws InterruptedException {
        u.startElection();
    }

    public void becomeCoordinator() {
        m.becomeCoordinator();
    }

    public void waitCoordinator() {
        m.waitCoordinator();
    }
}
