import org.eclipse.paho.client.mqttv3.MqttClient;

public class QueueBufferNode {

    private int mobile, m1, m2;
    private String DB_message;
    private MqttClient mqttClient;

    // Constructors -----------------------------------------------------------

    public QueueBufferNode(int mobile) {
        this.mobile = mobile;
    }

    public QueueBufferNode(int mobile, String DB_message) {
        this.mobile = mobile;
        this.mqttClient = null;
        this.DB_message = DB_message;
    }

    public QueueBufferNode(int mobile, int m1, int m2, MqttClient mqttClient) {
        this.mobile = mobile;
        this.m1 = m1;
        this.m2 = m2;
        this.mqttClient = mqttClient;
    }
    // Setters -----------------------------------------------------------------

    public void setMobile(int mobile) {
        this.mobile = mobile;
    }

    public void setM1(int m1) {
        this.m1 = m1;
    }

    public void setM2(int m2) {
        this.m2 = m2;
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void setAllInt(int mobile, int m1, int m2) {
        this.mobile = mobile;
        this.m1 = m1;
        this.m2 = m2;
    }

    public void setAll(int mobile, int m1, int m2, MqttClient mqttClient) {
        this.mobile = mobile;
        this.m1 = m1;
        this.m2 = m2;
        this.mqttClient = mqttClient;
    }

    // Getters ------------------------------------------------------------------

    public int getMobile() {
        return mobile;
    }

    public int getM1() {
        return m1;
    }

    public int getM2() {
        return m2;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public String getDB_message() {
        return DB_message;
    }
}

