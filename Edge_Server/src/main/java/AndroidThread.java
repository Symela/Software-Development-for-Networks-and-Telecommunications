import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AndroidThread implements Runnable {

    private ConcurrentLinkedQueue<QueueBufferNode> queue;

    AndroidThread(ConcurrentLinkedQueue<QueueBufferNode> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        while (true) {
            if (!queue.isEmpty()) {
                QueueBufferNode helper = queue.poll();
                if (helper.getMobile() == -1) {
                    System.out.println("\nANDROID OUT! PEACE :P");
                    break;
                }

                MqttMessage response = new MqttMessage();
                response.setPayload(String.valueOf(helper.getM1()).getBytes());
                MqttClient mqttClient = helper.getMqttClient();

                try {
                    if (helper.getMobile() == 1)
                        mqttClient.publish("edge1", response);
                    else if (helper.getMobile() == 2)
                        mqttClient.publish("edge2", response);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                response.setPayload(String.valueOf(helper.getM2()).getBytes());

                if (helper.getM2() == 2) {
                    try {
                        mqttClient.publish("edge1", response);
                        mqttClient.publish("edge2", response);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mqttClient.publish("edge2", response);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
