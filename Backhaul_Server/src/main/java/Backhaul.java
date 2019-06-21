import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

public class Backhaul {

    private static int portNumber = 8000;
    private static final String OUT_CSV_PATH = "./out_file.csv";

    public static void main(String[] args) throws IOException {

        // ------------- JDBC ----------------------------------------------------------------------------------
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }
        System.out.println("MySQL JDBC Driver Registered!");
        Connection conn = null;

        try {

	    //TODO: username and password for access in database
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys?useLegacyDatetimeCode=false&serverTimezone=UTC", <username>, <password>);
            if (conn != null) {
                System.out.println("You made it, take control your database now!");
                System.out.println(conn);
            } else {
                System.out.println("Failed to make connection!");
            }

        } catch (Exception e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
        //check_data(conn);

        // ---------- TRAINING ------------------------------------------------------------------------------------------

        File folder = new File("./Training_Set");

        File[] listOfFiles = folder.listFiles();    //listFiles is an array of file pathnames

        Experiment[] exp_array = new Experiment[listOfFiles.length];    //array of Experiment objects

        String CSV_FILE_PATH1 = "./Training_Set/";
        int k = 0; // the index of the array of experiments

        //Reads all CSV files and creates an Experiment object for each of them
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String file_name = file.getName();
                String CSV_FILE_PATH = CSV_FILE_PATH1.concat(file_name);
                try {
                    Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH));

                    CSVReader csvReader = new CSVReader(reader);

                    List<String[]> records = csvReader.readAll();   //Stores file in a list of String arrays

                    Iterator<String[]> iterator = records.iterator();
                    String[] tempStringArray;

                    double[][] input_vectors = new double[14][records.size() - 1];    //input for the calculateEntropy function
                    double[] feature_vector = new double[14];                       //output of calculateEntropy function

                    int counter = 0;
                    while (iterator.hasNext()) {    //stores entries in input_vectors as doubles
                        if (counter == 0) {           //ignores first line (header)
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

                    exp_array[k] = new Experiment(feature_vector, file_name.substring(file_name.indexOf(".") + 1, file_name.indexOf("d") + 1));
                    k++;

                } catch (Exception e) {
                    System.out.println("Exception in reader");
                }
            }
        }

        //Creates the new CSV file
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(OUT_CSV_PATH));

            String[] headerRecord = {"Name of Experiment", "AF3", "F7", "F3", "FC5", "T7", "P7", "O1", "O2", "P8", "T8", "FC6", "F4", "F8", "AF4"};
            csvWriter.writeNext(headerRecord);

            for (int j = 0; j < exp_array.length; j++) {
                String[] new_line = new String[15];
                new_line[0] = exp_array[j].get_name();
                double[] temp = exp_array[j].get_feature_vector();

                String[] temp_str = new String[temp.length];
                for (int i = 0; i < temp_str.length; i++) {
                    temp_str[i] = String.valueOf(temp[i]);
                    new_line[i + 1] = temp_str[i];
                }
                csvWriter.writeNext(new_line);
            }
            csvWriter.close();
        } catch (Exception ex) {
            System.out.println("exception in writer");
        }

        // create socket
        ServerSocket servsock = new ServerSocket(portNumber);
        while (true) {
            System.out.println("Waiting...");
            Socket sock = servsock.accept();
            System.out.println("Accepted connection : " + sock);
            // sendfile
            File myFile = new File("./out_file.csv");
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            OutputStream os = sock.getOutputStream();
            System.out.println("Sending...");
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            sock.close();
            DatabaseThread dataThread = new DatabaseThread(conn);
            Thread t = new Thread(dataThread);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static void check_data(Connection conn) {       // a testing function to print some information
        // from a dummy entry

        PreparedStatement ps = null;


        try {
            String query = "select * from events";  // a dummy query
            ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {     // while there is data to be printed
                System.out.println("events1 " + rs.getFloat("gps_signal"));
            }
        } catch (Exception e) {
            System.out.println(e);


        }
    }
}
