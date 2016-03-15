import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by SHIVAM on 3/12/2016.
 */
public class FtpClient {

    public static int fileSize = 0;
    public static boolean fileExist = false;

    // Variable to store Server's IP address
    public static String serverIP;

    // variables to store port numbers for commands and data
    public static int cmdPort;
    public static int dataPort;


    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);

        // Take server's IP address
        System.out.print("Enter FTP server's IP address: ");
        serverIP = in.nextLine();
        // Take port number for commands
        System.out.print("Enter port number for commands: ");
        cmdPort = Integer.parseInt(in.nextLine());
        // Take port number for data
        System.out.print("Enter port number for data: ");
        dataPort = Integer.parseInt(in.nextLine());

        try (Socket cmdSocket = new Socket(serverIP, cmdPort);
             BufferedReader cmdIn = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()));
             PrintWriter cmdOut = new PrintWriter(cmdSocket.getOutputStream(), true);
        ) {

            String userInput;
            showResponse(cmdIn);

            while (true) {

                System.out.print("ftp->");
                userInput = in.nextLine();
                cmdOut.println(userInput);

                if (userInput.equals("EXIT")) {
                    showResponse(cmdIn);
                    cmdSocket.close();
                    System.exit(0);
                } else if (userInput.startsWith("GET ")) {
                    Socket dataSocket = null;
                    InputStream dataIn = null;
                    try {
                        dataSocket = new Socket(serverIP, dataPort);
                        dataIn = dataSocket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String fileName = userInput.substring(4, userInput.length());
                    showResponse(cmdIn);

                    if (fileExist) {
                        System.out.print("Enter the directory where you want to download the file: ");
                        String directory = in.nextLine();
                        receiveFile(directory, fileName, dataIn);
                    }
                    dataSocket.close();
                } else if (userInput.equals("LIST")) {
                    showResponse(cmdIn);
                } else {
                    System.out.println("Unknown Command.");
                }

            }

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverIP);
            System.exit(1);
        }
    }

    private static void receiveFile(String directory, String fileName, InputStream dataIn) {


        int bytesRead = 0;
        int count;

        OutputStream os = null;


        try {

            byte[] buffer = new byte[16 * 1024];

            os = new FileOutputStream(directory + "\\" + fileName);


//            bytesRead = dataIn.read(buffer, 0, buffer.length);
//
//            current = bytesRead;
//
//            bos.write(buffer, 0, current);
//            bos.flush();

            while ((count = dataIn.read(buffer)) > 0) {

                os.write(buffer, 0, count);
            }

            System.out.println("File download complete!!!");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {
                dataIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (os != null) try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileExist = false;
        }

    }

    private static void showResponse(BufferedReader cmdIn) {
        String inputStream;

        try {
            while ((inputStream = cmdIn.readLine()) != null) {

//                if (inputStream.startsWith("~FileSize:")) {
//                    fileSize = Integer.parseInt(inputStream.substring(10, inputStream.length()));
//                    fileExist = true;
//                    break;
//                }

                if (inputStream.equals("EXIST")) {
                    fileExist = true;
                    break;
                }

                if (inputStream.equals("~"))
                    break;

                System.out.println(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
