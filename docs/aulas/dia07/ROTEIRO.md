# Aula de 7 de outubro de 2026: concorrência e sincronização

**MATA58 · 90 minutos · duas partes: teoria e demonstração visual, seguidas de prática**

Pergunta que conduz a aula: **"Se as threads compartilham a memória, como impedir que uma atrapalhe a outra?"**

Ao final, cada aluno deve conseguir:

- explicar por que `contador++` em duas threads perde incrementos;
- definir região crítica e exclusão mútua;
- dizer o que `mutex`, `empty` e `full` controlam no produtor-consumidor e por que a ordem dos `sem_wait` importa;
- explicar o que um monitor acrescenta (`synchronized`, `wait`, `notify`) e por que se usa `while` antes de `wait`;
- completar o produtor-consumidor com os semáforos certos.

## Preparação do professor

Na raiz do repositório:

```bash
make test               # 11 testes, inclusive corrida, Peterson e produtor-consumidor
make test-simulation    # testa o modelo da simulação, sem janela
make run-simulation     # abre a janela da simulação (JavaFX via Maven)
```

**Ensaie a simulação no computador da sala.** Na primeira execução, o Maven baixa o JavaFX pela internet; se o computador da sala não tiver internet, rode `make run-simulation` antes, num lugar com rede, ou leve o seu notebook.

Deixe abertos: os slides, a janela da simulação (minimizada) e um terminal na raiz do repositório para a prática.

Slides: [aula07_concorrencia.pdf](slides/aula07_concorrencia.pdf) (fonte em [aula07_concorrencia.tex](slides/aula07_concorrencia.tex)). Apostila para os alunos: [apostila07_concorrencia.pdf](apostila/apostila07_concorrencia.pdf); indique **depois da prática**, porque ela traz o `prod_cons.c` completo, que é a solução das lacunas.

Distribua [ATIVIDADE.md](ATIVIDADE.md) sem o [gabarito](GABARITO.md). O esqueleto da prática está em [prod_cons_atividade.c](../../../examples/dia07/prod_cons_atividade.c). Comandos e respostas da demonstração: [COLA_DEMONSTRACAO.md](COLA_DEMONSTRACAO.md).

**Prioridade:** corrida → região crítica → semáforos e produtor-consumidor → simulação → prática. Peterson e monitores podem ser encurtados se o tempo apertar; a simulação e a prática não.

## Agenda

| Minutos | Etapa | Material | Resultado esperado |
|---|---|---|---|
| 0 a 5 | Abertura: de processos a threads | Slides | Lembrar que processos não compartilham memória; threads sim |
| 5 a 12 | Condição de corrida | Slides + `corrida.c` | Explicar o incremento perdido |
| 12 a 20 | Região crítica, exclusão mútua, Peterson | Slides | Distinguir exclusão mútua de espera ocupada |
| 20 a 32 | Semáforos | Slides + quadro | Simular `empty`, `full` e `count` com N=3 |
| 32 a 42 | Produtor-consumidor em C | Slides | Justificar a ordem dos `sem_wait`; construir o deadlock |
| 42 a 50 | Monitores em Java | Slides | Separar lock de condição; explicar o `while` |
| **50 a 70** | **Parte 2: simulação** | Janela da simulação | Ligar cada espera da tela a um semáforo do C |
| 70 a 85 | Prática: seis lacunas | Esqueleto C | Programa termina com 40 itens |
| 85 a 90 | Bilhete de saída | Papel | Separar exclusão mútua de disponibilidade |

---

# Parte 1: teoria (0 a 50 min)

## 0 a 5 min: de processos a threads

Abra com a pergunta: **"Na aula passada, por que o pai somou 0?"** Espere a resposta: cada processo tem a sua memória.

Fala sugerida: "Hoje o problema se inverte. As **threads** de um mesmo processo dividem a memória: código, globais, heap e arquivos abertos. Cada uma tem só a sua pilha e os seus registradores. Comunicar ficou fácil. O difícil agora é não atrapalhar."

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

Mostre o slide com a saída real do `corrida.c`: cerca de 1 milhão em vez de 2 milhões, mudando a cada execução. **Não precisa rodar na aula.**

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

## 20 a 32 min: semáforos

Defina no quadro: semáforo é um **contador que nunca fica negativo**.

- `sem_wait`: se o contador for maior que 0, diminui 1 e segue; se for 0, a thread **dorme**.
- `sem_post`: soma 1 e acorda quem estiver dormindo.

Diferença para Peterson: quem espera **dorme**, não gasta CPU.

Os três semáforos do produtor-consumidor:

| Semáforo | Começa em | Controla |
|---|---|---|
| `mutex` | 1 | **Exclusão mútua:** só uma thread mexe no buffer |
| `empty` | N | **Disponibilidade:** vagas livres |
| `full` | 0 | **Disponibilidade:** itens prontos |

Simule no quadro com N = 3 (os valores valem quando nenhuma operação está em andamento):

| Evento | empty | full | count |
|---|---:|---:|---:|
| início | 3 | 0 | 0 |
| insere A | 2 | 1 | 1 |
| insere B | 1 | 2 | 2 |
| retira A | 2 | 1 | 1 |

Pergunta: **"Onde o quarto item esperaria, se o buffer enchesse?"** Em `sem_wait(&empty)`, sem segurar o mutex.

## 32 a 42 min: produtor-consumidor em C

Projete os laços do produtor e do consumidor do `prod_cons.c`. Leia os cinco passos de cada um:

```text
produtor:   espera vaga → pega mutex → insere → devolve mutex → avisa item
consumidor: espera item → pega mutex → retira → devolve mutex → avisa vaga
```

