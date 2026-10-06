# Preparação das aulas de SO: 5 e 7 de outubro de 2026

## Atualização: exemplos C corrigidos

Os quatro arquivos C da raiz foram revisados. As análises dos problemas abaixo se referem às versões originais, preservadas em `preparacao/originais/`.

- `fork2.c`: calloc inicializa o vetor; falhas são verificadas; cada processo coleta seus filhos. Sem falhas, oito processos são criados ao todo e o pai imprime soma zero.
- `pipe2.c`: o pai lê a, b e delta antes dos forks. Mantém seis processos e cinco pipes, transportando doubles binários com tratamento de transferências parciais. Fecha pontas não usadas, coleta filhos e valida entradas/resultados. Os alunos devem digitar `1 -3 1` para obter raízes 1 e 2. Não há prompts dos fornecedores nesta versão.
- `peterson-code.c`: atomics C11 sequencialmente consistentes, protocolo convencional sem alternância forçada, buffer suficiente e 10.000 entradas por thread. O contador final deve ser 20.000; a ordem das threads não é fixa.
- `prod_cons.c`: acessos a count protegidos; funções e comentários corrigidos; falhas verificadas. Produz e consome 40 itens, mantendo capacidade 20, e termina após joins e destruição dos semáforos. A ordem FIFO é preservada; números e intercalação dos logs variam.

Compile com `-std=c11 -Wall -Wextra -Wpedantic`; acrescente `-lm` em pipe2 e `-pthread` nos exemplos de threads. As demonstrações C revisadas terminam sozinhas. As limitações dos exemplos Java continuam válidas.

Este guia complementa `Aulas_Guiadas_SO_Processos_Sincronizacao.pdf` e explica os arquivos fornecidos. Planejamento confirmado: dois encontros de 90 minutos; a turma já conhece C e o básico de Java. Na comparação das mailboxes, concentre a leitura na classe Mailbox e nas operações de sincronização.

## 1. A história que liga as duas aulas

No dia 5: como criar processos e fazê-los cooperar, se suas memórias são independentes?

No dia 7: como fazer threads cooperarem corretamente, se elas compartilham memória?

Fala de transição: “Na primeira aula, precisamos construir um caminho para os dados chegarem a outro processo. Na segunda, o caminho já existe, porque as threads compartilham memória. Precisamos controlar quando cada uma pode acessar os dados.”

## 2. Entendendo os arquivos

### fork2.c: criação de processos e memória

`malloc` aloca um vetor de três inteiros. `mypid` guarda o PID original. O laço chama `fork` três vezes em cada caminho de execução. O filho recebe retorno zero e escreve em `v[i]`; o pai recebe o PID do filho. Ambos continuam o laço. Se nenhuma criação falhar, são criados sete filhos, totalizando oito processos ao longo da execução. Não é garantido que os oito permaneçam vivos simultaneamente.

```text
Antes do laço:                 1 processo
Após a primeira bifurcação:    2 caminhos
Após a segunda bifurcação:     4 caminhos
Após a terceira bifurcação:    8 caminhos
```

Há sete chamadas de fork executadas no conjunto da árvore: 1 + 2 + 4. Cada caminho percorre três iterações. A quantidade de chamadas no texto do programa não é a quantidade de chamadas executadas.

O PID armazenado em `mypid` é copiado para os filhos. Já `getpid()` retorna o PID de quem está executando agora. Por isso somente o processo original satisfaz a comparação final e imprime a soma.

**Problema decisivo:** `malloc` não inicializa os elementos. O pai não escreve em nenhum elemento do vetor e soma valores indeterminados. A saída original não serve como resultado previsível; não explique uma soma estranha como “efeito do escalonamento”.

Para uma demonstração controlada, faça uma cópia e substitua a alocação por `int *v = calloc(tamanho, sizeof *v);`, verificando se retornou NULL. O pai então imprime zero. Se quiser mostrar o filho, acrescente uma impressão logo após sua escrita, com PID, índice e valor. Inicializar resolve a leitura indeterminada; não implementa tratamento de falha de fork nem coleta dos filhos.

