# Cola de comandos: aula de 7/10

Todos os comandos são executados **na raiz do repositório**.

## Antes da aula

```bash
cd ~/Downloads/aulasdia05e07mata58
rm -rf build
make test               # tem que terminar com: Ran 11 tests ... OK
make java               # compila as mailboxes 3b e 3s
make run-simulation     # abre a janela; feche depois de conferir
```

**Use o seu notebook para a simulação.** Nele, o JavaFX já está baixado e a janela abre sem internet. Num computador novo, a primeira execução do `make run-simulation` baixa o JavaFX e **precisa de rede**.

Nos computadores dos alunos, confira `gcc --version` e `javac -version`.

Deixe abertos: os slides, a janela da simulação (minimizada) e um terminal na raiz.

## Parte 1: teoria (0 a 45 min)

Só slides e quadro. Se quiser mostrar a corrida ao vivo (opcional):

```bash
gcc -Wall -Wextra examples/dia07/corrida.c -o corrida -pthread
./corrida
```

Esperado: algo como `Contador: 1038289 (esperado: 2000000)`. O número muda a cada execução, e é isso que você quer mostrar.

### Ver funcionando: simulação (35 a 40 min, só demonstração)

```bash
make run-simulation
```

| Produtor | Consumidor | O que aparece | O que dizer |
|---|---|---|---|
| **10** | **0,2** | Buffer cheio; **"Produtor: Aguardando vaga: buffer cheio"** | "Dormindo em `sem_wait(&empty)`, com `empty = 0`." |
| **0,2** | **10** | Buffer vazio; **"Consumidor: Aguardando item: buffer vazio"** | "Dormindo em `sem_wait(&full)`, com `full = 0`." |

Na tela: 10 posições = `N`; W = `hi` (próxima escrita); R = `lo` (próxima leitura); "itens disponíveis" = `full`; "vagas livres" = `empty`.

Se não abrir: pule. Não há atividade sobre a simulação.

## Parte 2: prática (45 a 90 min)

### Prática 1: seis lacunas (45 a 58 min)

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread
./prod-cons-aluno
```

Antes de preencher as lacunas, **o gcc dá erro**. É proposital.

Correto, na última linha: `Fim: 40 itens produzidos e consumidos; buffer vazio.`

| Sintoma | Lacuna provavelmente errada |
|---|---|
| Trava sem imprimir nada (`Ctrl + C` para sair) | 1 (`empty` em 0) ou 3 (produtor esperando `full`) |
| `Assertion 'count < N' failed` | 4 (produtor avisando `empty`) |
| `Assertion 'count > 0' failed` | 2 (`full` em N), 1 e 2 trocadas, ou 6 (consumidor avisando `full`) |

Dica para dar: **cada thread espera o que consome e avisa o que cria.**

Para mostrar a solução: `make run-resposta-dia07`.

### Prática 2: Mailbox 3b (58 a 68 min)

```bash
javac -d build/java-3b Monitor-20261002T192224Z-1-001/Monitor/1/*.java
timeout --foreground 5 java -cp build/java-3b ThreadSync3b | grep "My name"
```

Esperado (varia): mensagens misturadas, por exemplo `Dave. I say, Helloog!`, `Bill. I say, Hotlo, world.`, `Dave. I say, Hot dog!`. Em teste, cerca de metade chegou errada.

Sem o `| grep "My name"`, aparecem dezenas de `How sad, no mail ..`: o consumidor consulta a caixa vazia a cada 20 ms.

**Não tire o `--foreground`** enquanto houver `| grep`: sem ele, num terminal interativo, o `timeout` derruba o `grep` junto e a tela fica vazia.

Se sobrar tempo: mude `MAXPROCESSTIME = 7` para `50` em `Monitor-.../Monitor/1/Mailbox3b.java`, recompile e rode de novo. A maioria das mensagens passa a sair misturada. **Desfaça a mudança depois** (`git checkout -- Monitor-20261002T192224Z-1-001`).

### Prática 3: Mailbox 3s (68 a 80 min)

```bash
javac -d build/java-3s Monitor-20261002T192224Z-1-001/Monitor/3/*.java
timeout 5 java -cp build/java-3s ThreadSync3s
```

Esperado: `Looking for my mail ..` seguido de uma mensagem íntegra, alternando. Nenhuma mistura e nenhum `How sad, no mail`.

Versão sem perdas, se sobrar tempo: `make run-caixa`. Ela confere se as 40000 mensagens chegaram.

### Atividade final (80 a 90 min)

Questões 7 a 10, individuais, no papel. Nada para rodar.

## Se algo der errado

| Problema | O que fazer |
|---|---|
| A simulação não abre | Pule: são só 5 minutos e não há atividade sobre ela. |
| `javac: command not found` | Sem JDK no computador do aluno: rode as mailboxes no projetor e faça as questões em duplas. |
| Mailbox 3b não mostra nada com `grep` | Faltou o `--foreground` no `timeout`. |
| `undefined reference to pthread_create` ou `sem_wait` | Faltou `-pthread` no final do comando do gcc. |
| Programa da prática travou | Esperado em alguns erros: `Ctrl + C` e confira a tabela de diagnóstico. |
| `No such file or directory` | Você não está na raiz: `cd ~/Downloads/aulasdia05e07mata58`. |

## Resumo em uma linha por momento

```bash
make test && make java                                                       # antes da aula
make run-simulation                                                          # 35 min: só demonstração
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread && ./prod-cons-aluno   # prática 1
javac -d build/java-3b Monitor-20261002T192224Z-1-001/Monitor/1/*.java && timeout --foreground 5 java -cp build/java-3b ThreadSync3b | grep "My name"   # prática 2
javac -d build/java-3s Monitor-20261002T192224Z-1-001/Monitor/3/*.java && timeout 5 java -cp build/java-3s ThreadSync3s   # prática 3
```
