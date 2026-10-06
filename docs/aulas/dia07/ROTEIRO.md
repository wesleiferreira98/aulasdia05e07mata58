# Aula de 7 de outubro de 2026: concorrência e sincronização

**MATA58 · 90 minutos · Parte 1: teoria (45 min) · Parte 2: prática (45 min)**

Pergunta que conduz a aula: **"Se as threads compartilham a memória, como impedir que uma atrapalhe a outra?"**

Ao final, cada aluno deve conseguir:

- explicar por que `contador++` em duas threads perde incrementos;
- definir região crítica e exclusão mútua;
- dizer o que `mutex`, `empty` e `full` controlam no produtor-consumidor e por que a ordem dos `sem_wait` importa;
- completar o produtor-consumidor com os semáforos certos;
- identificar a condição de corrida na Mailbox 3b e explicar como `synchronized`, `wait` e `notify` a resolvem na 3s, e por que se usa `while` antes de `wait`.

Esta aula segue o roteiro do material-base do professor (PDF *Aulas guiadas*, Aula 2), com dois acréscimos: uma prática em C com o produtor-consumidor e uma simulação visual curta, só para a turma **ver** os semáforos funcionando.

## Preparação do professor

Na raiz do repositório:

```bash
make test               # 12 testes, inclusive corrida, Peterson e produtor-consumidor
make java               # compila as mailboxes 3b e 3s
make run-simulation     # abre a janela da simulação; feche depois de conferir
```

**Ensaie a simulação no computador da sala.** Na primeira execução, o Maven baixa o JavaFX pela internet. Se o computador da sala não tiver rede, leve o seu notebook. Se não abrir de jeito nenhum, pule: são só 5 minutos e não há atividade sobre ela.

Confira se os computadores dos alunos têm `gcc` e `javac` (`gcc --version`, `javac -version`). As duas práticas dependem deles.

Deixe abertos: os slides, a janela da simulação (minimizada) e um terminal na raiz do repositório.

Slides: [aula07_concorrencia.pdf](slides/aula07_concorrencia.pdf) (fonte em [aula07_concorrencia.tex](slides/aula07_concorrencia.tex)). Apostila: [apostila07_concorrencia.pdf](apostila/apostila07_concorrencia.pdf). Indique **depois da aula**, porque ela traz o `prod_cons.c` completo, que é a solução das lacunas.

Distribua [ATIVIDADE.md](ATIVIDADE.md) sem o [gabarito](GABARITO.md). Comandos e saídas esperadas: [COLA_DEMONSTRACAO.md](COLA_DEMONSTRACAO.md).

**Prioridade se o tempo apertar:** na teoria, encurte Peterson; na prática, as lacunas e a Mailbox 3b vêm primeiro. A simulação é a primeira coisa a cortar.

## Agenda

| Minutos | Etapa | Material | Resultado esperado |
|---|---|---|---|
| **Parte 1** | **Teoria** | | |
| 0 a 5 | De processos a threads | Slides | Lembrar que processos não compartilham memória; threads sim |
| 5 a 12 | Condição de corrida | Slides | Explicar o incremento perdido |
| 12 a 20 | Região crítica, exclusão mútua, Peterson | Slides | Distinguir exclusão mútua de espera ocupada |
| 20 a 28 | Semáforos e atividade rápida N = 5 | Slides + quadro | `empty = 4`, `full = 1`; `empty + full = N` |
| 28 a 35 | Produtor-consumidor em C | Slides | Justificar a ordem dos `sem_wait`; construir o deadlock |
| 35 a 40 | Ver funcionando: simulação | Janela da simulação | Ver quem espera quando o buffer enche ou esvazia |
| 40 a 45 | Por que monitores? | Slides | Chave do objeto, `wait`, `notify` |
| **Parte 2** | **Prática** | | |
| 45 a 58 | Prática 1: seis lacunas em C | `prod_cons_atividade.c` | Programa termina com 40 itens |
| 58 a 68 | Prática 2: Mailbox 3b | `Monitor/1` | Ver mensagens misturadas; questões 3 a 5 |
| 68 a 80 | Prática 3: Mailbox 3s | `Monitor/3` | Comparar com a 3b; questão 6 |
| 80 a 90 | Atividade final e fechamento | Papel | Questões 7 a 10 |

