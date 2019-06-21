import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackhaulThread implements Runnable {

    private ConcurrentLinkedQueue<QueueBufferNode> queue;
    private OutputStream out;
    private int portNumber = 9000;

    BackhaulThread(ConcurrentLinkedQueue<QueueBufferNode> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        while (true) {
            if (!queue.isEmpty()) {
                QueueBufferNode helper = queue.poll();
                if (helper.getMobile() == -1) {
                    System.out.println("\nBACKHAUL OUT!");
                    try {
                        Socket socket = new Socket(Edge.hostname, portNumber);
                        this.out = socket.getOutputStream();
                        out.write("exit".getBytes());
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                try {
                    Socket socket = new Socket(Edge.hostname, portNumber);
                    this.out = socket.getOutputStream();
                    out.write(helper.getDB_message().getBytes());
                    out.flush();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
