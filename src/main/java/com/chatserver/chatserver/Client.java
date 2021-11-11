package com.chatserver.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {//클라이언트와 통신을 하기 위해 필요한 기능 정의
    Socket socket;//소켓 통신을 위한 라이브러리

    public Client(Socket socket){
        this.socket=socket;
        receive();
    }
    public void receive(){//클라이언트로부터 메시지를 전달 받는 메소드
        Runnable thread= () -> {
            try{
                while (true){
                    InputStream in = socket.getInputStream();
                    byte[] buffer = new byte[512];
                    int length = in.read(buffer);
                    while(length==-1) throw new IOException();
                    System.out.println("[메시지 수신 성공]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
                    String message = new String(buffer, 0, length, "UTF-8");
                    for(Client client : Main.clients){
                        client.send(message);
                    }
                }
            }catch(Exception e){
                try{
                    System.out.print("[메시지 수신 오류]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
                } catch (Exception e2){
                e2.printStackTrace();
                }
            }
        };
        Main.threadPool.submit(thread);
    }
    public void send(String message) {//클라이언트로 메시지를 전달하는 메소드
        try {
            OutputStream out = socket.getOutputStream();
            byte[] buffer = message.getBytes("UTF-8");
            out.write(buffer);//버퍼에 담긴 내용을 클라이언트로 전송
            out.flush();
        } catch (Exception e) {
            try {
                System.out.println("[메시지 송신 오류]" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
                Main.clients.remove(Client.this);//오류 발생시 오류 발생한 클라이언트를 서버에서 제거
                socket.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    };
}