O heap não vira compartilhado porque foi alocado antes do fork. Pai e filho têm espaços virtuais independentes; o sistema pode usar copy-on-write internamente. Endereços virtuais iguais não provam compartilhamento. `sleep` e `waitpid` não fazem as escritas do filho aparecerem na memória comum do pai.

### pipe2.c: o arquivo aberto no editor

Objetivo: transportar componentes de Bhaskara para um calculador e devolver duas raízes ao processo original. A entrada contém `a`, `b` e **delta já calculado**, não `c`. A fórmula é `(-b ± sqrt(delta)) / (2*a)`.

```text
Processo original
├── son1: calculador
└── son5: criador dos três fornecedores
    ├── son2: lê a e envia 2*a
    ├── son3: lê b e envia -b
    └── son4: lê delta e envia delta
```

Total: seis processos, contando o original. `son2`, `son3` e `son4` são netos do original. Os nomes das variáveis não definem a relação de parentesco; quem executa cada fork define.

| Pipe | Escritor | Leitor | Dado |
|---|---|---|---|
| fd_1 | son3 | son1 | -b |
| fd_2 | son2 | son1 | 2*a |
| fd_3 | son4 | son1 | delta |
| fd_4 | son1 | original | raiz com sinal menos |
| fd_5 | son1 | original | raiz com sinal mais |

Os pipes são criados antes dos forks, então os processos herdam seus descritores. `fd[0]` lê e `fd[1]` escreve. `write` envia bytes, `read` recebe bytes. Aqui números são convertidos em texto com `sprintf`, enviados incluindo o terminador zero, e recuperados com `atoi`. O comentário “Receiving sqrt(delta)” está incorreto: o dado recebido é delta; a raiz quadrada é calculada depois.

Quando `son1` chega a uma leitura sem dados, pode bloquear. O escalonador pode então executar os fornecedores. Não há garantia de que o prompt de `a` apareça antes do de `b` ou do de delta.

**Cuidados antes da demonstração:**

- Três processos usam o mesmo terminal para `scanf`. Responda ao prompt exibido, sem presumir uma ordem. Redirecionar três números de um arquivo não é uma boa demonstração desse original: buffers de entrada e leitores concorrentes complicam a distribuição.
- `char string[10]` é pequeno para vários resultados formatados por `%f`. Aumentar o buffer e usar `snprintf` evita escrita além do limite, mas ainda exige verificar truncamento.
- `fflush(stdin)` não é uma operação portátil de descarte de entrada. Remova em uma versão preparada; não o ensine como sincronização.
- Validar `a != 0`, delta não negativo e o sucesso de `scanf`, `pipe`, `fork`, `read` e `write`.
- Pipes são fluxos de bytes: uma leitura não garante uma mensagem inteira. Implementações robustas acumulam bytes e definem enquadramento; `read` também não acrescenta terminador de string.
- Fechar **todos** os descritores desnecessários de cada processo. O original mantém várias pontas herdadas abertas; isso pode impedir EOF se um fornecedor falhar.
- Coletar filhos com `waitpid` em seus respectivos pais. `exit(1)` sinaliza falha; fornecedores bem-sucedidos devem terminar com sucesso.

Caso para desenhar no quadro: `a=1`, `b=-3`, `delta=1` (equação `x²-3x+2=0`). Os fornecedores enviam `2`, `3` e `1`. O calculador devolve `1` e `2`. Faça a conta antes de mostrar o código.

### peterson-code.c: intenção de entrar e desempate

Peterson é um algoritmo de exclusão mútua para dois participantes, com hipóteses sobre atomicidade e ordenação de memória. `interested[i]` indica intenção de entrar. `turn` resolve a disputa. Nesta variante, quem entra escreve seu próprio número e espera se o outro estiver interessado e o turno ainda for o seu. Para apresentar sem ambiguidade, use a forma convencional abaixo:

```text
interessado[eu] = verdadeiro
vez = outro
enquanto interessado[outro] e vez == outro:
    esperar
região crítica
interessado[eu] = falso
```

