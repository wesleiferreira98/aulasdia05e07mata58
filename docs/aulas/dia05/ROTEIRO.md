# Aula de 5 de outubro de 2026 — processos e comunicação

**MATA58 · 90 minutos · turma com conhecimento de C**

Pergunta que conduz a aula: **“Como um processo recebe o resultado calculado por outro?”**

Ao final, cada aluno deve conseguir identificar os retornos de fork, explicar o isolamento da memória, desenhar a árvore do exemplo e completar uma comunicação filho→pai com pipe.

## Preparação do professor

Na raiz do repositório:

```bash
make test
make build/pipe-soma
```

Deixe abertos `fork2.c`, `pipe2.c` e o [exemplo pequeno resolvido](../../../examples/dia05/pipe_soma.c). Prepare dois terminais: um para código, outro para execução. As saídas de fork2 e do pipe pequeno são determinísticas; os PIDs que o SO atribui não são.

Para estudo, indique a [apostila](apostila/apostila05_processos_ipc.pdf) **depois da prática**: ela traz o `pipe_soma.c` completo (seção 7.7), que é a solução das seis lacunas. Distribua [ATIVIDADE.md](ATIVIDADE.md) sem o [gabarito](GABARITO.md). O esqueleto da prática está em [pipe_soma_atividade.c](../../../examples/dia05/pipe_soma_atividade.c).

**Prioridade:** fork → memória independente → IPC. O exemplo grande de Bhaskara é uma aplicação para leitura guiada; não exige estudar cada função auxiliar. A simulação Java de produtor-consumidor fica para o dia 7.

## Agenda

| Minutos | Etapa | Material | Resultado esperado |
|---|---|---|---|
| 0–8 | Problema de abertura: programa e processo | Quadro | Distinguir programa, processo e PID |
| 8–20 | Uma chamada de fork | Trecho curto | Identificar pai, filho e continuação |
| 20–33 | Fork dentro do laço | fork2.c | Prever oito processos no total |
| 33–45 | Memória e espera pelo filho | fork2.c + terminal | Justificar soma zero no pai |
| 45–53 | Checagem em duplas | Questões 1–4 | Explicar, não só decorar |
| 53–65 | Pipe: canal, pontas e bloqueio | Desenho + exemplo pequeno | Seguir bytes do filho ao pai |
| 65–75 | Aplicação com vários processos | pipe2.c | Mapear fornecedores e calculador |
| 75–87 | Prática pai-filho | Esqueleto | Enviar e receber um inteiro |
| 87–90 | Bilhete de saída | Questão final | Ligar isolamento da memória e IPC |

## 0–8 min — abra com uma pergunta

Pergunte: **“Se eu executar o mesmo programa em dois terminais, tenho um programa ou dois processos?”** Dê um minuto para respostas.

Fala sugerida: “O arquivo contém as instruções. Cada execução tem seu próprio estado: PID, registradores, pilha, heap e recursos. Chamamos essa instância de processo.”

Desenhe um arquivo apontando para duas caixas, com PIDs diferentes. Não precisa detalhar todas as estruturas internas do kernel.

Pergunte: “Se uma dessas execuções altera uma variável, a outra necessariamente observa a alteração?” Registre as hipóteses; retome na demonstração.

## 8–20 min — explique uma bifurcação antes de mostrar o laço

Projete apenas:

```c
pid_t pid = fork();
if (pid == -1) {
    /* falha: nenhum filho foi criado nesta chamada */
} else if (pid == 0) {
    /* este caminho executa no filho */
} else {
    /* este caminho executa no pai; pid identifica o filho */
}
/* pai e filho podem chegar aqui */
```

Fala sugerida: “Uma chamada bem-sucedida cria um filho. O pai e o filho continuam depois da chamada, mas recebem retornos diferentes. O filho não recomeça pela primeira linha de main.”

Escreva no quadro: **filho → 0; pai → PID do filho; falha → -1**.

Explique `getpid`: retorna o PID do processo que está executando. O valor retornado por fork no pai identifica o filho, não o próprio pai.

Pergunta de previsão: “O pai necessariamente executa o próximo printf antes do filho?” Resposta esperada: não; a ordem depende do escalonamento e da sincronização existente.

## 20–33 min — agora mostre fork2.c

Projete primeiro só a alocação, o PID original e o laço, sem executar:

```c
int *v = calloc((size_t)tamanho, sizeof *v);
pid_t original = getpid();
for (int i = 0; i < tamanho; ++i) {
    pid_t pid = fork();
    if (pid == -1) { /* tratamento de falha */ }
    if (pid == 0) v[i] = i + 5;
}
```

O trecho é um recorte explicativo; a versão completa trata falhas com break.

Peça dois minutos para a turma desenhar o que acontece com `tamanho=3`. Depois construa:

