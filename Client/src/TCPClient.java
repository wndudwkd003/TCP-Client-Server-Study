import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;

public class TCPClient {
    private static boolean isClientRun = false;
    private static Socket clientSocket = null;
    public static void main(String[] args) throws Exception {
        String serverHostname = "127.0.0.1"; // 서버 IP 주소
        int serverPort = 12345; // 서버 포트번호

        // 서버에 연결

        Thread serverThread = new Thread(() -> {
            while (!isClientRun) {
                try {
                    if (clientSocket == null) {
                        System.out.println("Server finding...");
                        try {
                            clientSocket = new Socket(serverHostname, serverPort);
                            System.out.println("Connected to server.");
                            if (clientSocket.isConnected()) {
                                isClientRun = true;
                                InputStream in = clientSocket.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

                                OutputStream out = clientSocket.getOutputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));



                                Thread serverToClientThread = new Thread(() -> {
                                    // 서버로부터 응답 수신
                                    while (true) {
                                        try {
                                            String response = reader.readLine();
                                            if (response == null) {
                                                // 서버와의 연결이 끊어진 경우
                                                System.out.println("Disconnected from server.");
                                                isClientRun = false;
                                                clientSocket = null;
                                                break;
                                            }
                                            System.out.println("Response from server: " + response);
                                        } catch (IOException e) {
                                            System.out.println("Disconnected from server.");
                                            try {
                                                br.close();
                                                reader.close();
                                            } catch (IOException ex) {
                                                throw new RuntimeException(ex);
                                            }

                                            clientSocket = null;
                                            break;
                                        }
                                    }
                                });



                                Thread clientToServerThread = new Thread(() -> {
                                    // 서버로 메시지 전송
                                    while (true) {
                                        try {
                                            String input = br.readLine();
                                            if(input.equals("exit")) {
                                                isClientRun = false;
                                                break;
                                            }
                                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                                            writer.println(input);
                                        } catch (IOException e) {
                                            System.out.println("Disconnected from server.");
                                            break;
                                        }
                                    }
                                });

                                clientToServerThread.start();
                                serverToClientThread.start();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        serverThread.start();

    }
}