Só o primeiro teste falso permite prosseguir. Se ambos querem entrar, a última escrita de `vez` determina quem cede. Se o outro não quer entrar, não há motivo para esperar. Exclusão mútua não exige alternância estrita.

O original tem **testes externos de turn que impõem alternância** e usa a mesma variável no protocolo de entrada; essas esperas não fazem parte da apresentação convencional de Peterson. Não use a alternância observada para provar o algoritmo.

Além do include ausente de `unistd.h` e do estouro de `cr[8]` ao copiar `"thread1#######"`, há acessos concorrentes a `turn` e `interested` como inteiros comuns. Em C com pthreads, isso introduz data races e comportamento indefinido. Aumentar `cr` ou usar `volatile` não resolve a sincronização. Para executar uma versão correta, use atomics C11 com ordenação apropriada (sequencialmente consistente é uma escolha didática simples), sem os testes externos. Para esta aula, basta explicar Peterson no quadro e executar o exemplo de semáforos.

Espera ocupada repete testes e consome CPU. `sleep(1)` fora da região crítica não transforma o algoritmo em espera bloqueante e não garante correção.

### prod_cons.c: disponibilidade e exclusão mútua

Uma thread produz números; outra os consome. Ambas acessam o mesmo buffer circular de capacidade `N=20`.

| Estado | Significado |
|---|---|
| buffer | itens armazenados |
| hi | próxima posição de inserção |
| lo | próxima posição de retirada |
| count | quantidade armazenada |
| mutex, inicial 1 | permissão para acessar a região crítica |
| empty, inicial N | vagas disponíveis para reserva |
| full, inicial 0 | itens disponíveis para reserva |

`sem_wait` decrementa uma permissão disponível; se não houver, espera. `sem_post` disponibiliza uma permissão. Aqui `mutex` é um semáforo binário, não um objeto `pthread_mutex_t`.

Produtor: produz fora do lock → reserva vaga → adquire mutex → insere → libera mutex → publica item.

Consumidor: reserva item → adquire mutex → retira → libera mutex → publica vaga → processa item fora do lock.

Por que a ordem importa? Se um consumidor adquirir mutex e depois esperar por full com o buffer vazio, o produtor precisará do mutex para inserir. Cada um esperará algo que o outro não pode fornecer: deadlock.

`(indice + 1) % N` permite reutilizar as posições. Para N=3, índices percorrem 0, 1, 2, 0.

**Precisões que o PDF não destaca:**

- `consume_item` lê `count` depois que o consumidor liberou mutex. O produtor pode escrever nessa variável simultaneamente. Essa leitura também deve ser protegida, ou a mensagem “Vazio” deve usar um valor capturado dentro da região crítica.
- `empty + full = N` vale na simulação após operações completas, sem reservas em andamento. Durante um `sem_wait` seguido de inserção/retirada e antes do `sem_post` correspondente, a soma pode ser menor.
- `pthread_join` faz a thread chamadora esperar que a outra termine. Não impede a thread alvo de terminar antes do join. O segundo argumento de `pthread_create` recebe atributos, não um indicador que adia término.
- Remova os casts `(void *) producer` e `(void *) consumer`: as funções já têm a assinatura apropriada para `pthread_create`.
- Declare explicitamente `time` incluindo `time.h`; trate retornos das APIs. `remove_item` não retorna valor se a condição falhar, embora o protocolo correto deva garantir item disponível.
- No original, os laços eram infinitos e o join não retornava. A versão revisada produz e consome 40 itens e termina sozinha. A ordem das mensagens varia entre execuções; log não é prova de ausência de corrida.

### Monitores Java: pastas Monitor/1 e Monitor/3

O caminho real é `Monitor-20261002T192224Z-1-001/Monitor`.

`Mailbox3b` tem array e flag compartilhados por produtores e consumidor. Escritas caractere por caractere podem intercalar, e o consumidor pode observar uma mensagem em construção. Campos privados encapsulam, mas não sincronizam.

