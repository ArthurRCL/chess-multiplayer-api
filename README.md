---

<div align="center">

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![WebSocket](https://img.shields.io/badge/websocket-black.svg?style=for-the-badge&logo=socket.io&logoColor=white)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge)

*Um servidor backend robusto e em tempo real para gerenciamento de partidas de xadrez multiplayer.*

</div>

---

Este projeto é a API e servidor de WebSocket responsáveis pela lógica de jogo e comunicação em tempo real do **Chess Multiplayer**. Construído com **Kotlin** e **Spring Boot**, ele gerencia o estado das partidas, controle de tempo (relógio), histórico de movimentos e validação do lado do servidor.

O objetivo é fornecer uma arquitetura de comunicação bidirecional rápida e confiável, garantindo que os jogadores recebam atualizações do tabuleiro (FEN) e eventos da partida de forma instantânea.

---

## Diferenciais Arquiteturais

A arquitetura foi pensada para escalar a comunicação em tempo real e isolar a complexidade das regras do jogo:

| Componente | Responsabilidade | Aplicação no Projeto |
| :--- | :--- | :--- |
| **WebSocket** | Comunicação full-duplex | Configuração centralizada para envio e recebimento de movimentos de xadrez e eventos em tempo real (`PartidaWebSocketController`). |
| **Serviço de Relógio** | Controle de Tempo Assíncrono | Um `RelogioService` e `PartidaTimeoutScheduler` gerenciam os modos de tempo e disparam eventos quando o tempo de um jogador esgota. |
| **DTOs (Data Transfer Objects)** | Desacoplamento de dados | Isolamento completo entre a camada de persistência (entidades) e as mensagens trafegadas no WebSocket. |
| **Gestão de Estado** | Consistência da Partida | A classe `Partida` mantém o estado atualizado (FEN, status, turno) evitando concorrência indesejada. |

---

## 🛠 Tecnologias Utilizadas
<div align="left">
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-Build_Tool-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSockets-Real_Time-black?style=for-the-badge" />
</div>

---

### Pré-requisitos
* **Java JDK 17+**
* **Gradle** (ou utilize o `gradlew` embutido)

### Configuração
1. Clone o repositório.
2. Importe o projeto na sua IDE (IntelliJ IDEA recomendado).
3. Execute o comando para iniciar a aplicação:
```bash
./gradlew bootRun
```
4. O servidor WebSocket estará disponível na porta configurada (padrão: `8080`).