Regra para a turma: **cada thread espera o que consome e avisa o que cria.**

Construa o deadlock no quadro: o consumidor pega o mutex e depois espera `full` com o buffer vazio. O produtor precisa do mutex para inserir. Um espera o outro para sempre.

## 42 a 50 min: monitores em Java

Fala de transição: "Semáforos funcionam, mas é fácil esquecer um, trocar a ordem ou proteger a coisa errada. O monitor junta os dados e a sincronização no mesmo objeto."

Mostre nos slides:

- `synchronized`: para entrar no método, a thread precisa da **chave** (lock) do objeto.
- `wait()`: **solta a chave** e dorme até ser avisada.
- `notify()`: acorda uma thread que espera. Ela ainda precisa **pegar a chave de novo**.
- `while` e não `if` antes de `wait()`: quem acorda precisa **conferir de novo** a condição.

Compare as mailboxes pelo código dos slides (não precisa rodar): a 3b mistura mensagens (`Hello, worl`, `Het dog!`); a 3s não mistura.

**Limite que você precisa dizer:** a 3s ainda pode **perder** mensagens. O produtor não espera a caixa esvaziar e sobrescreve uma mensagem não lida. Ela garante integridade, não entrega.

---

# Parte 2: demonstração visual (50 a 70 min)

Transição: "Agora vamos ver o produtor-consumidor acontecendo. Cada coisa que aparecer na tela tem um semáforo do C por trás."

Abra a simulação (`make run-simulation`). Antes de clicar, mostre na tela:

- as **10 posições** do buffer (N = 10 aqui);
- **W**: próxima escrita (o `hi` do C); **R**: próxima leitura (o `lo`);
- "itens disponíveis" corresponde a `full`, e "vagas livres" a `empty`;
- as linhas **Produtor:** e **Consumidor:** mostram quem está esperando.

Para cada experimento, **pergunte antes de mostrar.**

### Experimento 1: equilíbrio (3 min)

Deixe as duas velocidades em 2 itens/s e clique em **Iniciar**.

Pergunta: "O buffer vai encher?" Ele fica oscilando com poucas notas. W e R andam juntos.

### Experimento 2: produtor rápido (5 min)

Produtor em 10 itens/s, consumidor em 0,2.

Pergunta antes: "O que vai acontecer? Quem vai esperar?"

Na tela: o buffer enche e aparece **"Produtor: Aguardando vaga: buffer cheio"**.

Pergunte: **"Em qual linha do `prod_cons.c` o produtor está dormindo? Quanto vale `empty`?"** Resposta: em `wait_sem(&empty)`, com `empty = 0`.

### Experimento 3: consumidor rápido (5 min)

Inverta: produtor em 0,2, consumidor em 10.

Na tela: o buffer esvazia e aparece **"Consumidor: Aguardando item: buffer vazio"**.

Pergunte: "Onde o consumidor dorme? Quanto vale `full`?" Resposta: em `wait_sem(&full)`, com `full = 0`.

### Experimento 4: pausa e índices circulares (4 min)

Volte as velocidades para algo intermediário e deixe rodar até W e R darem a volta.

Pergunte: "Por que W voltou para a posição 0?" Resposta: o `% N` em `hi = (hi + 1) % N`.

Clique em **Pausar**. Conte com a turma: itens + vagas = 10. Pergunte: "Isso vale sempre?" Vale quando nenhuma operação está em andamento (`empty + full = N`).

### Fechamento da parte 2 (3 min)

Pergunte: "Em algum momento duas threads mexeram no buffer ao mesmo tempo?" Não: é o `mutex`.

Feche: "O `mutex` garante que ninguém atrapalha. `empty` e `full` garantem que ninguém trabalha sem ter o que precisa."

Se a simulação não abrir: siga com o quadro, usando a tabela N = 3 da parte 1 e perguntando quem espera em cada caso.

---

# Prática e fechamento (70 a 90 min)

## 70 a 85 min: seis lacunas

Distribua o esqueleto. As duplas completam os valores iniciais de `empty` e `full`, e qual semáforo cada thread espera e avisa. O `mutex` já vem pronto.

Condução sugerida: 2 minutos para explicar, 10 para implementar, 3 para comparar.

Comando dos alunos (na raiz):

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

## 85 a 90 min: bilhete de saída

Individual: **"Qual problema o `mutex` resolve e qual problema `empty` e `full` resolvem?"**

Fala final: "Processos isolam a memória e precisam de pipe para conversar. Threads compartilham e precisam de sincronização para não se atrapalhar. Semáforos e monitores são as ferramentas."

## Ajustes de ritmo

- **Atrasado 10 minutos:** encurte Peterson (só pseudocódigo e espera ocupada) e monitores (só `synchronized`, `wait` e o `while`). Preserve a simulação e a prática.
- **Sobrou tempo:** na simulação, pergunte o que aconteceria se o consumidor pegasse o `mutex` antes de esperar `full`. Depois, na prática, peça que uma dupla troque a ordem e veja o programa travar.
- **Laboratório sem gcc:** faça a prática no papel, com as seis respostas e a justificativa de cada uma.

## Referências de consulta

Material-base: PDF local e [guia completo do professor](../../../preparacao/GUIA_DO_PROFESSOR.md). Simulação: [Produção de dinheiro](../../../pc_trabalho04_202011393/README.md).

Documentação de [sem_wait](https://man7.org/linux/man-pages/man3/sem_wait.3.html), [pthread_create](https://man7.org/linux/man-pages/man3/pthread_create.3.html) e do [contrato de wait/notify em Java](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html).
