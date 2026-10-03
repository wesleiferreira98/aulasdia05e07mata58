/* Peterson para duas threads: atomics C11 sequencialmente consistentes.
 * Espera ocupada intencional para fins didaticos; nao ha alternancia forcada.
 */
#include <pthread.h>
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define ITERACOES 10000
static atomic_int turn = 0;
static atomic_bool interested[2];
static char cr[16];
static int contador;

static void enter_region(int eu)
{
    int outro = 1 - eu;
    atomic_store(&interested[eu], 1);
    atomic_store(&turn, outro);
    while (atomic_load(&interested[outro]) && atomic_load(&turn) == outro) {
        /* Espera ocupada. */
    }
}

static void leave_region(int eu)
{
    atomic_store(&interested[eu], 0);
}

static void *thread(void *arg)
{
    int eu = *(int *)arg;
    for (int i = 0; i < ITERACOES; ++i) {
        enter_region(eu);
        strcpy(cr, eu == 0 ? "thread0" : "thread1#######");
        ++contador;
        if (i == 0) printf("Regiao critica: %s\n", cr);
        leave_region(eu);
    }
    return NULL;
}

static void check(int erro, const char *operacao)
{
    if (erro != 0) {
        fprintf(stderr, "%s: %s\n", operacao, strerror(erro));
        exit(EXIT_FAILURE);
    }
}

int main(void)
{
    pthread_t threads[2];
    int ids[2] = {0, 1};
    atomic_init(&interested[0], 0);
    atomic_init(&interested[1], 0);
    for (int i = 0; i < 2; ++i)
        check(pthread_create(&threads[i], NULL, thread, &ids[i]), "pthread_create");
    for (int i = 0; i < 2; ++i)
        check(pthread_join(threads[i], NULL), "pthread_join");
    printf("Contador: %d (esperado: %d)\n", contador, 2 * ITERACOES);
    return contador == 2 * ITERACOES ? EXIT_SUCCESS : EXIT_FAILURE;
}
