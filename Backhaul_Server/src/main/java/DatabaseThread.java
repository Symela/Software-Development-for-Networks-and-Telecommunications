import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseThread implements Runnable {

    private int portNumber = 9000;
    private Connection conn;

    public DatabaseThread() {
    }

    public DatabaseThread(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                assert serverSocket != null;
                Socket sock = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String message = in.readLine();
                if (message.equals("exit")) {
                    in.close();
                    sock.close();
                    serverSocket.close();
                    break;
                }
                String[] fields = message.split(" ");       // New Shit!!!!

                Statement query = conn.createStatement();
                String s = "INSERT INTO events (TimeStamp,Gps_Signal,Criticality_Level,Android_IDs)" + "VALUES ('" + fields[2] + "', '" + fields[1] + "', '" + fields[3] + "', '" + fields[0] + "')";
                query.executeUpdate(s);


                System.out.println(message);
                in.close();
                sock.close();
            } catch (IOException | SQLException e) {
//            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