`Mailbox3s` usa métodos de instância `synchronized`: o lock é o da mailbox. `storeMessage` e `retrieveMessage` usam o mesmo lock, então seus trechos protegidos não executam simultaneamente. Durante `wait`, o consumidor libera esse monitor. Depois de acordar, precisa readquirir o lock e verificar a condição novamente. `notify` seleciona um aguardante; não transfere imediatamente o lock. `Thread.sleep` não libera o monitor.

**Limitação concreta:** o produtor da versão 3s não espera a caixa esvaziar. Ele pode sobrescrever uma mensagem ainda não consumida. A versão demonstra exclusão mútua e espera do consumidor, mas não garante entrega de todas as mensagens. Não apresente como equivalente completo ao buffer limitado em C.

Para mostrar uma mailbox de uma posição sem sobrescrita, o protocolo conceitual seria:

```text
guardar, sob o mesmo monitor:
    enquanto cheia: wait()
    copiar mensagem
    cheia = verdadeiro
    notifyAll()

retirar, sob o mesmo monitor:
    enquanto vazia: wait()
    copiar mensagem para resultado
    cheia = falso
    notifyAll()
    retornar resultado
```

`notifyAll` permite que produtores e consumidores acordados reavaliem suas próprias condições. Uma implementação também precisa tratar interrupção e tamanho máximo da mensagem. O original ignora interrupções.

## 3. Aula de 5/10: processos e comunicação

Objetivos observáveis: desenhar a árvore de forks, explicar por que o pai não recebe escritas no heap do filho e completar uma troca de dados com pipe.

| Minutos | Condução e fala sugerida | Evidência de compreensão |
|---|---|---|
| 0 a 10 | “Um programa no disco já é um processo?” Defina processo, PID e espaço de memória. | Aluno distingue instruções de instância em execução. |
| 10 a 25 | Mostre o laço de fork2, escondendo a execução. “O filho também volta para o laço?” Desenhe 1→2→4→8. | Aluno prevê oito caminhos e explica os retornos de fork. |
| 25 a 40 | Aponte o vetor não inicializado. Mostre uma cópia inicializada com calloc. “Esperar o filho mudaria a soma do pai?” | Aluno responde não e justifica memória independente. |
| 40 a 50 | Duplas respondem as perguntas 1 a 4 abaixo; faça correção oral. | Justificativas, não apenas números. |
| 50 a 62 | Desenhe pai, filho e pipe. “O dado atravessa a variável ou atravessa o pipe?” Apresente leitura, escrita e fechamento. | Aluno identifica ponta leitora e escritora. |
| 62 a 75 | Use o mapa de pipe2 e os valores 1, -3, 1. Leia só os trechos de envio, cálculo e recepção. | Aluno segue -b, 2a e delta até as raízes. |
| 75 a 87 | Prática pai-filho: filho envia 30, pai imprime. Use o esqueleto do PDF. | Compila ou entrega pseudocódigo correto com close/read/write. |
| 87 a 90 | Bilhete de saída: “Por que malloc antes de fork não basta para compartilhar resultado?” | Resposta menciona espaços independentes e IPC. |

Se a demonstração original de pipe2 falhar, mantenha a análise do fluxo e use o programa pequeno pai-filho. A aula deve demonstrar IPC; não precisa executar todos os fornecedores interativos.

### Questões e respostas do dia 5

1. Retornos de fork? Zero no filho, PID do filho no pai e -1 na falha.
2. Quantos processos com três iterações se todos continuam? Oito no total, sete novos, supondo sucesso.
3. Por que só o original imprime em fork2? Compara PID atual com PID original copiado.
4. Qual soma prever no original? Nenhuma soma confiável: elementos não inicializados. Na cópia com calloc, o pai soma zero.
5. O pai precisa executar depois do filho para enxergar a escrita? A ordem não resolve isolamento; deve haver IPC ou memória explicitamente compartilhada.
6. Por que criar o pipe antes do fork? Para ambos herdarem seus descritores.
7. Pipe vazio implica EOF? Não. Uma leitura bloqueante espera se ainda há escritor aberto; EOF ocorre com pipe vazio e todas as pontas de escrita fechadas.
8. Gabarito da prática: filho fecha leitura, calcula 30, escreve os bytes do inteiro, fecha escrita; pai fecha escrita, lê os bytes, imprime e fecha leitura. Na versão preparada, verificar resultados e aguardar o filho com waitpid. Para dados gerais, tratar leituras/escritas parciais.

