import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServer {

    private static final Socket[] clientSockets = new Socket[51];

    public static void main(String[] args) throws Exception {

        int serverPort = 12345; // 서버 포트번호

        // 서버 소켓 생성
        ServerSocket serverSocket = new ServerSocket(serverPort);
        System.out.println("TCP Server started.");

        Thread serverAcceptThread = new Thread(() -> {
            while (true) {
                for (int i = 0; i < clientSockets.length; i++) {
                    try {
                        // 클라이언트 대기
                        System.out.println("Waiting for clients...");
                        clientSockets[i] = serverSocket.accept();
                        System.out.println("Client connected: " + clientSockets[i].getInetAddress().getHostAddress());

                        Socket clientSocket = clientSockets[i];

                        if (clientSocket != null && clientSocket.isConnected()) {
                            InputStream in = clientSocket.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                            Thread clientToServerThread = new Thread(() -> {
                                // 클라이언트로부터 메시지 수신
                                while (true) {
                                    try {
                                        String message = reader.readLine();
                                        System.out.println("Message from client: " + message);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            clientToServerThread.start();
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        });
        serverAcceptThread.start();

        Thread serverInputThread = new Thread(() -> {
            try {
                while(true) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                    String input = br.readLine();

                    for (Socket clientSocket : clientSockets) {
                        if (clientSocket!= null && clientSocket.isConnected()) {
                            System.out.println("Sending message to client: " + input);
                            OutputStream out = clientSocket.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                            writer.println(input);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverInputThread.start();
    }
}
