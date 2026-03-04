package br.loki.chat.chat_tcp_udp.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class LokiAiService {

    private final WebClient client;

    public LokiAiService() {
        this.client = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String ask(String user, String prompt) {
        // Prompt “didático” para seu tema (TCP/UDP + cliente-servidor)
        String system = """
                Você é o LokiBot, assistente do projeto Loki Chat.
                Responda em português do Brasil, de forma didática e objetiva.
                Contexto: estamos demonstrando TCP (WebSocket) vs UDP (datagramas).
                Se a pergunta pedir, explique com exemplos práticos ligados ao projeto.
                Evite texto longo: 6 a 10 linhas no máximo, a menos que o usuário peça mais.
                """;

        String fullPrompt = "Usuário: " + user + "\nPergunta: " + prompt + "\n";

        // /api/generate (Ollama)
        Map<String, Object> body = Map.of(
                "model", "llama3.2:3b",
                "prompt", system + "\n" + fullPrompt,
                "stream", false
        );

        try {
            var resp = client.post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OllamaGenerateResponse.class)
                    .timeout(Duration.ofSeconds(8))
                    .block();

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                return fallback(prompt);
            }
            return resp.response().trim();
        } catch (Exception e) {
            return fallback(prompt);
        }
    }

    private String fallback(String prompt) {
        // fallback simples se IA não responder
        String p = prompt.toLowerCase();
        if (p.contains("tcp") && p.contains("udp")) {
            return "TCP: conexão, entrega e ordem garantidas. UDP: sem conexão, sem garantia, menor overhead. "
                 + "No seu projeto: chat via WebSocket (TCP) e ping/timeout no UDP.";
        }
        if (p.contains("tcp")) {
            return "TCP é orientado à conexão: mantém sessão, garante entrega e ordem. No seu chat via WebSocket, "
                 + "o servidor mantém sessões e retransmite mensagens com confiabilidade.";
        }
        if (p.contains("udp")) {
            return "UDP é sem conexão: envia datagramas, pode perder pacotes e não garante ordem. No seu ping UDP, "
                 + "o timeout mostra perda/ausência de resposta.";
        }
        return "Estou sem acesso ao modelo de IA agora. Tente perguntar: '@loki explique tcp' ou '@loki explique udp'.";
    }

    public record OllamaGenerateResponse(String response) {}
}