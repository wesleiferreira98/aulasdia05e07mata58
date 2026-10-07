# Gabarito do professor: aula de 7/10

As questões 3 a 5 e 7 a 10 são as questões 1 a 7 do guia do professor (PDF *Aulas guiadas*, Aula 2). As respostas seguem o gabarito do guia, com complementos.

## Atividade rápida da teoria (oral)

N = 5, `empty = 5`, `full = 0`. Depois de dois itens produzidos e um consumido: **`empty = 4`, `full = 1`**. Reforce que `empty + full = N` sempre que nenhuma operação está em andamento. (No meio de uma operação, depois do `sem_wait` e antes do `sem_post`, a soma pode ficar menor por um instante.)

## Prática 1: seis lacunas

| Lacuna | Resposta | Por quê |
|---|---|---|
| 1 | `N` | No início, todas as posições são vagas livres. |
| 2 | `0` | No início, não há nenhum item. |
| 3 | `&empty` | O produtor precisa de uma vaga para inserir. |
| 4 | `&full` | O produtor acabou de criar um item. |
| 5 | `&full` | O consumidor precisa de um item para retirar. |
| 6 | `&empty` | O consumidor acabou de liberar uma vaga. |

Regra para a turma: **cada thread espera o que consome e avisa o que cria.**

A resposta comentada, com o motivo de cada lacuna e o que acontece nos erros, está em [prod_cons_resposta.c](../../../examples/dia07/prod_cons_resposta.c) (`make run-resposta-dia07`). O programa completo e comentado é o [prod_cons.c](../../../examples/dia07/prod_cons.c).

### Sintomas dos erros (todos testados)

| Erro do aluno | Sintoma |
|---|---|
| Lacuna 1 = `0` | Trava: o produtor espera vaga, o consumidor espera item, ninguém avança. |
| Lacuna 3 = `&full` | Trava: o produtor espera um item que só ele criaria. |
| Lacuna 4 = `&empty` | `Assertion 'count < N' failed`: `empty` nunca diminui, o produtor insere com o buffer cheio. |
| Lacuna 2 = `N`, ou lacunas 1 e 2 trocadas | `Assertion 'count > 0' failed`: o consumidor retira de um buffer vazio. |
| Lacuna 6 = `&full` | `Assertion 'count > 0' failed`: `full` cresce sem haver item. |

Quando a saída vai para um arquivo ou para outro programa, a linha do `assert` pode aparecer sem as linhas "Produzido/Consumido" anteriores: o `abort` descarta o que estava no buffer do `printf`. No terminal, elas aparecem.

1. Se o produtor pegasse o `mutex` e depois dormisse em `empty` com o buffer cheio, o consumidor não conseguiria pegar o `mutex` para retirar e liberar uma vaga. Os dois ficariam esperando para sempre: **deadlock**. Esperando antes, o produtor dorme sem segurar a chave.
2. Resposta livre; use a tabela de sintomas acima para conferir a explicação.

## Prática 2: Mailbox 3b

3. O objeto `Mailbox3b`, especialmente o array `message` e a variável `youHaveMail`, é compartilhado pelos dois produtores e pelo consumidor.
4. As operações que escrevem e leem o estado da mailbox são as regiões críticas. Em especial, o laço de `storeMessage()` que escreve o array `message` letra por letra pode ser intercalado entre os produtores. `retrieveMessage()` também é, porque lê `message` e `youHaveMail` enquanto um produtor pode estar escrevendo.
5. Porque o atraso entre as escritas das letras aumenta a janela em que outra thread pode ser escalonada e interferir na mensagem que ainda está sendo montada. Em teste, com o valor original 7 cerca de metade das mensagens chegou misturada; com `MAXPROCESSTIME = 50`, a maioria (cerca de 3 em 4).

## Prática 3: Mailbox 3s

