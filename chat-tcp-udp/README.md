Loki Chat — Cliente-Servidor com TCP, UDP e IA Local

Aplicação de chat em tempo real desenvolvida para demonstrar na prática o funcionamento de uma arquitetura cliente-servidor e o comportamento dos protocolos TCP e UDP.

O sistema utiliza Spring Boot como servidor, HTML/JavaScript como cliente, WebSocket para comunicação TCP, UDP para testes de datagramas, e integra uma IA local (Ollama) chamada LokiBot.

Objetivo do Projeto

Este projeto foi criado com objetivo educacional para demonstrar:

Arquitetura cliente-servidor

Comunicação em tempo real usando WebSocket (TCP)

Comunicação baseada em datagramas usando UDP

Troca de dados estruturados utilizando JSON

Integração de IA local com Ollama

Arquitetura do Sistema
Cliente (Browser)
      │
HTML + JavaScript
      │
WebSocket / HTTP
      │
Spring Boot Server
      │
 ├── Chat Handler (TCP / WebSocket)
 ├── UDP Server
 └── Loki AI Service
         │
      Ollama (IA Local)

O navegador atua como cliente, enquanto o Spring Boot funciona como servidor central, gerenciando conexões e mensagens.

Tecnologias Utilizadas
Backend

Java

Spring Boot

WebSocket

UDP Sockets

WebClient

Frontend

HTML

CSS

JavaScript

Inteligência Artificial

Ollama

Modelo LLM local

Funcionamento da Aplicação
Conexão Cliente-Servidor

Quando o usuário abre o chat no navegador:

http://localhost:8080

O servidor Spring Boot envia a interface HTML.

Depois disso o cliente abre uma conexão WebSocket:

socket = new WebSocket("ws://" + location.host + "/chat");

Isso cria uma conexão persistente entre cliente e servidor.

Comunicação via JSON

As mensagens trocadas entre cliente e servidor são estruturadas em JSON.

Exemplo de usuário entrando no chat:

{
 "type": "JOIN",
 "user": "Sara"
}

Exemplo de mensagem enviada:

{
 "type": "GLOBAL_MSG",
 "user": "Sara",
 "text": "Olá pessoal"
}

O servidor lê o JSON e decide como processar a mensagem.

Chat em Tempo Real (TCP)

O chat utiliza WebSocket, que funciona sobre TCP.

Características do TCP demonstradas no projeto:

conexão persistente

garantia de entrega

ordenação de mensagens

comunicação bidirecional

Fluxo de comunicação:

Cliente ⇄ Servidor

Isso permite que mensagens sejam enviadas em tempo real entre usuários.

Teste de UDP

A aplicação também implementa um teste de comunicação via UDP.

UDP funciona de forma diferente do TCP:

não estabelece conexão

não garante entrega

não garante ordem dos pacotes

Ao clicar no botão Testar UDP, o sistema envia um datagrama para o servidor.

O resultado pode ser:

UDP: sucesso
UDP: timeout
UDP: pacote perdido

Isso demonstra o comportamento não confiável do UDP.

Integração com IA (LokiBot)

O sistema possui um assistente chamado LokiBot.

Ele responde perguntas dentro do chat utilizando um modelo de IA executado localmente com Ollama.

Exemplo de pergunta:

@loki explique TCP

O backend envia a pergunta para o Ollama:

{
 "model": "llama3.2:3b",
 "prompt": "Explique TCP",
 "stream": false
}

A resposta é retornada para o chat.

Estrutura do Projeto
src
 ├── main
 │   ├── java
 │   │   ├── config
 │   │   │   └── WebSocketConfig.java
 │   │   ├── handler
 │   │   │   └── ChatHandler.java
 │   │   ├── udp
 │   │   │   ├── UdpServer.java
 │   │   │   └── UdpController.java
 │   │   └── ai
 │   │       └── LokiAiService.java
 │   │
 │   └── resources
 │       └── static
 │           └── index.html
Explicação dos Principais Arquivos
WebSocketConfig.java

Responsável por configurar o endpoint WebSocket.

Exemplo:

registry.addHandler(chatHandler, "/chat");

Isso permite que clientes conectem usando:

ws://localhost:8080/chat
ChatHandler.java

Gerencia a comunicação entre clientes conectados.

Funções principais:

registrar usuários

receber mensagens

enviar mensagens para todos

enviar mensagens privadas

UdpServer.java

Implementa um servidor UDP que recebe datagramas.

Ele demonstra que UDP pode:

perder pacotes

não garantir entrega

não manter conexão

UdpController.java

Expõe um endpoint HTTP para testar UDP.

Quando o cliente clica no botão Testar UDP, o servidor envia um datagrama UDP.

LokiAiService.java

Responsável por enviar perguntas para a IA.

Ele utiliza WebClient para chamar a API do Ollama:

http://localhost:11434/api/generate
Como Executar o Projeto
1. Clonar o repositório
git clone https://github.com/seu-usuario/loki-chat.git
cd loki-chat
2. Executar o servidor

No terminal:

./mvnw spring-boot:run

Servidor disponível em:

http://localhost:8080
Como Ativar a IA

Instale o Ollama:

https://ollama.com

Baixe um modelo:

ollama pull llama3.2:3b

Execute:

ollama run llama3.2:3b

A aplicação se conecta automaticamente à IA.

Comparação TCP vs UDP Demonstrada
Protocolo	Uso no Projeto	Características
TCP	Chat WebSocket	confiável, ordenado
UDP	Teste de ping	rápido, sem garantia
Autor

Sara
Estudante de Engenharia de Software