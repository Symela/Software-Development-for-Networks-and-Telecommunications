import com.opencsv.CSVReader;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Edge {
    //Multiple test csv files coming from Android, so maybe we need multiple csv files...
    public static final String OUT_CSV_PATH = "./test.csv";  // The path that the created test csv file is.
    //    public static final String OUT_CSV_PATH = "/home/sokb/project/Edge_Server/test.csv";  // The path that the created test csv file is.
    static Experiment[] exp_array = new Experiment[36];    //Array of training Experiments for the knn. Declaring here for visibility

    static String[] labels = {"EyesOpened", "EyesClosed"};
    static int K = 9;  //number of neighbours

    //TODO: 1. change backhaul IP!
    public static String hostname = "192.168.4.164";
    public static int portNumber = 8000;


    private Edge() {
    }

    public static void main(String[] args) throws IOException {


        // buffer for android
        ConcurrentLinkedQueue<QueueBufferNode> androidBuffy = new ConcurrentLinkedQueue<QueueBufferNode>();
        // buffer for backhaul
        ConcurrentLinkedQueue<QueueBufferNode> backhaulBuffy = new ConcurrentLinkedQueue<QueueBufferNode>();


        int filesize = 6022386; // filesize temporary hardcoded
        long start = System.currentTimeMillis();
        int bytesRead;
        int current = 0;
        // localhost for testing

        Socket sock = new Socket(hostname, portNumber);
        System.out.println("Connecting...");
        // receive file
        byte[] mybytearray = new byte[filesize];
        InputStream is = sock.getInputStream();
        FileOutputStream fos = new FileOutputStream("abc.csv");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray, 0, mybytearray.length);  // reads up to mybytearray.length bytes of byte array
        current = bytesRead;
        //if a smaller number than mybytearray.length is read, read remaining bytes
        do {
            bytesRead =
                    is.read(mybytearray, current, (mybytearray.length - current));
            if (bytesRead >= 0) current += bytesRead;
        } while (bytesRead > -1);
        bos.write(mybytearray, 0, current);
        bos.flush();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        bos.close();
        sock.close();

        String CSV_FILE_PATH = "./abc.csv";

        //Reads all CSV files and creates an Experiment object for each of them
        // String CSV_FILE_PATH = CSV_FILE_PATH1.concat(file.getName());
        try {
            Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH));

            CSVReader csvReader = new CSVReader(reader);

            List<String[]> records = csvReader.readAll();   //Stores file in a list of String arrays
            //System.out.println(records.size() - 1);
            Iterator<String[]> iterator = records.iterator();
            String[] tempStringArray;

            int counter = 0;
            while (iterator.hasNext()) {        // iterates through all the arrays of strings in the list
                double[] feature_vector = new double[14];
                String name;

                if (counter == 0) {           //ignores first line (header)
                    iterator.next();
                    counter++;
                    continue;
                }
                tempStringArray = iterator.next();
                name = tempStringArray[0];      // during each iteration the first element (exp name) is saved in the "name" variable
                for (int i = 0; i < 14; i++) {    // iterates through each string in the "current" row of the csv
                    // array of doubles for each experiment
                    feature_vector[i] = Double.parseDouble(tempStringArray[i + 1]);   // starts from the second column to skip the experiment name                            }

                }

                exp_array[counter - 1] = new Experiment(feature_vector, name); //temporary file name
                //System.out.println(exp_array[counter - 1].get_name());

                counter++;
            }

                /*for (int i=0;i < records.size()-1;i++){
                    System.out.println((Arrays.toString(exp_array[i].get_feature_vector())));
                }*/

        } catch (Exception e) {
            System.out.println("Exception\n");
        }

        MamaThread mt = new MamaThread(androidBuffy, backhaulBuffy);
        Thread t = new Thread(mt, "mqttThread");
        t.start();
        AndroidThread at = new AndroidThread(androidBuffy);
        Thread t1 = new Thread(at, "anThread");
        t1.start();
        BackhaulThread bt = new BackhaulThread(backhaulBuffy);
        Thread t2 = new Thread(bt, "bhThread");
        t2.start();
        try {
            t.join();
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GrandFinale gt = new GrandFinale();
        Thread t3 = new Thread(gt, "LetItBeMusic");
        t3.start();
        System.out.println("Printing statistics:");

        synchronized (MamaThread.vV){
            System.out.println( ("Success rate of knn for k = " + K +" is: "+ (MamaThread.vV.success_counter / (double)MamaThread.vV.total_tests)*100) + "%" );
        }
        System.out.println("\n\n\n\n\n\n\n\n\n\n\t\tWOW... that was a lot of work......   ROLL CREDITS!!!!!" +
                "\n\n\n\t\t\t*STAR WARS THEME SONG PLAYING IN BACKGROUND*" +
                "\n\n\n\t--------------------------- Credits ----------------------------" +
                "\n\n\n\t\t\tSpecial thanks to..." +
                "\n\n\t\t\t  * This group OFC!" +
                "\n\n\t\t\t\t  - Βαγγέλης Τζιρώνης" +
                "\n\n\t\t\t\t  - Γιάννης Μανωλάκης" +
                "\n\n\t\t\t\t  - Συμέλα Φωτεινή Κομίνη" +
                "\n\n\t\t\t\t  - Σωκράτης Μπέης" +
                "\n\n\t\t\t  * Our mothers that made us" +
                "\n\n\t\t\t  * Our fathers who... well they helped in making us!" +
                "\n\n\t\t\t  * StackOverflow! It was SOOOOO useful! <3" +
                "\n\n\t\t\t\t and finally," +
                "\n\n\t\t\t  * GOD who enlightened us in order to complete this project!" +
                "\n\n\n\n\t\t\t\t\tCya la.... NEVER AGAIN ;)\n\n\n\n\n\n\n\n\n");

        try {
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
