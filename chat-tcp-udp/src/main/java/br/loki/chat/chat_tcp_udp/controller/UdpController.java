package br.loki.chat.chat_tcp_udp.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UdpController {

    @GetMapping("/udp/ping")
    public String udpPing() {
        int udpPort = 9000;

        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(800); // tempo máximo esperando resposta (ms)

            InetAddress host = InetAddress.getByName("127.0.0.1");

            byte[] ping = "PING".getBytes(StandardCharsets.UTF_8);
            DatagramPacket pingPacket = new DatagramPacket(ping, ping.length, host, udpPort);

            long t0 = System.currentTimeMillis();
            client.send(pingPacket);

            byte[] buf = new byte[1024];
            DatagramPacket respPacket = new DatagramPacket(buf, buf.length);

            client.receive(respPacket);
            long t1 = System.currentTimeMillis();

            String resp = new String(respPacket.getData(), 0, respPacket.getLength(), StandardCharsets.UTF_8);
            return "UDP OK: " + resp + " | RTT=" + (t1 - t0) + "ms";
        } catch (SocketTimeoutException e) {
            return "UDP TIMEOUT: pacote possivelmente perdido (sem resposta)";
        } catch (Exception e) {
            return "UDP ERRO: " + e.getMessage();
        }
    }
}