```text
Inicio                       1 caminho
Depois da iteracao i=0       2 caminhos
Depois da iteracao i=1       4 caminhos
Depois da iteracao i=2       8 caminhos
```

Todos continuam o laço. Cada processo existente ao entrar na próxima iteração pode criar outro. Supondo sucesso de todos os forks, são **oito processos ao todo, sete novos**. Há 1+2+4=7 chamadas executadas no conjunto da árvore; o texto do código contém uma chamada dentro do laço.

Precisão para você: não afirme que os oito estarão vivos simultaneamente. O número descreve os processos criados ao longo da execução. Um escalonamento pode permitir que algum termine enquanto outro ainda não foi criado.

Pergunte: “Se os filhos saíssem imediatamente do laço, a contagem continuaria sendo oito?” Não: se só o original completasse as três iterações e cada filho parasse de criar filhos, seriam quatro processos ao todo.

## 33–45 min — execute e explique a soma

Antes de rodar, peça uma previsão para a soma do pai: 0, 18 ou outro valor? Então:

```bash
make run-fork
```

Saída:

```text
Soma total no pai: 0
```

Fala sugerida: “calloc começa com três zeros. Quando um filho escreve, muda a sua própria memória. O pai não executa aquele if e seu vetor continua zerado. A soma do pai continua zero.”

Desenhe:

```text
Pai:   v = [0, 0, 0]
           fork
Filho: v = [0, 0, 0] → escreve na propria copia
Pai:   v = [0, 0, 0] → permanece igual
```

Esse desenho representa uma bifurcação; a árvore completa tem mais caminhos e valores herdados de escritas anteriores em alguns descendentes.

Mostre a comparação `getpid() == original`. Explique que o número original foi copiado aos filhos, mas o PID atual de cada um é diferente. Por isso apenas o processo original imprime.

Apresente o bloco de waitpid como: **“Cada processo espera e coleta seus próprios filhos diretos.”** Não percorra linha a linha errno/status nesta primeira explicação. Um processo terminado pode manter informações de término no SO até seu pai coletá-las; esperar/coletar trata esse ciclo de vida, não compartilha memória.

Pergunte: “O pai já espera os filhos. Por que a soma não virou 18?” Resposta: esperar não transfere resultados. Nem sleep nem waitpid tornam o heap compartilhado.

Se alguém perguntar sobre o original: ele usava malloc sem inicializar os elementos. A saída era uma leitura de valores indeterminados, sem soma confiável. Use a versão corrigida nesta aula.

## 45–53 min — checagem em duplas

Peça as questões 1–4 da atividade. Dê cinco minutos e use três para correção oral. Escolha uma dupla para explicar a árvore e outra para explicar por que esperar não muda a soma.

Se muitos responderem 18, volte ao desenho das duas memórias antes de introduzir pipe.

## 53–65 min — construa o canal

Transição: **“Se a memória comum não leva o resultado ao pai, precisamos enviar os dados explicitamente.”** Apresente IPC como comunicação entre processos.

Desenhe o pipe antes do fork:

```text
pipe(fd) → fd[0]: leitura     fd[1]: escrita
fork()   → pai e filho herdam os descritores

Filho: calcula 30 → write(fd[1], ...) → pipe → read(fd[0], ...) → Pai
       fecha fd[0]                              fecha fd[1]
```

Descritor é um número que identifica um recurso aberto para o processo. O pipe é mantido pelo SO. Pai e filho possuem suas próprias tabelas de descritores, com referências herdadas ao mesmo canal.

Mostre as quatro operações essenciais no [exemplo resolvido](../../../examples/dia05/pipe_soma.c): pipe, close, write/read dentro de transferir, e fork. `waitpid` completa a coleta do filho.

Os ponteiros em `write(fd, &valor, sizeof valor)` e `read(fd, &valor, sizeof valor)` apontam para variáveis **locais de cada processo**. O SO copia os bytes pelo canal; o ponteiro não torna as duas variáveis compartilhadas.

Pergunte: “E se o pai chegar em read antes de o filho escrever?” A leitura bloqueante pode esperar dados. Com pipe vazio e todas as pontas de escrita fechadas, read retorna zero: EOF. Por isso é importante fechar pontas desnecessárias, incluindo cópias herdadas.

Execute:

```bash
make run-pipe-soma
```

Saída: `Resultado recebido: 30`.

Diga em uma frase que pipes transportam fluxo de bytes: nem toda chamada de read recebe tudo que pedimos. O helper transferir lida com isso e com interrupções; a prática pede entender as pontas e o fluxo, não reescrever esse helper.

## 65–75 min — aplique a pipe2.c

Use um desenho com dois tipos de seta separados.

**Arvore de criacao:**

```text
Pai original
├── Calculador (son1)
└── Coordenador (son5)
    ├── Fornecedor de -b
    ├── Fornecedor de 2a
    └── Fornecedor de delta
```

