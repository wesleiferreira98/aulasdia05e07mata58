/* Aula de SO: todos os processos continuam o laco de fork. */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

int main(void)
{
    const int tamanho = 3;
    int *v = calloc((size_t)tamanho, sizeof *v);
    pid_t original = getpid();
    int falhou = 0;
    if (v == NULL) { perror("calloc"); return EXIT_FAILURE; }

    for (int i = 0; i < tamanho; ++i) {
        pid_t pid = fork();
        if (pid == -1) { perror("fork"); falhou = 1; break; }
        if (pid == 0) v[i] = i + 5; /* Altera apenas a copia do filho. */
    }
    /* Cada processo coleta seus proprios filhos diretos. */
    for (;;) {
        int status;
        pid_t pid = waitpid(-1, &status, 0);
        if (pid > 0) {
            if (!WIFEXITED(status) || WEXITSTATUS(status) != 0) falhou = 1;
        } else if (errno == EINTR) continue;
        else if (errno == ECHILD) break;
        else { perror("waitpid"); falhou = 1; break; }
    }
    if (getpid() == original) {
        int soma = 0;
        for (int i = 0; i < tamanho; ++i) soma += v[i];
        printf("Soma total no pai: %d\n", soma);
    }
    free(v);
    return falhou ? EXIT_FAILURE : EXIT_SUCCESS;
}