---

# Parte 1: teoria (0 a 45 min)

Só slides e quadro. Os alunos não precisam do computador nesta parte.

## 0 a 5 min: de processos a threads

Abra com a pergunta do guia: **"Na aula anterior vimos que processos têm memória independente. E threads do mesmo processo?"**

Fala sugerida: "As **threads** de um mesmo processo dividem a memória: código, globais, heap e arquivos abertos. Cada uma tem só a sua pilha e os seus registradores. Comunicar ficou fácil. O difícil agora é não atrapalhar."

Desenhe um processo com duas pilhas e uma única área de globais e heap.

## 5 a 12 min: condição de corrida

Projete o slide da intercalação e construa a tabela no quadro, passo a passo, com contador começando em 0:

| Passo | Thread A | Thread B |
|---|---|---|
| 1 | lê 0 | |
| 2 | | lê 0 |
| 3 | calcula 1 | |
| 4 | | calcula 1 |
| 5 | escreve 1 | |
| 6 | | escreve 1 |

Resultado: 1, e não 2. Fala: "`contador++` não é uma operação só. É ler, somar e escrever, e a outra thread pode entrar no meio."

Mostre o slide com a saída real do `corrida.c`: cerca de 1 milhão em vez de 2 milhões, mudando a cada execução. Não precisa rodar.

Precisão para você: em C, duas threads acessando uma variável comum sem sincronização é *data race*, comportamento indefinido. Não diga que o resultado "só pode ser 1 ou 2".

## 12 a 20 min: região crítica, exclusão mútua e Peterson

Definições no quadro:

- **Região crítica:** trecho que acessa o recurso compartilhado.
- **Exclusão mútua:** no máximo uma thread por vez dentro da região crítica.

Projete o pseudocódigo de Peterson:

```text
interessado[eu] = verdadeiro
vez = outro
enquanto interessado[outro] e vez == outro: esperar
... região crítica ...
interessado[eu] = falso
```

Explique: se o outro não quer entrar, entro direto. Se os dois querem, quem escreveu `vez` por último cede.

Pergunta: **"O que a thread faz enquanto está presa no `enquanto`?"** Resposta: gira testando a condição e gasta CPU. Isso é **espera ocupada**.

O `peterson-code.c` revisado sempre dá 20000. Se perguntarem por que usa atomics: com variáveis comuns, o compilador e o processador podem reordenar as operações, e Peterson deixa de funcionar.

## 20 a 28 min: semáforos

Defina no quadro: semáforo é um **contador que nunca fica negativo**.

- `sem_wait` (down, P): se o contador for maior que 0, diminui 1 e segue; se for 0, a thread **dorme**.
- `sem_post` (up, V): soma 1 e acorda quem estiver dormindo.

Diferença para Peterson: quem espera **dorme**, não gasta CPU.

Os três semáforos do produtor-consumidor:

| Semáforo | Começa em | Controla |
|---|---|---|
| `mutex` | 1 | **Exclusão mútua:** só uma thread mexe no buffer |
| `empty` | N | **Disponibilidade:** vagas livres |
| `full` | 0 | **Disponibilidade:** itens prontos |

Pergunta do guia: **"Qual é a diferença entre `mutex` e `empty`/`full`?"** O `mutex` protege o acesso ao buffer; `empty` e `full` representam estados de disponibilidade.

**Atividade rápida (oral, 2 min):** com N = 5, `empty` começa em 5 e `full` em 0. Depois de dois itens produzidos e um consumido, quanto valem? Resposta: `empty = 4`, `full = 1`. Reforce que `empty + full = N` sempre que nenhuma operação está em andamento.

## 28 a 35 min: produtor-consumidor em C

Projete os laços do produtor e do consumidor do `prod_cons.c`. Leia os cinco passos de cada um:

```text
produtor:   espera vaga → pega mutex → insere → devolve mutex → avisa item
consumidor: espera item → pega mutex → retira → devolve mutex → avisa vaga
```

