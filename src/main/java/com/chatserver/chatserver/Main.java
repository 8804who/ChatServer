package com.chatserver.chatserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    public static Scanner scanner=new Scanner(System.in);
    public static ExecutorService threadPool;//스레드를 관리하는 라이브러리
    public static Vector<Client> clients = new Vector<Client>();

    ServerSocket serverSocket;
    
    public void startServer(String IP, int port){//서버를 구동시키는 메소드
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP, port));
        } catch (IOException e) {
            e.printStackTrace();
            if(!serverSocket.isClosed()){
                stopServer();
            }
            return;
        }
        //클라이언트가 접속할 때까지 기다리는 쓰레드
        Runnable thread = () -> {
            while(true){
                try{
                    Socket socket = serverSocket.accept();
                    clients.add(new Client(socket));
                    System.out.println("[클라이언트 접속] "+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
                } catch (IOException e) {
                    if(!serverSocket.isClosed()){
                        stopServer();
                    }
                    break;
                }
            }
        };
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(thread);
    }

    private void stopServer() {
        try{//현재 작동중인 모든 소켓 닫기
            Iterator<Client> iterator = clients.iterator();
            while(iterator.hasNext()){
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            if(serverSocket != null && !serverSocket.isClosed()){//서버 소켓 객체 닫기
                serverSocket.close();
            }
            if(threadPool != null && !threadPool.isShutdown()){//쓰레드 풀 종료하기
                threadPool.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(Stage primaryStage) throws IOException {//프로그램 진입점
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕",15));
        root.setCenter(textArea);

        Button toggleButton = new Button("시작하기");
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
        root.setBottom(toggleButton);

        //String IP = "127.0.0.1";
        //int port = 9876;
        System.out.print("IP와 port 번호를 입력하세요\n");
        String IP=scanner.nextLine();
        int port=scanner.nextInt();
        toggleButton.setOnAction(event->{
            if(toggleButton.getText().equals("시작하기")){
                startServer(IP,port);
                System.out.println("IP 주소는 "+IP+"입니다.");
                System.out.println("port 주소는 "+port+"입니다.");
                Platform.runLater(()->{
                    String message = String.format("[서버 시작]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("종료하기");
                });
            } else{
                stopServer();
                Platform.runLater(()->{
                    String message = String.format("[서버 종료]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("시작하기");
                });
            }
        });
        Scene scene = new Scene(root,400,400);
        primaryStage.setTitle("[ 채팅 서버 ]");
        primaryStage.setOnCloseRequest(event->stopServer());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}