import java.io.*;
import java.net.*;

public class TCPServer {

    public static void main(String[] args) throws Exception {
        int serverPort = 12345; // 서버 포트번호

        // 서버 소켓 생성
        ServerSocket serverSocket = new ServerSocket(serverPort);
        System.out.println("TCP Server started.");

        boolean flag = true;

        while (true) {
            // 클라이언트 연결 대기
            System.out.println("Waiting for clients...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            Thread clientToServerThread = new Thread(() -> {
                // 클라이언트로부터 메시지 수신
                while(true) {
                    try {
                        InputStream in = clientSocket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String message = reader.readLine();
                        System.out.println("Message from client: " + message);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            Thread serverToClientThread = new Thread(() -> {
                while (true) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String input = br.readLine();

                        OutputStream out = clientSocket.getOutputStream();
                        PrintWriter writer = new PrintWriter(out);

                        writer.println(input);
                        writer.flush();

                        if(input.equals("bye")) {
                            // 클라이언트 소켓 종료
                            clientSocket.close();
                            System.out.println("Client disconnected.");
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });



            if(flag) {
                flag = false;
                clientToServerThread.start();
                serverToClientThread.start();
            }

        }



    }
}
