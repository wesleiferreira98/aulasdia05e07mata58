# Preparação do laboratório

## Requisitos

Linux com GCC compatível com C11, GNU Make, Python 3 e, para Java, JDK com javac e java. Os exemplos C usam APIs POSIX. Em Windows, utilize um ambiente Linux, como WSL. Este projeto não configura automaticamente esse ambiente.

Confira:

```bash
gcc --version
make --version
python3 --version
javac -version
java -version
```

No Debian/Ubuntu, instalação opcional dos requisitos:

```bash
sudo apt update
sudo apt install build-essential python3 default-jdk
```

Em outras distribuições, use o gerenciador correspondente. Ter java instalado não garante que javac esteja disponível: é necessário o JDK.

## C

Na raiz do projeto, execute `make test`. Os executáveis ficam em build. Use os alvos run documentados no README.

Equivalentes individuais:

```bash
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror -O2 fork2.c -o /tmp/fork2
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror -O2 pipe2.c -o /tmp/pipe2 -lm
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror -O2 examples/dia07/peterson-code.c -o /tmp/peterson -pthread
gcc -std=c11 -Wall -Wextra -Wpedantic -Werror -O2 examples/dia07/prod_cons.c -o /tmp/prod_cons -pthread
```

## Java

```bash
make java
make run-java-3b
# Ctrl+C para encerrar, depois:
make run-java-3s
```

A compilação grava novos .class em build, sem substituir os arquivos fornecidos. A CI compila Java, mas não executa seus laços infinitos nem testa a ocorrência visual da corrida.

## Antes de projetar

Ensaie os exemplos na máquina da aula, aumente a fonte do terminal e peça aos alunos uma previsão antes de executar. Use o [roteiro de 90 minutos](../preparacao/GUIA_DO_PROFESSOR.md). Não compile as versões em preparacao/originais como soluções corrigidas.
