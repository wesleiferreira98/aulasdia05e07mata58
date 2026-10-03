# Atividade — 5/10/2026 · processos e IPC

Nome(s): _____________________________________

## Parte 1 — previsão e explicação (em duplas)

Considere fork2.c corrigido: vetor zerado com calloc, três iterações, todos os processos continuam o laço e somente o original imprime. Suponha que todas as criações tenham sucesso.

1. Quantos processos são criados ao todo, incluindo o original? Desenhe a evolução por iteração.
2. Quais valores de retorno de fork identificam o filho, o pai e uma falha? De onde pai e filho continuam a executar?
3. O filho altera v[i]. Qual soma o pai imprime? Explique o papel do espaço de memória de cada processo.
4. O pai espera os filhos terminarem. Isso faz o vetor dele receber as escritas dos filhos? Justifique.

## Parte 2 — prática (em duplas)

Abra `examples/dia05/pipe_soma_atividade.c`. Complete as seis lacunas para:

- O filho fechar sua ponta de leitura, calcular 10+20, enviar o inteiro e fechar sua ponta de escrita.
- O pai fechar sua ponta de escrita, receber o inteiro, imprimir e fechar sua ponta de leitura.

Use o helper transferir já fornecido; ele recebe descritor, endereço do dado, quantidade de bytes e modo (1 para enviar, 0 para receber).

Compile na raiz do repositório:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia05/pipe_soma_atividade.c -o /tmp/pipe-soma-aluno
/tmp/pipe-soma-aluno
```

Resultado esperado: `Resultado recebido: 30`.

Explique:

5. Por que criar o pipe antes do fork?
6. Se o pai executar read antes da escrita do filho, precisa haver erro? Quando pode esperar? Quando pode encontrar EOF?
7. Por que os dois processos fecham pontas diferentes?

## Bilhete de saída — individual

Em duas ou três frases: **por que waitpid não resolve a comunicação do resultado e pipe resolve?**