Regra para a turma: **cada thread espera o que consome e avisa o que cria.**

Construa o deadlock no quadro: o consumidor pega o mutex e depois espera `full` com o buffer vazio. O produtor precisa do mutex para inserir. Um espera o outro para sempre.

## 35 a 40 min: ver funcionando (simulação)

**Só demonstração.** Os alunos apenas olham; não há questão sobre a simulação.

Fala: "Antes de praticar, vamos ver isso acontecendo. O banco produz notas, o carro-forte consome, e o cofre tem 10 posições."

Abra a janela (`make run-simulation`) e faça dois movimentos:

1. **Produtor 10 itens/s, consumidor 0,2.** O buffer enche e aparece "Produtor: Aguardando vaga: buffer cheio". Diga: "Ele está dormindo em `sem_wait(&empty)`, e `empty` vale 0."
2. **Inverta as velocidades.** O buffer esvazia e aparece "Consumidor: Aguardando item: buffer vazio". Diga: "Agora é o consumidor, em `sem_wait(&full)`."

Feche: "O `mutex` garante que ninguém atrapalha. `empty` e `full` garantem que ninguém trabalha sem ter o que precisa."

Se a simulação não abrir, pule e siga para monitores.

## 40 a 45 min: por que monitores?

Fala de transição: "Semáforos funcionam, mas é fácil esquecer um, trocar a ordem ou proteger a coisa errada. O monitor junta os dados e a sincronização no mesmo objeto."

Use a analogia do guia: um banheiro com **uma chave única**. Quem tem a chave entra; os outros esperam. A chave é o lock do objeto.

Mostre o slide das ferramentas:

- `synchronized`: para entrar no método, a thread precisa da **chave** do objeto.
- `wait()`: **solta a chave** e dorme até ser avisada.
- `notify()`: acorda uma thread que espera. Ela ainda precisa **pegar a chave de novo**.
- `Thread.sleep()` **não** solta a chave.

Fala de saída: "Na prática vamos ver um objeto compartilhado sem isso, e depois com isso."

---

# Parte 2: prática (45 a 90 min)

Os alunos vão para o computador, em duplas, com a [atividade](ATIVIDADE.md) em mãos. Todos os comandos são executados **na raiz do repositório**.

## 45 a 58 min: Prática 1, seis lacunas em C

Distribua o esqueleto [prod_cons_atividade.c](../../../examples/dia07/prod_cons_atividade.c). As duplas completam os valores iniciais de `empty` e `full` e qual semáforo cada thread espera e avisa. O `mutex` já vem pronto.