**Fluxo de dados:**

```text
-b    -- canais[0] --> calculador
2a    -- canais[1] --> calculador
delta -- canais[2] --> calculador
calculador -- canais[3]: x1 --> pai
calculador -- canais[4]: x2 --> pai
```

São seis processos e cinco pipes. A versão atual recebe a entrada no pai antes dos forks. Os valores de entrada são herdados; os fornecedores enviam os componentes explicitamente ao calculador, que envia os resultados ao pai. O exemplo foi organizado para ilustrar IPC, não para acelerar esse cálculo pequeno.

Projete só `main` (criação dos pipes e dos dois filhos), o laço de `fornecedores` e as três recepções de `calculador`. Explique os helpers por responsabilidade: fechar pontas, transferir bytes e coletar filhos. Não tente explicar todos os detalhes em dez minutos.

Execute:

```bash
make run-pipe
```

Digite `1 -3 1` para **a, b e delta**. Delta já está calculado; não é c. No quadro: `-b=3`, `2a=2`, `sqrt(delta)=1`; raízes `(3-1)/2=1` e `(3+1)/2=2`.

Peça à turma quem escreve e quem lê cada canal. Opcional: entrada `1 -2 0` mostra raiz dupla; não gaste tempo da prática com validações matemáticas.

## 75–87 min — prática

Distribua o esqueleto. Os alunos completam as seis lacunas para o filho calcular `10+20`, enviar ao pipe e o pai receber. O código auxiliar de transferência e espera já está pronto.

Sugestão de condução: 2 minutos para explicar a tarefa, 7 para implementar em duplas, 3 para comparar com o gabarito. Se houver uma máquina por dupla, ambos devem explicar uma ponta do canal.

Peça que compilem na raiz:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia05/pipe_soma_atividade.c -o /tmp/pipe-soma-aluno
/tmp/pipe-soma-aluno
```

Antes de preencher, o esqueleto propositalmente não compila. Se não houver laboratório, peça que escrevam as seis respostas e desenhem o canal no papel.

Avalie três coisas: pontas corretas, tamanho correto do dado e explicação de por que uma escrita numa variável local não bastaria.

## 87–90 min — fechamento

Peça o bilhete de saída individual: **“Por que waitpid não resolve a comunicação do resultado e pipe resolve?”**

Fala final sugerida: “fork cria processos com memórias independentes. waitpid coordena o término e coleta filhos. Pipe transporta dados entre eles. No dia 7 vamos estudar threads que compartilham memória e precisam controlar o acesso.”

## Sequência para projetar — dez telas

Use estes tópicos como ordem do material no projetor; não é necessário abrir tudo de uma vez. Os slides prontos seguem essa ordem: [aula05_processos_ipc.pdf](slides/aula05_processos_ipc.pdf), com fonte em [aula05_processos_ipc.tex](slides/aula05_processos_ipc.tex) (Beamer, tema metropolis). Há slides de apoio além das dez telas (memória do processo, herança no fork, buffers do printf, waitpid, zumbis e órfãos, tabela de descritores, escrita no pipe, glossário): eles servem para o aluno rever sozinho e podem ser passados rapidamente se o tempo apertar.

1. Pergunta central e objetivos.
2. Programa → duas instâncias com PIDs diferentes.
3. Uma chamada de fork e seus três retornos.
4. Laço: previsão 1→2→4→8.
5. Duas cópias do vetor e a soma zero no pai.
6. waitpid: término e coleta; IPC: transporte do resultado.
7. Canal filho→pai e fd[0]/fd[1].
8. Árvore de pipe2 e mapa dos cinco canais.
9. Prática: seis lacunas.
10. Bilhete de saída e ligação com a próxima aula.

## Ajustes de ritmo

Se estiver dez minutos atrasado, reduza pipe2 a três minutos de mapa e execução. Preserve a demonstração pequena e a prática. Se a turma tiver dificuldade com o laço, desenhe duas iterações antes da terceira.

Se sobrar tempo, discuta: “O pai deixar sua própria ponta de escrita aberta pode impedir EOF?” Sim, enquanto houver ponta de escrita aberta o pipe vazio não sinaliza EOF. Não demonstre um programa travado sem preparar o encerramento.

Se a compilação falhar no laboratório, use as saídas esperadas e a simulação no quadro. Não atribua comportamento imprevisível do código original à independência da memória.

## Referências de consulta

Material-base: PDF local e [guia completo do professor](../../../preparacao/GUIA_DO_PROFESSOR.md).

Documentação de [fork](https://man7.org/linux/man-pages/man2/fork.2.html), [waitpid](https://man7.org/linux/man-pages/man2/waitpid.2.html) e [pipes](https://man7.org/linux/man-pages/man7/pipe.7.html). As referências servem para preparação; a leitura completa não é pré-requisito para os alunos.
