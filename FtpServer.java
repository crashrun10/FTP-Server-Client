import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SHIVAM on 3/12/2016.
 */
public class FtpServer {

    public static void main(String[] args) {

        // variable to store port numbers for commands and data
        int cmdPort;
        int dataPort;

        Scanner in = new Scanner(System.in);

        // Take port number for commands
        System.out.print("Enter port number for commands: ");
        cmdPort = Integer.parseInt(in.nextLine());
        // Take port number for data
        System.out.print("Enter port number for data: ");
        dataPort = Integer.parseInt(in.nextLine());

        ExecutorService executor = null;

        try (ServerSocket cmd = new ServerSocket(cmdPort);) {

            executor = Executors.newFixedThreadPool(5);

            // Informing user that server is running now
            System.out.println("FTP Server Started." +
                "\nListening for commands on port: " + cmdPort +
                "\nPort for transfer of data: " + dataPort);

            while (true) {

                Socket cmdSocket = cmd.accept();
                Runnable worker = new RequestHandler(cmdSocket, dataPort);
                executor.execute(worker);

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if(executor != null) {
                executor.shutdown();
            }
        }


    }
}
