package br.loki.chat.chat_tcp_udp.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UdpServer {

    private static final int UDP_PORT = 9000;

    @PostConstruct
    public void start() {
        Thread t = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                System.out.println("UDP: Servidor iniciado na porta " + UDP_PORT);

                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    System.out.println("UDP: Recebido '" + msg + "' de " + packet.getAddress() + ":" + packet.getPort());

                    // Simula perda de pacote (30%)
                    if (Math.random() < 0.30) {
                        System.out.println("UDP: **SIMULANDO PERDA** (não respondi)");
                        continue;
                    }

                    String resp = "PONG";
                    byte[] respBytes = resp.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket respPacket = new DatagramPacket(
                            respBytes, respBytes.length,
                            packet.getAddress(), packet.getPort()
                    );
                    socket.send(respPacket);
                }
            } catch (Exception e) {
                System.out.println("UDP: Erro no servidor UDP");
                e.printStackTrace();
            }
        });

        t.setName("udp-server");
        t.setDaemon(true);
        t.start();
    }
}