# Cola de comandos: aula de 7/10

Todos os comandos são executados **na raiz do repositório**.

## Antes da aula

```bash
cd ~/Downloads/aulasdia05e07mata58
rm -rf build
make test               # tem que terminar com: Ran 11 tests ... OK
make test-simulation    # tem que terminar com: OK: capacidade, FIFO, ...
make run-simulation     # abre a janela; feche depois de conferir
```

**Use o seu notebook para a simulação.** Nele, o JavaFX já está baixado e a janela abre sem internet. Num computador novo, a primeira execução do `make run-simulation` baixa o JavaFX e **precisa de rede**.

Deixe abertos: os slides, a janela da simulação (minimizada) e um terminal na raiz para a prática.

## Parte 1: teoria (só slides)

Nada para rodar. Se quiser mostrar a corrida ao vivo (opcional):

```bash
gcc -Wall -Wextra examples/dia07/corrida.c -o corrida -pthread
./corrida
```

Esperado: algo como `Contador: 1038289 (esperado: 2000000)`. O número muda a cada execução, e é isso que você quer mostrar.

## Parte 2: simulação (50 a 70 min)

```bash
make run-simulation
```

O que mostrar na tela antes de clicar:

| Na tela | No `prod_cons.c` |
|---|---|
| 10 posições | `N` |
| W (próxima escrita) | `hi` |
| R (próxima leitura) | `lo` |
| itens disponíveis | `full` |
| vagas livres | `empty` |
| linhas "Produtor:" e "Consumidor:" | quem está dormindo em qual semáforo |

### Os quatro experimentos

| # | Produtor | Consumidor | O que aparece | Pergunta e resposta |
|---|---|---|---|---|
| 1 | 2 itens/s | 2 itens/s | Buffer oscila com poucas notas | "Vai encher?" Não: os ritmos se equilibram. |
| 2 | **10** | **0,2** | Buffer cheio; **"Produtor: Aguardando vaga: buffer cheio"** | "Onde o produtor dorme?" Em `wait_sem(&empty)`, com `empty = 0`. |
| 3 | **0,2** | **10** | Buffer vazio; **"Consumidor: Aguardando item: buffer vazio"** | "Onde o consumidor dorme?" Em `wait_sem(&full)`, com `full = 0`. |
| 4 | 3 | 3 | W e R dão a volta; depois **Pausar** | "Por que W voltou ao 0?" `% N`. "Itens + vagas?" Sempre 10. |

Botões: **Iniciar** começa; **Pausar** congela tudo (vira **Continuar**); **Reiniciar** zera o buffer.

**Pergunte antes de mexer nos controles.** Peça a previsão e só depois mostre.

## Prática dos alunos (70 a 85 min)

Comando dos alunos:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread
./prod-cons-aluno
```

Antes de preencher as lacunas, **o gcc dá erro**. É proposital.

Correto, na última linha: `Fim: 40 itens produzidos e consumidos; buffer vazio.`

Diagnóstico quando der errado:

| Sintoma | Lacuna provavelmente errada |
|---|---|
| Trava sem imprimir nada (`Ctrl + C` para sair) | 1 (`empty` em 0) ou 3 (produtor esperando `full`) |
| `Assertion 'count < N' failed` | 4 (produtor avisando `empty`) |
| `Assertion 'count > 0' failed` | 2 (`full` em N), 1 e 2 trocadas, ou 6 (consumidor avisando `full`) |

Regra para dar como dica: **cada thread espera o que consome e avisa o que cria.**

Para mostrar a solução no final:

```bash
diff examples/dia07/prod_cons_atividade.c examples/dia07/prod_cons_resposta.c | grep "^>" | grep -v "^> *\*\|^> */\*"
```

Ou rode a resposta comentada:

```bash
make run-resposta-dia07
```

## Se algo der errado

| Problema | O que fazer |
|---|---|
| A simulação não abre | Siga no quadro com a tabela N = 3 dos slides (empty, full, count). |
| `mvn: command not found` | Sem Maven nesse computador: use o seu notebook ou o plano do quadro. |
| Erro de download do Maven | Sem internet: use o seu notebook, onde o JavaFX já está baixado. |
| `undefined reference to pthread_create` ou `sem_wait` | Faltou `-pthread` no final do comando do gcc. (No Linux atual costuma compilar mesmo sem ele, mas em sistemas mais antigos não; use sempre.) |
| Programa da prática travou | Esperado em alguns erros: `Ctrl + C` e confira a tabela de diagnóstico. |
| `No such file or directory` | Você não está na raiz: `cd ~/Downloads/aulasdia05e07mata58`. |

## Resumo em uma linha por momento

```bash
make test && make test-simulation      # antes da aula
make run-simulation                    # parte 2
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia07/prod_cons_atividade.c -o prod-cons-aluno -pthread && ./prod-cons-aluno   # prática
make run-resposta-dia07                # mostrar a solução
```
