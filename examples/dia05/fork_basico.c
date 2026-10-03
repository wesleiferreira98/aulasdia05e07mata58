/* Aula de 5/10: os dois retornos de fork e a coleta do filho. */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

int main(void)
{
    printf("Antes do fork: PID %d\n", (int)getpid());
    fflush(stdout); /* Evita que o filho herde texto ainda no buffer. */

    pid_t pid = fork();
    if (pid == -1) { perror("fork"); return EXIT_FAILURE; }
    if (pid == 0) {
        printf("Filho: fork retornou 0; meu PID %d; meu pai %d\n",
               (int)getpid(), (int)getppid());
        fflush(stdout); /* _exit nao esvazia buffers de stdio. */
        _exit(EXIT_SUCCESS);
    }
    printf("Pai: fork retornou %d; meu PID %d\n", (int)pid, (int)getpid());

    int status;
    pid_t coletado;
    do { coletado = waitpid(pid, &status, 0); } while (coletado == -1 && errno == EINTR);
    if (coletado == -1) { perror("waitpid"); return EXIT_FAILURE; }
    if (WIFEXITED(status))
        printf("Pai: filho %d terminou com codigo %d\n", (int)coletado, WEXITSTATUS(status));
    return EXIT_SUCCESS;
}
