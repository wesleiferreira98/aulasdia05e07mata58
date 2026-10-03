/* Aula de 5/10: um filho envia um inteiro ao pai pelo pipe. */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

/* modo=1 envia; modo=0 recebe. Trata transferencias parciais e EINTR. */
static int transferir(int fd, void *dado, size_t tamanho, int modo)
{
    unsigned char *p = dado;
    while (tamanho > 0) {
        ssize_t n = modo ? write(fd, p, tamanho) : read(fd, p, tamanho);
        if (n < 0 && errno == EINTR) continue;
        if (n < 0) { perror(modo ? "write" : "read"); return -1; }
        if (n == 0) {
            fprintf(stderr, "Transferencia incompleta: nenhum byte recebido/enviado.\n");
            return -1;
        }
        p += n;
        tamanho -= (size_t)n;
    }
    return 0;
}

int main(void)
{
    int fd[2], valor;
    if (pipe(fd) == -1) { perror("pipe"); return EXIT_FAILURE; }
    pid_t pid = fork();
    if (pid == -1) {
        perror("fork"); close(fd[0]); close(fd[1]); return EXIT_FAILURE;
    }
    if (pid == 0) {
        close(fd[0]);
        valor = 10 + 20;
        int resultado = transferir(fd[1], &valor, sizeof valor, 1);
        close(fd[1]);
        _exit(resultado == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
    }
    close(fd[1]);
    int resultado = transferir(fd[0], &valor, sizeof valor, 0);
    close(fd[0]);
    int status;
    pid_t coletado;
    do { coletado = waitpid(pid, &status, 0); } while (coletado == -1 && errno == EINTR);
    if (coletado == -1) { perror("waitpid"); return EXIT_FAILURE; }
    if (resultado != 0 || !WIFEXITED(status) || WEXITSTATUS(status) != 0)
        return EXIT_FAILURE;
    printf("Resultado recebido: %d\n", valor);
    return EXIT_SUCCESS;
}
