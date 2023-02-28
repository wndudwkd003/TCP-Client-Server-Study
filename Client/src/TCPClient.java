import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class TCPClient {
    private static boolean isRunning;
    private static Socket clientSocket = null;
    private static final int SERVER_PORT = 12345;
    public static void main(String[] args) {
        String serverHostname = "127.0.0.1"; // 서버 IP 주소
        isRunning = true;
        // 서버에 연결하는 쓰레드
        // 서버에 연결 중인 클라이언트는 한 번만 연결하면 해당 쓰레드는 필요 없지만
        // 서버가 끊기고 다시 서버에 자동을 접속하려면 루프가 필요하다
        Thread serverThread = new Thread(() -> {
            // 서로 다른 쓰레드에서 동작한다
            ServerToClientThread serverToClientThread = null;    // 서버에서 클라이언트로 메시지를 받는 쓰레드
            ClientToServerThread clientToServerThread = null;    // 클라이언트에서 서버로 메시지를 보내는 쓰레드

            while (isRunning) {
                if (clientSocket == null) {
                    try {
                        System.out.println("Server finding...");
                        clientSocket = new Socket(serverHostname, SERVER_PORT);
                        if (clientSocket.isConnected()) {
                            System.out.println("Connected to server.");
                            // 서버와 접속하면 소켓을 사용하는 쓰레드 생성
                            serverToClientThread = new ServerToClientThread();
                            serverToClientThread.start();
                            clientToServerThread = new ClientToServerThread();
                            clientToServerThread.start();
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to connect to server. Retrying...");
                        // 서버와 연결이 끊기면 현재 접속중인 소켓을 사용중인 쓰레드를 전부 종료한다
                        if (serverToClientThread != null) {
                            serverToClientThread.setFlag(false);
                        }
                        if (clientToServerThread!= null) {
                            clientToServerThread.setFlag(false);
                        }
                    }
                }
                try {
                    // 재접속은 1초 간격으로 한다
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        });
        serverThread.start();

    }

    static class ClientToServerThread extends Thread {
        private boolean flag = true;
        private final OutputStream out;
        private final BufferedReader br;

        ClientToServerThread() throws IOException {
            out = clientSocket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            super.run();
            // 서버로 메시지 전송
            while (flag) {
                try {
                    // 서버로 보낼 메시지를 받는 코드
                    // 애플리케이션 자체를 종료하려면 해당 명령어 입력
                    String input = br.readLine();
                    if(input.equals("exit")) {
                        System.out.println("Client Off... Goodbye!");
                        clientSocket = null;
                        isRunning = false;
                        break;
                    }
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                    writer.println(input);
                } catch (IOException e) {
                    System.out.println("Failed to send message.");
                }
            }
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }

    static class ServerToClientThread extends Thread {
        private boolean flag = true;
        private final BufferedReader reader;

        public ServerToClientThread() throws IOException {
            InputStream in = clientSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            super.run();
            // 서버로부터 응답 수신
            while (flag) {
                try {
                    String response = reader.readLine();
                    System.out.println("Response from server: " + response);
                } catch (IOException e) {
                    // 서버와 연결이 끊기면 현재 사용중인 소켓은 null 으로 변경하여
                    // 서버 접속 쓰레드에서 재접속 하도록 유도
                    System.out.println("Disconnected from server.");
                    clientSocket = null;
                    break;
                }
            }
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }


}
