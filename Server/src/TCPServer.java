import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServer {

    public static void main(String[] args) throws Exception {
        int serverPort = 12345; // 서버 포트번호

        // 서버 소켓 생성
        ServerSocket serverSocket = new ServerSocket(serverPort);
        System.out.println("TCP Server started.");

        // 클라이언트 연결 대기
        System.out.println("Waiting for clients...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

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

        OutputStream out = clientSocket.getOutputStream();
        Thread serverToClientThread = new Thread(() -> {
            // 클라이언트로 메시지 전송
            while (true) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                    String input = br.readLine();

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

                    writer.println(input);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clientToServerThread.start();
        serverToClientThread.start();
    }
}