## 4. Aula de 7/10: concorrência e sincronização

Material pronto para aplicação: [roteiro](../docs/aulas/dia07/ROTEIRO.md) (teoria, simulação e prática), [atividade](../docs/aulas/dia07/ATIVIDADE.md), [gabarito](../docs/aulas/dia07/GABARITO.md) e [cola de demonstração](../docs/aulas/dia07/COLA_DEMONSTRACAO.md). A seção abaixo continua valendo como referência conceitual.

Objetivos observáveis: construir uma intercalação problemática, justificar a ordem dos semáforos e explicar a diferença entre lock e espera por condição.

| Minutos | Condução e fala sugerida | Evidência de compreensão |
|---|---|---|
| 0 a 12 | Retome memória dos processos e contraste com threads do mesmo processo. Simule incremento no quadro. | Aluno identifica resultado perdido. |
| 12 a 23 | Região crítica, exclusão mútua e Peterson em pseudocódigo. “O que essa espera faz com a CPU?” | Aluno distingue exclusão de alternância e espera ocupada. |
| 23 a 38 | Distribua cartões de vagas, itens e chave. Inicialize N=3, empty=3, full=0, mutex=1. | Aluno distingue disponibilidade de proteção. |
| 38 a 53 | Leia produtor e consumidor. Simule duas inserções e uma retirada. Inverta a ordem no quadro para construir deadlock. | Aluno explica por que esperar full dentro do mutex trava. |
| 53 a 63 | Apresente monitor como estado e operações protegidas. Analogia: chave para entrar e condição para prosseguir. | Aluno não confunde possuir lock com haver mensagem. |
| 63 a 75 | Compare Mailbox3b e 3s. Execute se houver JDK; senão simule duas escritas intercaladas. | Aluno aponta array/flag e métodos críticos. |
| 75 a 83 | Explique wait, notify e while. Mostre que 3s pode sobrescrever mensagens. | Aluno separa integridade da mensagem de garantia de entrega. |
| 83 a 90 | Duplas respondem questões abaixo; recolha um bilhete de saída. | Aluno justifica uma escolha de sincronização. |

### Intercalação para o quadro

Exemplo conceitual de duas threads incrementando um contador inicialmente zero:

| Passo | Thread A | Thread B |
|---|---|---|
| 1 | lê 0 | |
| 2 | | lê 0 |
| 3 | calcula 1 | |
| 4 | | calcula 1 |
| 5 | escreve 1 | |
| 6 | | escreve 1 |

Resultado da simulação: 1, embora se desejasse 2. Em C real, acessos concorrentes sem sincronização a um inteiro comum configuram data race; não limite a explicação do comportamento indefinido aos resultados 1 e 2.

### Simulação dos semáforos

Valores nos momentos em que cada operação terminou e nenhuma reserva está pendente:

| Evento, N=3 | empty | full | count |
|---|---:|---:|---:|
| início | 3 | 0 | 0 |
| insere A | 2 | 1 | 1 |
| insere B | 1 | 2 | 2 |
| retira A | 2 | 1 | 1 |

Pergunte onde o quarto item aguardaria após preencher as três posições: em `sem_wait(&empty)`, sem possuir o mutex.

### Questões e respostas do dia 7

1. Qual estado threads compartilham? Dados globais, heap e recursos do processo; cada thread tem sua própria pilha e contexto.
2. O que mutex protege? O acesso ao estado do buffer, não a quantidade de vagas.
3. O que full=0 significa? Nenhum item disponível para reserva pelo consumidor.
4. Por que sem_wait(full) vem antes de sem_wait(mutex)? Para não bloquear a inserção enquanto espera por item.
5. Com N=5, duas produções e um consumo completos: empty=4, full=1, count=1.
6. Por que while em torno de wait? A condição pode não estar satisfeita após acordar, inclusive por despertares espúrios ou ação de outras threads.
7. notify entrega imediatamente o monitor? Não; o aguardante precisa readquirir o lock.
8. sleep libera monitor Java? Não. wait libera o monitor do objeto em que foi chamado.
9. A versão 3s entrega toda mensagem? Não; um produtor pode sobrescrever a mensagem anterior antes da retirada.
10. Bilhete de saída: “Qual problema mutex resolve e qual problema full/empty resolvem?” Esperado: exclusão mútua versus disponibilidade.