Condução sugerida: 2 minutos para explicar, 8 para implementar e 3 para comparar.

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread
./prod-cons-aluno
```

Correto: termina com `Fim: 40 itens produzidos e consumidos; buffer vazio.`

Diagnóstico rápido quando der errado:

| Sintoma | Causa provável |
|---|---|
| Trava sem imprimir (Ctrl+C) | `empty` começando em 0, ou o produtor esperando `full` |
| `Assertion 'count < N' failed` | O produtor avisando `empty` em vez de `full` |
| `Assertion 'count > 0' failed` | `full` começando em N, valores trocados, ou o consumidor avisando `full` |

Antes de preencher, o esqueleto não compila. É proposital.

## 58 a 68 min: Prática 2, Mailbox 3b (sem sincronização)

Explique o exemplo antes de rodar: uma única `Mailbox3b` é compartilhada por **dois produtores** e **um consumidor**. Dave envia "Hello, world." e Bill envia "Hot dog!". O próprio comentário do código avisa que essa versão não tem coordenação.

**Peça uma previsão:** "As mensagens vão chegar certas?"

```bash
javac -d build/java-3b Monitor-20261002T192224Z-1-001/Monitor/1/*.java
timeout --foreground 5 java -cp build/java-3b ThreadSync3b | grep "My name"
```

Os programas Java nunca terminam sozinhos. O `timeout` encerra depois de 5 segundos; sem ele, use `Ctrl + C`. O `--foreground` é necessário por causa do `| grep`: sem ele, num terminal interativo, o `timeout` derruba o `grep` junto e nada aparece. O `grep` esconde as linhas "How sad, no mail ..". Rode também sem o `grep` para a turma ver quantas vezes o consumidor encontra a caixa vazia.

Saída real (varia a cada execução):

```text
My name is, Dave. I say, Helloog!
My name is, Bill. I say, Hotlo, world.
My name is, Dave. I say, Hot dog!
My name is, Dave. I say, Hello, worl
```

Mostre `storeMessage()` no slide: os produtores escrevem **letra por letra** no **mesmo** array `message`, com um `sleep` aleatório entre as letras. Nada impede o outro produtor de entrar no meio.

As duplas respondem as **questões 3 a 5**.

## 68 a 80 min: Prática 3, Mailbox 3s (com monitor)

Mostre no slide o que mudou: `storeMessage()` e `retrieveMessage()` agora são `synchronized`, e o consumidor espera com `while (!youHaveMail) wait();`.

**Peça uma previsão:** "As mensagens ainda vão se misturar? O consumidor ainda vai dizer 'How sad, no mail'?"

```bash
javac -d build/java-3s Monitor-20261002T192224Z-1-001/Monitor/3/*.java
timeout 5 java -cp build/java-3s ThreadSync3s
```

Saída real:

```text
Looking for my mail ..
My name is, Dave. I say, Hello, world.
Looking for my mail ..
My name is, Bill. I say, Hot dog!
```

Compare com a turma:

- **Integridade:** nenhuma mensagem misturada. Enquanto um produtor escreve, o outro espera a chave.
- **Sem consulta repetida:** "How sad, no mail" desapareceu. O consumidor dorme em `wait()` até um `notify()`.
- **Por que `while`:** quem acorda precisa disputar a chave de novo e **conferir** a condição.

**Limite que você precisa dizer:** a 3s ainda pode **perder** mensagens. O produtor não espera a caixa esvaziar e pode sobrescrever uma mensagem não lida. Ela garante integridade, não entrega. A versão sem perdas está em [CaixaDeCorreio.java](../../../examples/dia07/caixa/CaixaDeCorreio.java) (`make run-caixa`). Mostre se sobrar tempo.

As duplas respondem a **questão 6**.

## 80 a 90 min: atividade final e fechamento

Individual, **questões 7 a 10** (as questões finais do guia do professor): o que o monitor acrescenta, o papel de `synchronized`, o `while` antes de `wait()` e a diferença entre espera ocupada e `wait()`.

Feche com a frase do guia: "Threads compartilham estado; condições de corrida exigem sincronização; semáforos são primitivas explícitas; monitores encapsulam o estado compartilhado e a sincronização, e `wait`/`notify` coordenam condições entre threads."

## Ajustes de ritmo

- **Atrasado na teoria:** encurte Peterson (só pseudocódigo e espera ocupada) e corte a simulação. Não comece a prática depois dos 50 minutos.
- **Atrasado na prática:** na Prática 3, mostre a 3s só no projetor e deixe a questão 6 oral. Preserve as questões 7 a 10.
- **Sobrou tempo:** na Mailbox 3b, peça que uma dupla mude `MAXPROCESSTIME` de 7 para 50 em `Mailbox3b.java` e rode de novo: a mistura fica mais frequente (questão 5). Depois rode `make run-caixa` para mostrar a caixa sem perdas.
- **Laboratório sem gcc ou javac:** faça a Prática 1 no papel e rode as mailboxes só no projetor.

## Referências de consulta

Material-base: [PDF do professor](../../../Aulas_Guiadas_SO_Processos_Sincronizacao.pdf) e [guia completo do professor](../../../preparacao/GUIA_DO_PROFESSOR.md). Simulação: [Produção de dinheiro](../../../pc_trabalho04_202011393/README.md).

Documentação de [sem_wait](https://man7.org/linux/man-pages/man3/sem_wait.3.html), [pthread_create](https://man7.org/linux/man-pages/man3/pthread_create.3.html) e do [contrato de wait/notify em Java](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html).
