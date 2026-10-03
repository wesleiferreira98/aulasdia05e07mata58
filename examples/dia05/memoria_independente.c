/* Aula de 5/10: o mesmo endereco virtual guarda valores diferentes. */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

int main(void)
{
    int x = 10;
    pid_t pid = fork();
    if (pid == -1) { perror("fork"); return EXIT_FAILURE; }
    if (pid == 0) {
        x = 99; /* Altera somente a copia do filho. */
        printf("Filho: x = %d em %p\n", x, (void *)&x);
        fflush(stdout); /* _exit nao esvazia buffers de stdio. */
        _exit(EXIT_SUCCESS);
    }
    int status;
    pid_t coletado;
    do { coletado = waitpid(pid, &status, 0); } while (coletado == -1 && errno == EINTR);
    if (coletado == -1) { perror("waitpid"); return EXIT_FAILURE; }
    printf("Pai:   x = %d em %p\n", x, (void *)&x);
    return EXIT_SUCCESS;
}