6. Deixam de aparecer:
   - **mensagens misturadas**, porque `storeMessage()` é `synchronized`: um produtor só começa a escrever depois que o outro terminou e devolveu a chave do objeto;
   - **"How sad, no mail .."**, porque `retrieveMessage()` faz `while (youHaveMail == false) wait();`: o consumidor dorme até um `notify()` em vez de voltar de mãos vazias.

   Aceite também a observação de que agora aparece "Looking for my mail .." antes de cada mensagem: o consumidor fica parado depois dessa linha até chegar correspondência.

## Atividade final

7. O monitor associa a exclusão mútua às operações sobre o estado compartilhado, encapsulando dados e sincronização no mesmo objeto. Quem usa o objeto não precisa lembrar de pegar e devolver a chave.
8. `synchronized` exige que a thread adquira o lock (monitor) do objeto antes de executar o método. Enquanto uma thread está dentro de um método sincronizado do objeto, outra não executa ao mesmo tempo nenhum trecho protegido pelo mesmo monitor. Na 3s, isso impede que dois produtores escrevam `message` ao mesmo tempo e que o consumidor leia uma mensagem pela metade.
9. A condição precisa ser verificada de novo quando a thread acorda. Quem acorda ainda disputa a chave; enquanto isso, outra thread pode mudar o estado. O Java também permite despertares espúrios. O `while` garante que a thread só prossiga quando a condição for realmente verdadeira.
10. Na espera ocupada, a thread continua executando testes e consumindo CPU, como no `while` de Peterson. Com `wait()`, ela entra em estado de espera, **libera o monitor** e não consome CPU até ser notificada.

## Critério de correção sugerido (10 pontos)

| Evidência | Pontos |
|---|---:|
| Seis lacunas corretas, programa termina com 40 itens | 3 |
| Questão 1: ordem dos `sem_wait` e deadlock | 1 |
| Questão 2: relação entre erro e sintoma | 1 |
| Questões 3 a 5: recurso, região crítica e `MAXPROCESSTIME` | 1,5 |
| Questão 6: comparação 3b × 3s ligada ao código | 0,5 |
| Questões 7 a 10: monitores (0,75 cada) | 3 |

Pode ser usada como checagem formativa sem nota. Não desconte por dificuldades no comando de compilação se as lacunas e a explicação estiverem corretas.

## Semáforos × monitores (síntese do guia)

| Aspecto | Semáforo | Monitor |
|---|---|---|
| Nível | Primitiva explícita de sincronização | Abstração de nível mais alto |
| Exclusão mútua | O programador organiza `wait`/`post` | Associada às operações sincronizadas do objeto |
| Espera por condição | Modelada com semáforos adicionais | `wait()`/`notify()`/`notifyAll()` |
| Risco didático | Ordem incorreta das operações | Ainda exige uso correto das condições e dos locks |

## Equívocos e intervenções

- **"O mutex conta as vagas."** Separe chave (quem pode mexer agora) de contagem (quanto há para mexer).
- **"Tanto faz a ordem dos `sem_wait`."** Construa o deadlock no quadro: consumidor com a chave, esperando item.
- **"Semáforo é igual a Peterson."** Em Peterson quem espera gira e gasta CPU; no semáforo quem espera dorme.
- **"`volatile` resolve a corrida."** Não resolve: só impede algumas otimizações. É preciso exclusão mútua.
- **"`notify` entrega a chave na hora."** Não: quem acorda ainda precisa readquirir o lock, e por isso confere a condição de novo com `while`.
- **"A mailbox 3s entrega todas as mensagens."** Não: o produtor pode sobrescrever uma mensagem ainda não lida. Ela garante integridade, não entrega. A versão sem perdas é a [CaixaDeCorreio](../../../examples/dia07/caixa/CaixaDeCorreio.java).
- **"`sleep` libera o monitor."** Não: só `wait` libera o monitor do objeto. Na 3s, o produtor dorme entre as letras **segurando** a chave, e é por isso que ninguém entra no meio.
