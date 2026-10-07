# Produção de dinheiro: simulação visual

Refatoração do trabalho de **Weslei Ferreira Santos**, de abril de 2022, para a aula de produtor-consumidor da MATA58. A versão histórica, incluindo imagens e configuração antiga, está em `original/`.

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
6. **Monitor:** pause e escolha **Monitor (synchronized, wait, notifyAll)**. Repita os passos 2 e 3: o comportamento é o mesmo, mas agora quem espera aparece "dormindo em wait() no monitor", e não há nenhum semáforo. A caixa azul no topo explica o modo.
7. **Condição de corrida:** pause, escolha **SEM proteção (desliga o mutex)**, ponha o produtor em 10 e o consumidor em 3 e inicie. O alerta vermelho mostra quantas vezes produtor e consumidor estiveram juntos na região crítica e quantas atualizações do contador se perderam; o buffer ganha borda vermelha enquanto os dois estão lá dentro. Pausando, o contador fica diferente das notas nas posições (até "vagas livres" negativas).

### Aba Mailbox (exemplo do professor)

A segunda aba mostra as Mailbox3b e Mailbox3s das práticas 2 e 3. Dave ("Hello, world.", em azul) e Bill ("Hot dog!", em laranja) escrevem letra por letra na mesma caixa de 72 posições; o consumidor lê a caixa inteira. Cada letra aparece na cor de quem a escreveu, então uma mensagem misturada tem as duas cores.

| Versão da caixa | Equivale a | O que aparece |
|---|---|---|
| Sem sincronização | `Mailbox3b` | Mensagens MISTURADAS ou CORTADAS, alerta vermelho de condição de corrida, "How sad" |
| Monitor do professor | `Mailbox3s` | Todas íntegras, mas alerta laranja de MENSAGENS PERDIDAS (sobrescritas antes de serem lidas) |
| Monitor completo | `CaixaDeCorreio` | Íntegras e nenhuma perdida; os produtores esperam a caixa esvaziar |
| Semáforos | caixa de uma posição: vaga, mensagem, mutex | Mesmo resultado do monitor completo |

Os controles imitam as constantes do professor: pausa entre letras (`MAXPROCESSTIME`), descanso dos produtores (`DELAY`) e intervalo do consumidor (`CHECKTIME`). As classes são `Mailbox` (os quatro modos), `MailboxSimulation` (as três threads) e `MailboxPane` (a aba); os testes estão em `MailboxTest`.

### Os três modos

| Modo | Como o buffer é protegido | Métodos do `BoundedBuffer` |
|---|---|---|
| Semáforos (padrão) | `empty`, `full` e `mutex`, como o `prod_cons.c` | `offer` / `poll` |
| Monitor | o próprio objeto: `synchronized`, `while (cheio) wait()`, `while (vazio) wait()`, `notifyAll()` | `offerMonitor` / `pollMonitor` |
| Sem proteção | `empty` e `full`, sem o `mutex` | `offerUnprotected` / `pollUnprotected` |

No modo monitor, a thread dorme de verdade dentro do `wait()` do buffer, com tempo limite de 100 ms para a simulação continuar respondendo a Pausar e Reiniciar. O modo só pode ser trocado com a simulação parada, e a troca reinicia o buffer.

### Como funciona o modo sem proteção

Os semáforos de vagas e itens continuam ligados; só o `mutex` é desligado. Para a corrida ficar visível em velocidade humana, cada operação sem proteção lê o contador `size`, espera 250 ms (`Simulation.RACE_WINDOW_MILLIS`) e só então escreve o novo valor: é o `contador++` em câmera lenta. Um contador atômico registra quantas threads estão na região crítica (dois ao mesmo tempo = corrida), e cada thread confere se `size` mudou durante a sua espera (se mudou, a escrita dela apaga a da outra). No modo protegido, a mesma instrumentação comprova zero encontros. O modo só pode ser trocado com a simulação pausada, e a troca reinicia o buffer.

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