## 5. Preparação prática e limites verificados

Não use os originais como demonstrações de código correto sem os ajustes descritos. Preserve-os como referência e prepare cópias para execução. Não acrescente sleep como solução de corrida.

Comandos de compilação dos originais, depois dos ajustes pertinentes:

```bash
gcc -Wall -Wextra fork2.c -o /tmp/aula-fork
gcc -Wall -Wextra pipe2.c -o /tmp/aula-pipe -lm
gcc -Wall -Wextra peterson-code.c -o /tmp/aula-peterson -pthread
gcc -Wall -Wextra prod_cons.c -o /tmp/aula-prod-cons -pthread
```

Esses comandos não constituem garantia de correção concorrente. Não execute Peterson original apenas corrigindo o include e o buffer.

Java, no laboratório com JDK, usando diretórios temporários para não substituir os .class fornecidos:

```bash
mkdir -p /tmp/so-java-3b /tmp/so-java-3s
javac -d /tmp/so-java-3b Monitor-20261002T192224Z-1-001/Monitor/1/*.java
java -cp /tmp/so-java-3b ThreadSync3b
# Interrompa com Ctrl+C antes de iniciar a outra versão.
javac -d /tmp/so-java-3s Monitor-20261002T192224Z-1-001/Monitor/3/*.java
java -cp /tmp/so-java-3s ThreadSync3s
```

Neste ambiente, GCC está disponível; `javac` não foi encontrado no PATH. Foi feita verificação de sintaxe dos quatro arquivos C: Peterson falha por declaração ausente de sleep. Os demais passaram essa etapa com avisos, o que não valida seu comportamento em execução. Não foram executadas as demonstrações Java nem certificados os originais como seguros.

Antes de cada encontro: ensaie no computador do laboratório; deixe o mapa de processos pronto; escolha apenas trechos pequenos para projetar; peça previsão antes de executar; reserve tempo para os alunos explicarem o resultado. Se o ritmo atrasar, reduza a leitura de pipe2 no dia 5 e mantenha Peterson apenas conceitual no dia 7.

## 6. Referências para consulta

- Material local: `Aulas_Guiadas_SO_Processos_Sincronizacao.pdf` e arquivos C/Java da pasta.
- Pipes, fluxo de bytes, bloqueio e EOF: https://www.man7.org/linux/man-pages/man7/pipe.7.html
- Criação e extremidades de pipe: https://man7.org/linux/man-pages/man2/pipe.2.html
- Espera pelo término de thread: https://www.man7.org/linux/man-pages/man3/pthread_join.3.html
- Operação de semáforo: https://www.man7.org/linux/man-pages/man3/sem_wait.3.html
- Contrato de wait/notify, aquisição do monitor e despertares espúrios: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html

## Demonstração visual adicional para 7/10

Use [Produção de dinheiro](../pc_trabalho04_202011393/README.md) por cinco minutos antes da leitura do produtor-consumidor C, dentro do bloco de 38 a 53 min. Faça o buffer encher com produtor rápido e consumidor lento; depois inverta as velocidades. Peça à turma para identificar quem espera e em qual semáforo do C esperaria. Pause para discutir os dez slots e índices circulares. A nova versão usa threads Java e semáforos, não processos nem o monitor das mailboxes.

Atualização da verificação Java: o compilador deste ambiente pode ser chamado por `java -m jdk.compiler/com.sun.tools.javac.Main`, embora não haja executável javac no PATH. O modelo da simulação foi compilado e testado; a interface foi compilada com bibliotecas JavaFX. A janela passou por um teste de inicialização de oito segundos; a inspeção visual e a operação dos controles permanecem etapas de ensaio no laboratório.
