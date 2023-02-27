import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        String serverHostname = "127.0.0.1"; // 서버 IP 주소
        int serverPort = 12345; // 서버 포트번호

        // 서버에 연결
        Socket clientSocket = new Socket(serverHostname, serverPort);

        InputStream in = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        Thread serverToClientThread = new Thread(() -> {
            // 서버로부터 응답 수신
            while (true) {
                try {
                    String response = reader.readLine();
                    if (response == null) {
                        // 서버와의 연결이 끊어진 경우
                        System.out.println("Disconnected from server.");
                        break;
                    }
                    System.out.println("Response from server: " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        OutputStream out = clientSocket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Thread clientToServerThread = new Thread(() -> {
            // 서버로 메시지 전송
            while (true) {
                try {
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
