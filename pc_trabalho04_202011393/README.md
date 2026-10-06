# Produção de dinheiro — simulação visual

Refatoração do trabalho de **Weslei Ferreira Santos**, de abril de 2022, para a aula de produtor–consumidor da MATA58. A versão histórica, incluindo imagens e configuração antiga, está em `original/`.

## Executar

Requisitos: **JDK 25 ou superior**, Maven e ambiente gráfico. JavaFX é resolvido pelo Maven, sem caminhos absolutos de bibliotecas.

```bash
cd pc_trabalho04_202011393
mvn test
mvn javafx:run
```

Da raiz do repositório, `make test-simulation` testa apenas o modelo, sem JavaFX. `make run-simulation` inicia a janela via Maven.

## Para mostrar na aula

1. Clique em **Iniciar** e acompanhe os índices W (escrita) e R (leitura).
2. Ajuste produtor para 10 itens/s e consumidor para 0,2: o buffer enche e o produtor aguarda vaga.
3. Inverta as velocidades: o buffer esvazia e o consumidor aguarda item.
4. Clique em **Pausar**: o estado fica preservado. **Continuar** retoma; **Reiniciar** zera o buffer e deixa a simulação pronta.
5. Relacione vagas com `empty`, itens com `full` e acesso exclusivo com `mutex` do exemplo C.

As notas recebem números sequenciais. A posição visual corresponde ao índice físico do buffer; o consumo segue FIFO mesmo após os índices darem a volta. Os controles de velocidade determinam o intervalo após cada operação; uma alteração passa a valer no próximo intervalo. O histórico guarda os 12 eventos mais recentes.

## Arquitetura

- `BoundedBuffer`: semáforos de vagas, itens e mutex, índices circulares e snapshot imutável. Devolve reservas se uma aquisição for interrompida.
- `Simulation`: duas threads, pausa/reinício cooperativos, velocidades e estados protegidos. Não usa suspend/resume; fecha e interrompe workers ao sair.
- `MoneyApp`: interface JavaFX com cartões, indicadores e atualização por snapshots a cada 100 ms na thread gráfica. Workers nunca modificam a tela.
- `SimulationTest`: capacidade, FIFO, execução concorrente, interrupção, buffer cheio/vazio, pausa, reset e encerramento.

A simulação coordena seu ciclo de vida com um monitor adicional. Para exibir um estado estável e pausar imediatamente, as mutações no buffer são serializadas nesse monitor; a classe BoundedBuffer também é testada independentemente com chamadas concorrentes. Os indicadores mostram itens/vagas no buffer, não uma leitura ao vivo dos contadores dos semáforos durante reservas transitórias.

A interface foi reconstruída com componentes nativos JavaFX; os recursos gráficos do trabalho antigo permanecem no original. Não inclui áudio nem seleção de múltiplos consumidores: a demonstração tem um produtor e um consumidor.

Referências: [JavaFX com Maven](https://openjfx.io/openjfx-docs/) e [Semaphore](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Semaphore.html).

## Compatibilidade

Use JDK 25 ou superior para executar `mvn test` e `mvn javafx:run`. O projeto é compilado com APIs e bytecode Java 25.
