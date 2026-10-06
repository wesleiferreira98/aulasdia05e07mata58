# Atividade de 7/10/2026 · concorrência e sincronização

Nome(s): _____________________________________

Faça as práticas 1 a 3 em duplas e a atividade final individualmente. Execute todos os comandos **na raiz do repositório**.

## Prática 1: seis lacunas no produtor-consumidor (em C)

Abra `examples/dia07/prod_cons_atividade.c`. Complete as seis lacunas:

- **1 e 2:** os valores iniciais de `empty` (vagas) e `full` (itens);
- **3 e 4:** qual semáforo o produtor espera antes de inserir, e qual ele avisa depois;
- **5 e 6:** qual semáforo o consumidor espera antes de retirar, e qual ele avisa depois.

O `mutex` já está pronto. Não mude a ordem das linhas.

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread
./prod-cons-aluno
```

Resultado esperado na última linha: `Fim: 40 itens produzidos e consumidos; buffer vazio.`

Se o programa **travar** (parar de imprimir sem terminar), aperte `Ctrl + C`: alguma thread ficou esperando um semáforo que ninguém vai liberar. Se aparecer `Assertion ... failed`, alguém inseriu sem vaga ou retirou sem item.

1. Por que o produtor espera `empty` **antes** de pegar o `mutex`? O que poderia acontecer se pegasse o `mutex` primeiro?
________________________________________________________________________________
________________________________________________________________________________

2. Se o seu programa travou ou abortou em alguma tentativa, qual lacuna estava errada e por que isso causou aquele sintoma?
________________________________________________________________________________
________________________________________________________________________________

## Prática 2: Mailbox sem sincronização (Java, versão 3b)

Uma única `Mailbox3b` é compartilhada por um consumidor e dois produtores: Dave envia "Hello, world." e Bill envia "Hot dog!".

```bash
javac -d build/java-3b Monitor-20261002T192224Z-1-001/Monitor/1/*.java
timeout --foreground 5 java -cp build/java-3b ThreadSync3b | grep "My name"
```

O programa nunca termina sozinho: o `timeout` o encerra depois de 5 segundos (sem ele, use `Ctrl + C`). Rode também sem o `| grep "My name"` e observe as outras linhas. Abra `Mailbox3b.java` e leia `storeMessage()`.

3. Qual é o recurso compartilhado neste exemplo?
________________________________________________________________________________

4. Onde está a região crítica no `Mailbox3b`?
________________________________________________________________________________
________________________________________________________________________________

5. Por que aumentar `MAXPROCESSTIME` tende a tornar o problema mais visível?
________________________________________________________________________________
________________________________________________________________________________

## Prática 3: Mailbox com monitor (Java, versão 3s)

```bash
javac -d build/java-3s Monitor-20261002T192224Z-1-001/Monitor/3/*.java
timeout 5 java -cp build/java-3s ThreadSync3s
```

Abra `Mailbox3s.java` e compare com a 3b.

6. Compare a saída com a da versão 3b. O que deixou de aparecer? Indique qual parte do código da 3s causa cada mudança.
________________________________________________________________________________
________________________________________________________________________________

## Atividade final (individual)

7. O que um monitor acrescenta em relação à ideia de apenas compartilhar um objeto?
________________________________________________________________________________
________________________________________________________________________________

8. Qual é o papel de `synchronized` no exemplo `Mailbox3s`?
________________________________________________________________________________
________________________________________________________________________________

9. Por que `retrieveMessage()` usa `while` antes de `wait()` em vez de simplesmente continuar?
________________________________________________________________________________
________________________________________________________________________________

10. Qual a diferença conceitual entre espera ocupada e `wait()`?
________________________________________________________________________________
________________________________________________________________________________
