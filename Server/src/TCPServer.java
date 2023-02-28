import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static java.lang.System.exit;

public class TCPServer {
    private static final int SERVER_PORT = 12345; // 서버 포트번호
    private static final int SERVER_SPACE = 50; // 서버에서 가용할 수 있는 클라이언트 수 (임의 변경)
    private static final Socket[] clientSockets = new Socket[SERVER_SPACE];    // 클라이언트 소켓 배열
    private static ServerSocket serverSocket;   // 서버 소켓

    public static void main(String[] args) throws Exception {

        // 서버 소켓 생성
        serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("TCP Server started.");

        // 서버 오픈
        // 서버는 항상 루프가 돌고 있어야 하기 때문에 쓰레드로 구현
        // 나머지도 마찬가지
        Thread serverAcceptThread = new Thread(() -> {
            int i = 0;
            while (true) {
                try {
                    // 클라이언트 대기
                    System.out.println("Waiting for clients...");
                    // 클라이언트가 접속할때가지 아래 코드에서 기다린다
                    // 클라이언트가 접속하지 않으면 코드가 더이상 진행되지 않는다
                    clientSockets[i] = serverSocket.accept();
                    System.out.println("Client connected: " + clientSockets[i].getInetAddress().getHostAddress());

                    if (clientSockets[i] != null && clientSockets[i].isConnected()) {
                        // 클라이언트에서 서버로 메시지를 전송받는 클래스 생성
                        ClientToServerThread clientHandler = new ClientToServerThread(i);
                        clientHandler.start();
                    }

                    // 클라이언트 최대 수 만큼 인덱스가 자동 증가하고 넘어가는 숫자는 0부터 시작
                    // 이렇게 하면 클라이언트가 서버의 남는 소켓이 없도록 차곡차곡 접속할 수 있다
                    i = (i + 1) % clientSockets.length;
                } catch (IOException e) {
                    serverSocket = null;
                    break;
                }
            }

        });
        serverAcceptThread.start();

        // 서버에서 클라이언트로 메시지를 보내는 클래스 생성
        ServerToClientThread serverToClientThread = new ServerToClientThread();
        serverToClientThread.start();
    }

    static class ServerToClientThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while(true) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                    String input = br.readLine();

                    // 서버를 종료하고 싶을 때
                    if(input.equals("exit")) {
                        System.out.println("Server off... Goodbye!");
                        for (Socket socket : clientSockets) {
                            if (socket!=null) {
                                socket.close();
                            }
                        }
                        serverSocket.close();
                        break;
                    }

                    // 소켓 배열의 접속되어 있는 클라이언트를 찾아서 차례대로 전송한다
                    for (Socket clientSocket : clientSockets) {
                        if (clientSocket!= null && clientSocket.isConnected()) {
                            OutputStream out = clientSocket.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                            writer.println(input);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientToServerThread extends Thread {
        private final int index;
        private final InputStream in;
        private final BufferedReader reader;

        ClientToServerThread(int index) throws IOException {
            this.index = index;
            this.in = clientSockets[index].getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                // 클라이언트로부터 메시지 수신
                try {
                    String message = reader.readLine();
                    System.out.println("Message from client: " + message);
                } catch (IOException e) {
                    System.out.printf("Client[%d] disconnected.\n", index);
                    clientSockets[index] = null;
                    break;
                }
            }
        }
    }
}
