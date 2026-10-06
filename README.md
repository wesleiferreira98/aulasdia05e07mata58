# Laboratório de Sistemas Operacionais — MATA58

Material de estudo e demonstração para duas aulas de 90 minutos sobre **processos, comunicação e sincronização**. Exemplos em C revisados, exemplos Java para comparação e roteiro do professor em português.

Encontros previstos: **5 e 7 de outubro de 2026**.

## Comece aqui

Em Linux, com GCC, Make e Python 3 instalados:

```bash
make
make test
make run-fork
make run-pipe
make run-peterson
make run-prod-cons
```

Em `run-pipe`, digite `1 -3 1` (a, b e delta). O resultado será `x1 = 1; x2 = 2`. Delta é informado já calculado: o programa não pede c.

Os programas C terminam sozinhos. Saídas e explicações: [guia dos exemplos](docs/EXEMPLOS.md).

## Aulas

| Encontro | Tema | Exemplos |
|---|---|---|
| 5/10 | Processos, memória e IPC | `fork2.c`, `pipe2.c` |
| 7/10 | Threads, região crítica e sincronização | `corrida.c`, `peterson-code.c`, `prod_cons.c`, mailboxes Java, simulação |

[Guia completo do professor](preparacao/GUIA_DO_PROFESSOR.md): roteiro com tempos, explicações, perguntas e gabaritos. [Preparação do laboratório](docs/LABORATORIO.md): requisitos e comandos Java.

## Estrutura

```text
.
├── fork2.c, pipe2.c              # Processos e comunicação
├── peterson-code.c, prod_cons.c  # Sincronização entre threads
├── Makefile                     # Compilação, execução e testes
├── docs/                        # Documentação do laboratório e exemplos
├── tests/                       # Verificação de comportamento dos exemplos C
├── preparacao/                  # Guia e versões C originais
├── Monitor-.../Monitor/          # Material e exemplos Java fornecidos
├── Aulas_Guiadas_...pdf          # Roteiro-base fornecido
└── .github/                     # CI e modelos de colaboração
```

Os nomes e caminhos do material fornecido foram preservados para manter as referências do PDF. Os executáveis novos ficam em `build/`, ignorado pelo Git. Arquivos `.class` fornecidos e o ZIP redundante também são ignorados; compile Java a partir dos fontes.

## Verificação e limites

`make test` compila C11 com avisos tratados como erros e verifica os retornos de fork, o mesmo endereço com valores diferentes, a soma do pai, o valor enviado pelo pipe pequeno, as raízes, rejeição de entradas inválidas, contador protegido e entrega FIFO de 40 itens. A integração contínua repete os testes e compila os exemplos Java.

Peterson usa atomics com ordenação sequencialmente consistente para dois participantes e espera ocupada. É uma demonstração didática. Testes não constituem prova geral de correção concorrente.

Os exemplos Java foram preservados: a mailbox 3b tem corrida intencional; a 3s protege os métodos, mas ainda pode sobrescrever mensagem não consumida. Leia as limitações antes de apresentar.

## Colaboração e autoria

Leia [CONTRIBUTING.md](CONTRIBUTING.md) e [CHANGELOG.md](CHANGELOG.md). O material-base identifica Maycon Leone M. Peixoto como professor/autor; os ajustes desta pasta não alteram essa atribuição. Fontes e situação de licenciamento estão em [NOTICE.md](NOTICE.md). Ainda não foi definida uma licença de distribuição para o conjunto.

## Simulação visual de produtor–consumidor

[Produção de dinheiro](pc_trabalho04_202011393/README.md) moderniza o trabalho de graduação de Weslei: dez posições, velocidades independentes, pausa segura e indicação de espera por vagas ou itens.

```bash
make test-simulation
make run-simulation
```

A janela requer JDK 25+ e Maven; o teste do modelo não precisa de JavaFX. Use a simulação no encontro de 7/10 antes de ler o protocolo de semáforos em C.

## Aula de 5/10 pronta para aplicação

[Roteiro de 90 minutos](docs/aulas/dia05/ROTEIRO.md), [atividade dos alunos](docs/aulas/dia05/ATIVIDADE.md) e [gabarito](docs/aulas/dia05/GABARITO.md). Comece com fork e memória independente; apresente um pipe pequeno antes da leitura de Bhaskara.

```bash
make run-pipe-soma
```

## Aula de 7/10 pronta para aplicação

Duas partes: teoria nos slides (corrida, região crítica, Peterson, semáforos, produtor-consumidor, monitores) e demonstração visual com a simulação, seguidas de prática com seis lacunas no produtor-consumidor. [Roteiro](docs/aulas/dia07/ROTEIRO.md), [atividade](docs/aulas/dia07/ATIVIDADE.md), [gabarito](docs/aulas/dia07/GABARITO.md), [cola de demonstração](docs/aulas/dia07/COLA_DEMONSTRACAO.md), [slides](docs/aulas/dia07/slides/aula07_concorrencia.pdf) e [apostila](docs/aulas/dia07/apostila/apostila07_concorrencia.pdf).

```bash
make run-corrida          # condição de corrida: contador sai menor que o esperado
make run-simulation       # parte 2: Produção de dinheiro
make run-resposta-dia07   # resposta comentada da prática
```
