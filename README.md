# Chat TCP/UDP - Demonstração de Protocolos de Rede

Este projeto é uma aplicação **Spring Boot** desenvolvida para fins didáticos, demonstrando a implementação e as diferenças práticas entre os protocolos **TCP (via WebSockets)** e **UDP (Datagramas)**. A aplicação inclui um chat em tempo real, um sistema de ping/pong UDP com simulação de perda de pacotes e um assistente de IA integrado.

---

## 🚀 Funcionalidades

- **Comunicação TCP (WebSockets):** Chat multiusuário com suporte a mensagens globais, mensagens diretas (DM) e gerenciamento de presença (quem está online).
- **Comunicação UDP:** Servidor e cliente UDP para testes de latência (ping/pong).
- **Simulação de Rede Real:** O servidor UDP simula uma perda de pacotes de 30% para demonstrar a natureza não confiável do protocolo.
- **Integração com IA (LokiBot):** Um chatbot integrado via Ollama que responde dúvidas técnicas sobre redes e sobre o projeto.

---

## 🏗️ Estrutura e Explicação do Código

A aplicação está organizada em pacotes que separam as responsabilidades de cada protocolo e serviço:

### 1. Núcleo TCP: `br.loki.chat.chat_tcp_udp.handler.ChatHandler`
Este é o componente principal para o chat. Ele gerencia as conexões persistentes do WebSocket.
- **Gerenciamento de Estado:** Utiliza `ConcurrentHashMap` para mapear usuários a sessões, garantindo que as mensagens cheguem ao destino correto.
- **Protocolo de Mensagens:** As mensagens trafegam em JSON. O `handleTextMessage` decide o que fazer com base no campo `type` (JOIN, GLOBAL_MSG, DM, BOT).
- **Confiabilidade:** Como utiliza TCP, o sistema garante que a ordem das mensagens seja mantida e que nenhuma mensagem seja perdida durante a transmissão.

### 2. Núcleo UDP: `br.loki.chat.chat_tcp_udp.udp.UdpServer`
Um servidor que roda em segundo plano (background thread) escutando a porta `9000`.
- **Natureza Connectionless:** Diferente do WebSocket, o servidor UDP não "sabe" quem está conectado; ele apenas recebe pacotes individuais e responde para o endereço de origem.
- **Simulação de Perda:** 
  ```java
  if (Math.random() < 0.30) {
      // Simula a perda de pacotes comum em redes UDP instáveis
      continue; 
  }
  ```

### 3. Cliente UDP: `br.loki.chat.chat_tcp_udp.controller.UdpController`
Expõe um endpoint HTTP (`/udp/ping`) que atua como um cliente UDP. Ele envia um "PING", espera um "PONG" e calcula o **RTT (Round Trip Time)**. Se o pacote for perdido pelo servidor, o controlador retorna um erro de timeout.

### 4. Inteligência Artificial: `br.loki.chat.chat_tcp_udp.ai.LokiAiService`
Integração com o modelo **Llama 3.2** (via Ollama).
- **Didática:** O serviço utiliza um "System Prompt" para garantir que a IA responda sempre de forma educativa sobre os protocolos de rede.
- **Fallback:** Possui respostas pré-programadas caso o serviço de IA esteja offline, garantindo a funcionalidade básica do bot.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Finalidade |
| :--- | :--- |
| **Java 17** | Linguagem principal do projeto. |
| **Spring Boot 3** | Framework para o backend e servidor web. |
| **Spring WebSocket** | Implementação da comunicação TCP bidirecional. |
| **Ollama / Llama 3.2** | Motor de inteligência artificial local. |
| **Jackson** | Processamento de dados JSON. |
| **WebClient** | Comunicação reativa com a API da IA. |

---

## 🚦 Como Executar

### Pré-requisitos
- **JDK 17** ou superior instalado.
- **Maven** para gerenciar dependências.
- **Ollama** (opcional, necessário apenas para as funções de IA).

### Execução
1. Compile o projeto:
   ```bash
   mvn clean install
   ```
2. Inicie a aplicação:
   ```bash
   mvn spring-boot:run
   ```
3. A aplicação estará disponível em `http://localhost:8080`.

---

## 📝 Como Testar

1. **Testar TCP (Chat):** Utilize um cliente WebSocket (como o Postman ou uma interface frontend simples) conectando em `ws://localhost:8080/chat`.
   - Envie um JSON de JOIN: `{"type": "JOIN", "user": "SeuNome"}`.
2. **Testar UDP (Ping):** Acesse no seu navegador: `http://localhost:8080/udp/ping`.
   - Atualize a página várias vezes para observar os sucessos (RTT em ms) e as falhas (TIMEOUT).
3. **Testar em Dispositivos Móveis (Mesma Rede):**
   - Para testar a aplicação (tanto o chat quanto o ping UDP) em dispositivos móveis ou outros computadores na mesma rede local, você precisará do endereço IPv4 da máquina onde a aplicação está rodando.
   - No terminal do seu computador, execute `ipconfig` (Windows) ou `ifconfig`/`ip a` (Linux/macOS) para encontrar o seu endereço IPv4 (ex: `192.168.1.100`).
   - No navegador do dispositivo móvel, acesse: `http://<SEU_IPV4>:8080` para a interface web (se houver) ou `ws://<SEU_IPV4>:8080/chat` para o WebSocket e `http://<SEU_IPV4>:8080/udp/ping` para o teste UDP.
3. **Interagir com a IA:** No chat, envie uma mensagem do tipo BOT:

   ```json
   {"type": "BOT", "mode": "GLOBAL", "text": "Explique a diferença entre TCP e UDP"}
   ```

---

## ⚖️ Licença
Este projeto foi desenvolvido para fins estritamente educacionais, demonstrando conceitos fundamentais de redes de computadores e sistemas distribuídos.
