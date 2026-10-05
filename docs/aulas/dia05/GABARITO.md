# Gabarito do professor: aula de 5/10

## Questões

1. Oito processos ao todo, sete novos: 1→2→4→8 caminhos, porque os filhos também continuam o laço. Não exige que todos fiquem vivos simultaneamente.
2. Filho: zero; pai: PID do filho; falha: -1. Após uma chamada bem-sucedida, ambos continuam depois da chamada.
3. Zero. calloc inicializou o vetor; somente filhos entram no ramo pid==0. Cada processo altera sua própria memória, então o pai permanece com três zeros.
4. Não. waitpid permite esperar/coletar o filho; não faz transferência do vetor nem estabelece memória compartilhada.
5. Para ambos herdarem descritores que referenciam o mesmo canal criado previamente.
6. Não precisa haver erro: uma leitura bloqueante num pipe vazio pode esperar se existe uma ponta de escrita aberta. Se estiver vazio e todas as pontas de escrita fecharem, read retorna zero (EOF). Havendo dados, pode receber menos bytes que o solicitado.
7. O filho só escreve e o pai só lê nesse protocolo. Fechar descritores não usados evita referências desnecessárias e permite sinalizar corretamente EOF quando todas as escritas terminarem.

## Seis lacunas

1. `fd[0]`
2. `10 + 20`
3. `transferir(fd[1], &valor, sizeof valor, 1)`
4. `fd[1]`
5. `transferir(fd[0], &valor, sizeof valor, 0)`
6. `fd[0]`

A resposta comentada, lacuna por lacuna, com o motivo de cada resposta e o que acontece nos erros comuns, está em [pipe_soma_atividade_resposta.c](../../../examples/dia05/pipe_soma_atividade_resposta.c) (`make run-resposta`). O programa completo está em [pipe_soma.c](../../../examples/dia05/pipe_soma.c). O helper contém read e write; explique as operações antes de distribuir a atividade. O exercício foca o protocolo, não a implementação de I/O completo.

## Critério de correção sugerido (10 pontos)

| Evidência | Pontos |
|---|---:|
| Árvore e contagem corretas | 2 |
| Retornos de fork e continuação | 1 |
| Memória independente e distinção de waitpid | 2 |
| Pontas e transferência corretas na prática | 3 |
| Explicação de herança e bloqueio/EOF | 1 |
| Bilhete de saída coerente | 1 |

Pode ser usada como checagem formativa sem nota. Não descontar por dificuldades no comando de compilação se o protocolo e a explicação estiverem corretos.

## Equívocos e intervenções

- **“O pai soma 18.”** Pergunte em qual processo cada atribuição acontece. Desenhe memórias separadas.
- **“São quatro processos.”** Peça ao aluno seguir a próxima iteração como filho, sem sair do laço.
- **“waitpid compartilha memória.”** Separe espera pelo término de transporte dos bytes.
- **“read sempre recebe tudo.”** Explique que o retorno informa quantos bytes chegaram.
- **“O pipe vazio sempre dá EOF.”** Pergunte se alguma ponta de escrita ainda está aberta.
- **“O mesmo endereço implica memória compartilhada.”** Explique que endereço virtual é interpretado dentro do espaço do processo.
