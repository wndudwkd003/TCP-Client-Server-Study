import java.io.*;
import java.net.*;

import static java.lang.Thread.sleep;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        String serverHostname = "127.0.0.1"; // 서버 IP 주소
        int serverPort = 12345; // 서버 포트번호

        // 서버에 연결
        Socket clientSocket = new Socket(serverHostname, serverPort);

        Thread clientToServerThread = new Thread(() -> {
            while (true) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String line = br.readLine();

                    // 서버로 메시지 전송
                    OutputStream out = clientSocket.getOutputStream();
                    PrintWriter writer = new PrintWriter(out);

                    writer.println(line);
                    writer.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        clientToServerThread.start();

        Thread serverToClient = new Thread(() -> {
            while (true) {
                try {
                    // 서버로부터 응답 수신
                    InputStream in = clientSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    if (!reader.readLine().equals("")) {
                        String response = reader.readLine();
                        System.out.println("Response from server: " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverToClient.start();

    }
}
