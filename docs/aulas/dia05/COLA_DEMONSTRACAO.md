# Cola de comandos: demonstrações da aula de 5/10

Todos os comandos são executados **na raiz do repositório**. Os PIDs mudam a cada execução; o resto da saída deve ser igual.

## Antes da aula (em casa ou ao chegar no laboratório)

```bash
cd ~/Downloads/aulasdia05e07mata58
rm -rf build     # apaga executáveis antigos e começa do zero
make test        # compila tudo e roda 8 testes
```

Esperado no final: `Ran 8 tests ... OK`. Se passou, todas as demonstrações abaixo vão funcionar.

Deixe dois terminais abertos na raiz do repositório: um para mostrar o código, outro para executar.

## 1. Retornos de fork (8 a 20 min, opcional)

```bash
make run-fork-basico
```

Esperado:

```text
./build/fork-basico
Antes do fork: PID 4210
Filho: fork retornou 0; meu PID 4211; meu pai 4210
Pai: fork retornou 4211; meu PID 4210
Pai: filho 4211 terminou com codigo 0
```

Aponte: o número que o pai recebe é o PID do filho; o `meu pai` do filho é o PID do pai.

Para mostrar que a ordem entre pai e filho pode variar, rode algumas vezes:

```bash
for i in 1 2 3 4 5; do ./build/fork-basico; echo; done
```

A ordem pode até sair igual nas cinco vezes. Se isso acontecer, diga: "sair igual não garante que seja sempre igual; nada no código impõe essa ordem".

## 2. fork2.c: a soma no pai (33 a 45 min)

Mostre o código antes de rodar:

```bash
less fork2.c            # q para sair
```

Peça a previsão (0, 18 ou outro valor?). Depois:

```bash
make run-fork
```

Esperado:

```text
./build/fork2
Soma total no pai: 0
```

## 3. Mesmo endereço, valores diferentes (opcional, se perguntarem sobre ponteiros)

```bash
make run-memoria
```

Esperado (o endereço muda a cada execução, mas é **igual nas duas linhas**):

```text
./build/memoria-independente
Filho: x = 99 em 0x7ffe72feea88
Pai:   x = 10 em 0x7ffe72feea88
```

## 4. Pipe pequeno (53 a 65 min)

```bash
less examples/dia05/pipe_soma.c
make run-pipe-soma
```

Esperado:

```text
./build/pipe-soma
Resultado recebido: 30
```

## 5. pipe2.c: seis processos, cinco pipes (65 a 75 min)

```bash
make run-pipe
```

O programa **fica esperando entrada**. Digite os três números e aperte Enter:

```text
./build/pipe2
>>> Digite tres numeros separados por espaco e tecle Enter: a b delta
>>> Exemplo: 1 -3 1   (delta ja calculado; o programa nao pede c)
a b delta: 1 -3 1
Raizes: x1 = 1; x2 = 2
```

Lembre a turma: a entrada é **a, b e delta**, não a, b e c.

Opcional, raiz dupla:

```bash
make run-pipe           # digite: 1 -2 0
```

Esperado: `Raizes: x1 = 1; x2 = 1`

Se quiser rodar sem digitar:

```bash
echo "1 -3 1" | ./build/pipe2
```

## 6. Prática dos alunos (75 a 87 min)

Comando que os alunos usam:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror examples/dia05/pipe_soma_atividade.c -o /tmp/pipe-soma-aluno
/tmp/pipe-soma-aluno
```

Antes de preencher as lacunas, **o gcc dá erro**. É proposital.

Para mostrar a solução no final, comparando o esqueleto com o exemplo completo:

```bash
diff examples/dia05/pipe_soma_atividade.c examples/dia05/pipe_soma.c
```

As linhas com `<` são as lacunas; as linhas com `>` são as respostas.

## Plano B: sem make

Se o `make` não existir na máquina, compile direto com o gcc:

```bash
mkdir -p build
gcc -std=c11 -Wall -Wextra fork2.c -o build/fork2
gcc -std=c11 -Wall -Wextra pipe2.c -o build/pipe2 -lm
gcc -std=c11 -Wall -Wextra examples/dia05/pipe_soma.c -o build/pipe-soma
gcc -std=c11 -Wall -Wextra examples/dia05/fork_basico.c -o build/fork-basico
gcc -std=c11 -Wall -Wextra examples/dia05/memoria_independente.c -o build/memoria-independente
```

E execute com `./build/fork2`, `./build/pipe-soma`, `./build/pipe2`, e assim por diante.

O `-lm` no `pipe2.c` é obrigatório (biblioteca matemática, por causa do `sqrt`). Sem ele aparece `undefined reference to sqrt`.

## Se algo der errado

| Problema                         | O que fazer                                                                             |
| -------------------------------- | --------------------------------------------------------------------------------------- |
| `make: command not found`      | Use o plano B.                                                                          |
| `gcc: command not found`       | Sem compilador na máquina: mostre as saídas esperadas desta cola e siga com o quadro. |
| `undefined reference to sqrt`  | Faltou`-lm` no final do comando do `pipe2.c`.                                       |
| `pipe2` parado, sem fazer nada | Ele está esperando a entrada. Digite`1 -3 1` e Enter.                                |
| Um programa travou               | `Ctrl + C` encerra o programa em primeiro plano.                                      |
| `No such file or directory`    | Você não está na raiz do repositório:`cd ~/Downloads/aulasdia05e07mata58`.        |
| Projetor com letra pequena       | `Ctrl + Shift + +` aumenta a fonte na maioria dos terminais.                          |

## Resumo em uma linha por demonstração

```bash
make run-fork-basico     # retornos de fork (opcional)
make run-fork            # Soma total no pai: 0
make run-memoria         # mesmo endereço, x = 99 e x = 10 (opcional)
make run-pipe-soma       # Resultado recebido: 30
make run-pipe            # digite 1 -3 1  ->  x1 = 1; x2 = 2
```
