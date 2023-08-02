package test_dubug.src.main.java.org.apache.tomcat;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * @description:
 * @author: LiShuai
 * @createDate: 2023/7/25 23:25
 * @version: 1.0
 */

public class SocketTest {

    static class Server{
        public static void main(String[] args) throws Exception {
            ServerSocket serverSocket = new ServerSocket();
            ServerSocket serverSocket1 = new ServerSocket();
//                        System.out.println(serverSocket.getReuseAddress());
//        serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", 8088));
            Socket accept = serverSocket.accept();
//        int read = accept.getInputStream().read();
//        System.out.println(read);
//        accept.getOutputStream().write('c');
            serverSocket.close();
            System.out.println("close");
//        serverSocket1.setReuseAddress(true);
            serverSocket1.bind(new InetSocketAddress("127.0.0.1", 8088));
            System.out.println("accept");
////        serverSocket1.close();
//             Socket accept1 = serverSocket1.accept();
//        System.out.println("accept end");
//        System.out.println(accept1.getInputStream().read());

        }
    }

    static class Client{
        public static void main(String[] args) throws Exception {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 8088));
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);
//        int i = scanner.nextInt();
            System.out.println("write");
            int count = 0;
            while (true) {
                outputStream.write(1);
                outputStream.flush();
            }

//        outputStream.flush();
//        System.out.println("write end");
//        System.out.println(socket.getInputStream().read());
//        Thread.currentThread().join();
        }
    }
}
