import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by SHIVAM on 3/12/2016.
 */
public class RequestHandler implements Runnable {


    private Socket cmdSocket = null;
    private int dataPort;
    private static final String SERVER_PATH = "C:\\Users\\SHIVAM\\Desktop\\FTP NPL PROJECT\\ftp_server_folder";


    // Constructor to set cmdSocket and dataSocket values
    public RequestHandler(Socket cmdSocket, int dataPort) {
        this.cmdSocket = cmdSocket;
        this.dataPort = dataPort;
    }

    @Override
    public void run() {

        try (BufferedReader cmdIn = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()));
             PrintWriter cmdOut = new PrintWriter(cmdSocket.getOutputStream(), true);) {

            String cmdInput;

            // Inform user that connection is established
            System.out.println("Connection established with: " + cmdSocket.getInetAddress());

            String welcomeMessage = "\nWelcome to MGM FTP Server\nList of commands available:\n"
                    + "1. LIST -> Lists all the files(with index no.) available for download\n"
                    + "2. GET <file_name> -> Downloads the file\n"
                    + "3. EXIT -> Disconnect with the server\n"
                    + "~";

            cmdOut.println(welcomeMessage);

            while ((cmdInput = cmdIn.readLine()) != null) {

                System.out.println("Command: " + cmdInput + " from: " + cmdSocket.getInetAddress());

                if (cmdInput.equals("EXIT")) {
                    cmdOut.println("Thank You for using the server!");
                    System.out.println("Conection closed with: " + cmdSocket.getInetAddress());
                    cmdSocket.close();
                    return;
                }

                if (cmdInput.equals("LIST")) {
                    displayFileList(cmdOut);
                }

                if (cmdInput.startsWith("GET ")) {
                    ServerSocket data = null;
                    Socket dataSocket = null;
                    OutputStream dataOut = null;
                    try {
                        data = new ServerSocket(dataPort);
                        dataSocket = data.accept();
                        dataOut = dataSocket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String filename = cmdInput.substring(4, cmdInput.length());
                    sendFile(filename, cmdOut, dataOut);
                    dataSocket.close();
                    data.close();
                }


            }

        } catch (IOException e) {
            System.out.println("I/O exception: " + e);
        } catch (Exception ex) {
            System.out.println("Exception in Thread Run. Exception : " + ex);
        }
    }

    private void sendFile(String fileName, PrintWriter cmdOut, OutputStream dataOut) {


        File directory = new File(SERVER_PATH);
        File[] list = directory.listFiles();
        boolean file_exist = false;

        for (File file : list) {

            if (file.getName().equals(fileName))
                file_exist = true;
        }

        if (!file_exist) {
            cmdOut.println("No such file on the server!");
            cmdOut.println("~");
            return;
        } else {
            cmdOut.println("EXIST");
        }

        File file = new File(SERVER_PATH + "\\" + fileName);

       // cmdOut.println("~FileName:" + fileName);
        //cmdOut.println("~FileSize:" + file.length());

        InputStream is = null;

        try {

            is = new FileInputStream(file);

            byte[] buffer = new byte[16*1024];
            int count;

            while((count = is.read(buffer)) > 0) {
                dataOut.write(buffer, 0, count);
            }

            dataOut.close();
            //System.out.print("done");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dataOut != null) try {
                dataOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private static void displayFileList(PrintWriter out) {

        File directory = new File(SERVER_PATH);
        File[] list = directory.listFiles();

        Arrays.sort(list);
        for (File file : list) {
            out.println(file.getName());
        }
        out.println("~");
    }

}
