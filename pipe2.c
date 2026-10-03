/* Aula de SO — exemplo de Maycon Leone M. Peixoto, revisado.
 * Seis processos, cinco pipes; somente o pai le a entrada do terminal.
 * Os pipes transportam doubles binarios entre processos da mesma maquina.
 */
#include <errno.h>
#include <math.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>

static int canais[5][2];

/* Mantem somente os descritores usados por este processo. */
static void fechar_exceto(int leitura1, int leitura2, int leitura3,
                          int escrita1, int escrita2)
{
    for (int i = 0; i < 5; ++i) {
        if (i != leitura1 && i != leitura2 && i != leitura3)
            close(canais[i][0]);
        if (i != escrita1 && i != escrita2) close(canais[i][1]);
    }
}

static int enviar(int fd, double valor)
{
    const unsigned char *p = (const unsigned char *)&valor;
    size_t restante = sizeof valor;
    while (restante > 0) {
        ssize_t n = write(fd, p, restante);
        if (n < 0 && errno == EINTR) continue;
        if (n <= 0) { perror("write"); return -1; }
        p += n;
        restante -= (size_t)n;
    }
    return 0;
}

static int receber(int fd, double *valor)
{
    unsigned char *p = (unsigned char *)valor;
    size_t restante = sizeof *valor;
    while (restante > 0) {
        ssize_t n = read(fd, p, restante);
        if (n < 0 && errno == EINTR) continue;
        if (n < 0) { perror("read"); return -1; }
        if (n == 0) {
            fprintf(stderr, "Pipe fechado antes de receber o valor completo.\n");
            return -1;
        }
        p += n;
        restante -= (size_t)n;
    }
    return 0;
}

/* Cada pai coleta todos os seus filhos, inclusive em caminhos de falha. */
static int aguardar_filhos(void)
{
    int falhou = 0;
    for (;;) {
        int status;
        pid_t pid = waitpid(-1, &status, 0);
        if (pid > 0) {
            if (!WIFEXITED(status) || WEXITSTATUS(status) != 0) falhou = 1;
        } else if (errno == EINTR) continue;
        else if (errno == ECHILD) break;
        else { perror("waitpid"); return -1; }
    }
    return falhou ? -1 : 0;
}

static void calculador(void)
{
    double menos_b, dois_a, delta, x1, x2;
    fechar_exceto(0, 1, 2, 3, 4);
    if (receber(canais[0][0], &menos_b) == -1 ||
        receber(canais[1][0], &dois_a) == -1 ||
        receber(canais[2][0], &delta) == -1) _exit(EXIT_FAILURE);
    x1 = (menos_b - sqrt(delta)) / dois_a;
    x2 = (menos_b + sqrt(delta)) / dois_a;
    if (!isfinite(x1) || !isfinite(x2)) {
        fprintf(stderr, "Raizes fora do intervalo numerico suportado.\n");
        _exit(EXIT_FAILURE);
    }
    int falhou = enviar(canais[3][1], x1) == -1;
    if (enviar(canais[4][1], x2) == -1) falhou = 1;
    fechar_exceto(-1, -1, -1, -1, -1);
    _exit(falhou ? EXIT_FAILURE : EXIT_SUCCESS);
}

static void fornecedores(double a, double b, double delta)
{
    /* Coordenador conserva temporariamente as tres pontas de escrita. */
    for (int i = 0; i < 5; ++i) {
        close(canais[i][0]);
        if (i >= 3) close(canais[i][1]);
    }
    double valores[3] = {-b, 2 * a, delta};
    int falhou = 0;
    for (int i = 0; i < 3; ++i) {
        pid_t pid = fork();
        if (pid == -1) { perror("fork fornecedor"); falhou = 1; break; }
        if (pid == 0) {
            for (int j = 0; j < 3; ++j)
                if (j != i) close(canais[j][1]);
            int resultado = enviar(canais[i][1], valores[i]);
            close(canais[i][1]);
            _exit(resultado == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
        }
    }
    for (int i = 0; i < 3; ++i) close(canais[i][1]);
    if (aguardar_filhos() == -1) falhou = 1;
    _exit(falhou ? EXIT_FAILURE : EXIT_SUCCESS);
}

int main(void)
{
    double a, b, delta;
    printf("Digite a, b e delta (delta ja calculado): ");
    fflush(stdout);
    if (scanf("%lf %lf %lf", &a, &b, &delta) != 3 ||
        !isfinite(a) || !isfinite(b) || !isfinite(delta) ||
        a == 0 || delta < 0 || !isfinite(2 * a)) {
        fprintf(stderr, "Entrada invalida: use numeros finitos, a != 0 e delta >= 0.\n");
        return EXIT_FAILURE;
    }
    /* Converte pipe sem leitor em erro de write, permitindo tratar a falha. */
    if (signal(SIGPIPE, SIG_IGN) == SIG_ERR) { perror("signal"); return EXIT_FAILURE; }
    for (int i = 0; i < 5; ++i) {
        if (pipe(canais[i]) == -1) {
            perror("pipe");
            for (int j = 0; j < i; ++j) {
                close(canais[j][0]); close(canais[j][1]);
            }
            return EXIT_FAILURE;
        }
    }
    pid_t son1 = fork();
    if (son1 == -1) {
        perror("fork calculador");
        fechar_exceto(-1, -1, -1, -1, -1);
        return EXIT_FAILURE;
    }
    if (son1 == 0) calculador();
    pid_t son5 = fork();
    if (son5 == -1) {
        perror("fork coordenador");
        fechar_exceto(-1, -1, -1, -1, -1);
        (void)aguardar_filhos();
        return EXIT_FAILURE;
    }
    if (son5 == 0) fornecedores(a, b, delta);

    fechar_exceto(3, 4, -1, -1, -1);
    double x1, x2;
    int falhou = receber(canais[3][0], &x1) == -1;
    if (receber(canais[4][0], &x2) == -1) falhou = 1;
    close(canais[3][0]); close(canais[4][0]);
    if (aguardar_filhos() == -1) falhou = 1;
    if (!falhou) printf("Raizes: x1 = %.10g; x2 = %.10g\n", x1, x2);
    return falhou ? EXIT_FAILURE : EXIT_SUCCESS;
}
