# Atividade de 7/10/2026 · concorrência e sincronização

Nome(s): _____________________________________

## Parte 1: durante a simulação (em duplas)

Acompanhe a simulação "Produção de dinheiro" (buffer de 10 posições) e responda.

1. Com o produtor em 10 itens/s e o consumidor em 0,2, o buffer enche e aparece "Aguardando vaga". No `prod_cons.c`, em qual linha o produtor estaria dormindo? Quanto valeria `empty`?
2. Invertendo as velocidades, quem passa a esperar? Em qual semáforo? Quanto valeria `full`?
3. Com a simulação pausada, quanto dá "itens disponíveis" + "vagas livres"? Por que essa soma não muda?
4. Por que os indicadores W e R voltam para a posição 0 depois da posição 9?

## Parte 2: prática (em duplas)

Abra `examples/dia07/prod_cons_atividade.c`. Complete as seis lacunas:

- **1 e 2:** os valores iniciais de `empty` (vagas) e `full` (itens);
- **3 e 4:** qual semáforo o produtor espera antes de inserir, e qual ele avisa depois;
- **5 e 6:** qual semáforo o consumidor espera antes de retirar, e qual ele avisa depois.

O `mutex` já está pronto. Não mude a ordem das linhas.

Compile na raiz do repositório:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread
./prod-cons-aluno
```

Resultado esperado na última linha: `Fim: 40 itens produzidos e consumidos; buffer vazio.`

Se o programa **travar** (parar de imprimir sem terminar), aperte `Ctrl + C`: alguma thread ficou esperando um semáforo que ninguém vai liberar. Se aparecer `Assertion ... failed`, alguém inseriu sem vaga ou retirou sem item.

Explique:

5. Por que o produtor espera `empty` **antes** de pegar o `mutex`? O que poderia acontecer se pegasse o `mutex` primeiro?
6. Se o seu programa travou ou abortou em alguma tentativa, qual lacuna estava errada e por que isso causou aquele sintoma?

## Bilhete de saída (individual)

Em duas ou três frases: **qual problema o `mutex` resolve e qual problema `empty` e `full` resolvem?**
