# Gabarito do professor: aula de 7/10

## Parte 1: simulação

1. Em `wait_sem(&empty)`, o primeiro passo do laço do produtor, com `empty = 0`. Ele dorme sem segurar o `mutex`, e por isso o consumidor continua conseguindo retirar.
2. O consumidor passa a esperar, em `wait_sem(&full)`, com `full = 0`.
3. Dá 10, o tamanho do buffer. Cada inserção troca uma vaga por um item, e cada retirada troca um item por uma vaga: `empty + full = N` sempre que nenhuma operação está em andamento. (No meio de uma operação, depois do `sem_wait` e antes do `sem_post`, a soma pode ficar menor por um instante.)
4. Porque o índice avança com `(indice + 1) % N`: depois da posição 9 vem a 0. O buffer é circular e reaproveita as posições, mantendo a ordem FIFO.

## Parte 2: seis lacunas

| Lacuna | Resposta | Por quê |
|---|---|---|
| 1 | `N` | No início, todas as posições são vagas livres. |
| 2 | `0` | No início, não há nenhum item. |
| 3 | `&empty` | O produtor precisa de uma vaga para inserir. |
| 4 | `&full` | O produtor acabou de criar um item. |
| 5 | `&full` | O consumidor precisa de um item para retirar. |
| 6 | `&empty` | O consumidor acabou de liberar uma vaga. |

Regra para a turma: **cada thread espera o que consome e avisa o que cria.**

A resposta comentada, com o motivo de cada lacuna e o que acontece nos erros, está em [prod_cons_resposta.c](../../../examples/dia07/prod_cons_resposta.c) (`make run-resposta-dia07`). O programa completo e comentado é o [prod_cons.c](../../../prod_cons.c).

### Sintomas dos erros (todos testados)

| Erro do aluno | Sintoma |
|---|---|
| Lacuna 1 = `0` | Trava: o produtor espera vaga, o consumidor espera item, ninguém avança. |
| Lacuna 3 = `&full` | Trava: o produtor espera um item que só ele criaria. |
| Lacuna 4 = `&empty` | `Assertion 'count < N' failed`: `empty` nunca diminui, o produtor insere com o buffer cheio. |
| Lacuna 2 = `N`, ou lacunas 1 e 2 trocadas | `Assertion 'count > 0' failed`: o consumidor retira de um buffer vazio. |
| Lacuna 6 = `&full` | `Assertion 'count > 0' failed`: `full` cresce sem haver item. |

Quando a saída vai para um arquivo ou para outro programa, a linha do `assert` pode aparecer sem as linhas "Produzido/Consumido" anteriores: o `abort` descarta o que estava no buffer do `printf`. No terminal, elas aparecem.

### Perguntas

5. Se o produtor pegasse o `mutex` e depois dormisse em `empty` com o buffer cheio, o consumidor não conseguiria pegar o `mutex` para retirar e liberar uma vaga. Os dois ficariam esperando para sempre: **deadlock**. Esperando antes, o produtor dorme sem segurar a chave.
6. Resposta livre; use a tabela de sintomas acima para conferir a explicação.

### Bilhete de saída

Esperado: o `mutex` resolve a **exclusão mútua** (só uma thread mexe no buffer por vez, evitando a condição de corrida em `buffer`, `hi`, `lo` e `count`). `empty` e `full` resolvem a **disponibilidade** (o produtor só insere se houver vaga, o consumidor só retira se houver item, e quem não pode seguir dorme até ser avisado).

## Critério de correção sugerido (10 pontos)

| Evidência | Pontos |
|---|---:|
| Parte 1: espera e semáforo certos nas questões 1 e 2 | 2 |
| Parte 1: invariante e índice circular (3 e 4) | 1 |
| Seis lacunas corretas, programa termina com 40 itens | 4 |
| Questão 5: ordem dos `sem_wait` e deadlock | 1 |
| Questão 6: relação entre erro e sintoma | 1 |
| Bilhete de saída coerente | 1 |

Pode ser usada como checagem formativa sem nota. Não descontar por dificuldades no comando de compilação se as lacunas e a explicação estiverem corretas.

## Equívocos e intervenções

- **"O mutex conta as vagas."** Separe chave (quem pode mexer agora) de contagem (quanto há para mexer).
- **"Tanto faz a ordem dos `sem_wait`."** Construa o deadlock no quadro: consumidor com a chave, esperando item.
- **"Semáforo é igual a Peterson."** Em Peterson quem espera gira e gasta CPU; no semáforo quem espera dorme.
- **"`volatile` resolve a corrida."** Não resolve: só impede algumas otimizações. É preciso exclusão mútua.
- **"`notify` entrega a chave na hora."** Não: quem acorda ainda precisa readquirir o lock, e por isso confere a condição de novo com `while`.
- **"A mailbox 3s entrega todas as mensagens."** Não: o produtor pode sobrescrever uma mensagem ainda não lida.
- **"`sleep` libera o monitor."** Não: só `wait` libera o monitor do objeto.
