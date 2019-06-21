import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.eclipse.paho.client.mqttv3.*;

import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MamaThread implements MqttCallback, Runnable {

    private MqttClient mqttClient;
    private MqttClient mqttCl;
    private ConcurrentLinkedQueue<QueueBufferNode> qa, qb;

    public static final VolatileVariables vV = new VolatileVariables();

    public MamaThread(ConcurrentLinkedQueue<QueueBufferNode> qa, ConcurrentLinkedQueue<QueueBufferNode> qb) {
        this.qa = qa;
        this.qb = qb;
    }

    @Override
    public void run() {
        System.out.println("\nli");
        this.EdgeTopics();
        System.out.println("\nle");
    }

    private void EdgeTopics() {
        try {
            //TODO: 3. MQTT Broker IP
            String mqttBrokerIP = <MQTT IP> + ":1883";

            mqttCl = new MqttClient("tcp://" + mqttBrokerIP, "mob1");
            mqttCl.connect();
            mqttCl.setCallback(this);
            mqttCl.subscribe("mobile1");

            mqttClient = new MqttClient("tcp://" + mqttBrokerIP, "mob2");
            mqttClient.connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe("mobile2");


        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void connectionLost(Throwable throwable) {

    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {

        int critical;
        if (topic.equals("mobile1")) {

            synchronized (vV) {
                // Everything important that MUST BE DONE!
                vV.increaseTotal();
                critical = sunartisiForAll(mqttMessage.toString(), vV.total_tests, 1);
            }
            System.out.println("critical is: " + critical);

            if (critical == -1) {
                String[] topics = {"mobile1", "mobile2"};
                mqttCl.unsubscribe(topics);
                mqttClient.unsubscribe(topics);
                mqttCl.disconnect();
                mqttClient.disconnect();
                mqttCl.close();
                mqttClient.close();
            }

        } else {

            synchronized (vV) {
                // Everything important that MUST BE DONE!
                vV.increaseTotal();
                critical = sunartisiForAll(mqttMessage.toString(), vV.total_tests, 2);
            }
            System.out.println(critical);

            if (critical == -1) {
                String[] topics = {"mobile1", "mobile2"};
                mqttCl.unsubscribe(topics);
                mqttClient.unsubscribe(topics);
                mqttCl.disconnect();
                mqttClient.disconnect();
                mqttCl.close();
                mqttClient.close();
            }
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    private int sunartisiForAll(String messageToSplit, int maxMessagesToReceive, int mobile) {
        int criticalLevel = 0;

        String[] lines = usingBufferedReader(messageToSplit);

        String result;
        Experiment exp = CreateTestCSV(lines);
        if(lines[7] != null){
            result = knn_classifier(Edge.exp_array, exp, Edge.labels, Edge.K);
        }
        else{
            result = "EyesOpened";
        }

        System.out.println("Classified by knn as: " + result);
        System.out.println("Actual class is: " + exp.get_name());
        if (result.equals(exp.get_name())) {
            System.out.println("Successful classification!");
            synchronized (vV) {
                vV.increaseSuccessCounter();
            }
        } else {
            System.out.println("Failed classification!");
        }

        double distance;

        if (maxMessagesToReceive < 30) {

            if (result.equals("EyesOpened")) { // EVERYTHING IS FINE

                synchronized (vV) {
                    if (mobile == 1) {
                        vV.setTimesClosed1(0);
                        vV.setMac1(lines[0]);
                        vV.setMobile1Lat(Double.parseDouble(lines[1].replaceAll("[,]", "."))); // Set Latitude for mobile 1
                        vV.setMobile1Lon(Double.parseDouble(lines[2].replaceAll("[,]", "."))); // Set Longitude for mobile 1
                        System.out.println("\nopened_eyes 1  ------> " + vV.timesClosed1);
                    } else {
                        vV.setTimesClosed2(0);
                        vV.setMac2(lines[0]);
                        vV.setMobile2Lat(Double.parseDouble(lines[1].replaceAll("[,]", "."))); // Set Latitude for mobile 1
                        vV.setMobile2Lon(Double.parseDouble(lines[2].replaceAll("[,]", "."))); // Set Longitude for mobile 1
                        System.out.println("\nopened_eyes 2 ------> " + vV.timesClosed2);
                    }
                }

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date cal = new Date();
                System.out.println(dateFormat.format(cal));

                qa.add(new QueueBufferNode(mobile, 0, 0, mqttCl));
                qb.add(new QueueBufferNode(mobile, lines[0] + " " + lines[1] + "," + lines[2] + " " + dateFormat.format(cal) + " 0"));       // need timestamp and mac address from second mobile

            } else if (result.equals("EyesClosed")) { // SOMETHING IS WRONG

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date cal = new Date();
                System.out.println(dateFormat.format(cal));

                synchronized (vV) {
                    if (mobile == 1) { // TO 1o KINITO

                        vV.setMac1(lines[0]);
                        vV.setMobile1Lat(Double.parseDouble(lines[1].replaceAll("[,]", "."))); // Set Latitude for mobile 1
                        vV.setMobile1Lon(Double.parseDouble(lines[2].replaceAll("[,]", "."))); // Set Longitude for mobile 1
                        vV.increaseCounter1();

                        System.out.println("\nclosed_eyes 1  ------> " + vV.timesClosed1);

                        if (vV.timesClosed1 < 3) { // BUT WE SHOULDN'T WORRY

                            qa.add(new QueueBufferNode(mobile, 0, 0, mqttCl));
                            qb.add(new QueueBufferNode(mobile, lines[0] + " " + lines[1] + "," + lines[2] + " " + dateFormat.format(cal) + " 0"));

                        } else { // WORRY WORRY WORRYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

                            // euresh kai elegxos apostashs
                            distance = distanceInKmBetweenEarthCoordinates(vV.mobile1Lat, vV.mobile1Lon, vV.mobile2Lat, vV.mobile2Lon);

                            System.out.println("\ndistance: " + distance + "\n");

                            if (distance < 0.003) { // THA TRAKAROUMEEEEEEEEEEEE

                                qa.add(new QueueBufferNode(mobile, 1, 2, mqttCl));
                                qb.add(new QueueBufferNode(mobile, lines[0] + "__" + vV.mac2 + " " + lines[1] + "," + lines[2] + "__" + vV.mobile2Lat + "," + vV.mobile2Lon + " " + dateFormat.format(cal) + " 2"));

                            } else { // DEN THA TRAKAROUME ALLA KSUPNA

                                qa.add(new QueueBufferNode(mobile, 1, 0, mqttCl));
                                qb.add(new QueueBufferNode(mobile, lines[0] + " " + lines[1] + "," + lines[2] + " " + dateFormat.format(cal) + " 1"));

                            }
                        }

                    } else { // TO 2o KINITO

                        vV.setMac2(lines[0]);
                        vV.setMobile2Lat(Double.parseDouble(lines[1].replaceAll("[,]", "."))); // Set Latitude for mobile 2
                        vV.setMobile2Lon(Double.parseDouble(lines[2].replaceAll("[,]", "."))); // Set Longitude for mobile 2
                        vV.increaseCounter2();

                        System.out.println("\nclosed_eyes 2 ------> " + vV.timesClosed2);

                        if (vV.timesClosed2 < 3) { // WE SHOULDN'T WORRY

                            qa.add(new QueueBufferNode(mobile, 0, 0, mqttCl));
                            qb.add(new QueueBufferNode(mobile, lines[0] + " " + lines[1] + "," + lines[2] + " " + dateFormat.format(cal) + " 0"));

                        } else { // WORRY WORRY WORRYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

                            // euresh kai elegxos apostashs
                            distance = distanceInKmBetweenEarthCoordinates(vV.mobile1Lat, vV.mobile1Lon, vV.mobile2Lat, vV.mobile2Lon);

                            System.out.println("\ndistance: " + distance + "\n");

                            if (distance < 0.003) { // THA TRAKAROUMEEEEEEEEEEEE

                                qa.add(new QueueBufferNode(mobile, 1, 2, mqttCl));
                                qb.add(new QueueBufferNode(mobile, lines[0] + "__" + vV.mac1 + " " + lines[1] + "," + lines[2] + "__" + vV.mobile1Lat + "," + vV.mobile1Lon + " " + dateFormat.format(cal) + " 2"));

                            } else { // DEN THA TRAKAROUME ALLA KSUPNA

                                qa.add(new QueueBufferNode(mobile, 1, 0, mqttCl));
                                qb.add(new QueueBufferNode(mobile, lines[0] + " " + lines[1] + "," + lines[2] + " " + dateFormat.format(cal) + " 1"));
                            }
                        }
                    }
                }

            }
        } else { // proswrinh sunthiki termatismou

            qa.add(new QueueBufferNode(-1));
            qb.add(new QueueBufferNode(-1));
            criticalLevel = -1;
        }

        return criticalLevel;
    }

    private static String[] usingBufferedReader(String mes) {

        int j = 0;
        long length = 0;
        String line8;
        String[] l = mes.split("\n");
        String[] stringas = new String[8];

        while ((l[j] != null) && (j < 7)) {
            stringas[j] = l[j];
            j++;
        }

        line8 = "";

        while ((l[j] != null) && (j < l.length - 1)) {
            length += l[j].length();
            line8 = line8 + l[j].replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "") + "\n";
            stringas[7] = line8;    //stringas[7] is name and stringas[8] is feature vector
            j++;
        }

        return stringas;
    }

    private static double calculateDistance(double[] X, double[] x)  //X: training feature vector, x: test feature vector
    {
        double Sum = 0.0;
        for (int i = 0; i < X.length; i++) {
            Sum = Sum + Math.pow((X[i] - x[i]), 2.0);
        }
        return Math.sqrt(Sum);
    }

    // knn for one test vector
    private String knn_classifier(Experiment[] training_set, Experiment test, String[] labels, int K) {
        int c_opened = 0, c_closed = 0; // counters for votes
        double w_opened = 0.0, w_closed = 0.0, opened_Weight = 0.0, closed_Weight = 0.0;    // counters for weights
        double[][] euc_dist = new double[training_set.length][2];    // 2D array: 1st column: 0/1 (Opened/Closed), 2nd column: distance
        for (int i = 0; i < training_set.length; i++) {
            if (training_set[i].get_name().equals("EyesOpened")) {
                euc_dist[i][0] = 1.0;
            } else {
                euc_dist[i][0] = 0.0;
            }
            euc_dist[i][1] = calculateDistance(training_set[i].get_feature_vector(), test.get_feature_vector());

        }
        double[] I = new double[K]; // set I that contains the k nearest distances
        // sorting by 2nd column (distance)
        Arrays.sort(euc_dist, new java.util.Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                return Double.compare(a[1], b[1]);
            }

        });

        if (K > training_set.length || K <= 1) {
            return "error: Invalid K";
        }
        I[0] = euc_dist[0][1];
        int k = 1;
        for (int l = 1; l < training_set.length; l++) { // fills I with the K nearest distances
            if (euc_dist[l][1] == I[k - 1]) {     // if is duplicate
                continue;
            }
            I[k] = euc_dist[l][1];
            if (euc_dist[l][0] == 1.0) {
                c_opened++;
                w_opened = w_opened + 1.0 / I[k];
                opened_Weight = w_opened * c_opened;

            } else {
                c_closed++;
                w_closed = w_closed + 1.0 / I[k];
                closed_Weight = w_closed * c_closed;
            }
            k++;
            if (k >= K) {
                break;
            }
        }

        if (opened_Weight > closed_Weight) {
            return labels[0];
        } else {
            return labels[1];
        }

    }

    private Experiment CreateTestCSV(String[] test_array) {   //creates test csv file and returns the corresponding Experiment
        try {
            /* delimiter */
            String delimiter = "\n";

            /* given string will be split by the argument delimiter provided. */
            String[] tempArray;
            tempArray = test_array[7].split(delimiter);

            CSVWriter csvWriter = new CSVWriter(new FileWriter(Edge.OUT_CSV_PATH));
            for (int i = 0; i < tempArray.length; i++) {
                csvWriter.writeNext(tempArray[i].split(",")); //created an array of string to be converted to csv file
                // note that writeNext() in Backhaul writes line by line using a String array each time
            }
            csvWriter.close();

        } catch (Exception ex) {
            System.out.println("exception in writer");
        }

        Experiment test_exp = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(Edge.OUT_CSV_PATH));

            CSVReader csvReader = new CSVReader(reader);

            List<String[]> records = csvReader.readAll();   //Stores file in a list of String arrays
            Iterator<String[]> iterator = records.iterator();
            String[] tempStringArray;

            double[][] input_vectors = new double[14][records.size() - 1];    //input for the calculateEntropy function
            double[] feature_vector = new double[14];                       //output of calculateEntropy function

            int counter = 0;
            while (iterator.hasNext()) {    //stores entries in input_vectors as doubles
                if (counter < 2) {           //ignores first two lines (header + empty line)
                    iterator.next();
                    counter++;
                    continue;
                }
                tempStringArray = iterator.next();
                for (int i = 0; i < 14; i++) {
                    input_vectors[i][counter - 1] = Double.parseDouble(tempStringArray[i]);
                }
                counter++;

            }

            //calculate Entropy and create feature vector
            for (int i = 0; i < 14; i++) {
                feature_vector[i] = Entropy.calculateEntropy(input_vectors[i]);
            }
            String file_name = test_array[6];
            test_exp = new Experiment(feature_vector, file_name.substring(file_name.indexOf(".") + 1, file_name.indexOf("d") + 1));

        } catch (Exception ex) {
            System.out.println("exception in reader");
        }
        return test_exp;
    }

    private Double degreesToRadians(Double degrees) {
        return (degrees * Math.PI) / 180;
    }

    private Double distanceInKmBetweenEarthCoordinates(Double lat1, Double lon1, Double lat2, Double lon2) {
        int earthRadiusKm = 6371;

        Double dLat = degreesToRadians(lat2 - lat1);
        Double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
