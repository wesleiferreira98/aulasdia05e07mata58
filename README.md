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
| 7/10 | Threads, região crítica e sincronização | `peterson-code.c`, `prod_cons.c`, mailboxes Java |

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

`make test` compila C11 com avisos tratados como erros e verifica soma do pai, raízes, rejeição de entradas inválidas, contador protegido e entrega FIFO de 40 itens. A integração contínua repete os testes e compila os exemplos Java.

Peterson usa atomics com ordenação sequencialmente consistente para dois participantes e espera ocupada. É uma demonstração didática. Testes não constituem prova geral de correção concorrente.

Os exemplos Java foram preservados: a mailbox 3b tem corrida intencional; a 3s protege os métodos, mas ainda pode sobrescrever mensagem não consumida. Leia as limitações antes de apresentar.

## Colaboração e autoria

Leia [CONTRIBUTING.md](CONTRIBUTING.md) e [CHANGELOG.md](CHANGELOG.md). O material-base identifica Maycon Leone M. Peixoto como professor/autor; os ajustes desta pasta não alteram essa atribuição. Fontes e situação de licenciamento estão em [NOTICE.md](NOTICE.md). Ainda não foi definida uma licença de distribuição para o conjunto.
