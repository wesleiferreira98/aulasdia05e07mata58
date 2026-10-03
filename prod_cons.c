/* Aula de SO: produtor-consumidor com buffer circular e semaforos POSIX. */
#include <assert.h>
#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define N 20
#define TOTAL_ITENS 40
static sem_t mutex, empty, full;
static int buffer[N], lo, hi, count;

static void check_sem(int resultado, const char *operacao)
{
    if (resultado == -1) { perror(operacao); exit(EXIT_FAILURE); }
}

static void wait_sem(sem_t *sem)
{
    int resultado;
    do { resultado = sem_wait(sem); } while (resultado == -1 && errno == EINTR);
    check_sem(resultado, "sem_wait");
}

static void check_thread(int erro, const char *operacao)
{
    if (erro != 0) {
        fprintf(stderr, "%s: %s\n", operacao, strerror(erro));
        exit(EXIT_FAILURE);
    }
}

/* Chamadas somente com mutex adquirido. */
static void insert_item(int item)
{
    assert(count < N);
    buffer[hi] = item;
    hi = (hi + 1) % N;
    ++count;
}

static int remove_item(void)
{
    assert(count > 0);
    int item = buffer[lo];
    lo = (lo + 1) % N;
    --count;
    return item;
}

static void *producer(void *arg)
{
    (void)arg;
    for (int i = 0; i < TOTAL_ITENS; ++i) {
        int item = rand() % 100; /* Apenas o produtor usa rand. */
        wait_sem(&empty); /* Espera vaga ANTES de adquirir mutex. */
        wait_sem(&mutex);
        insert_item(item);
        printf("Produzido: %d | ocupacao: %d/%d\n", item, count, N);
        check_sem(sem_post(&mutex), "sem_post mutex");
        check_sem(sem_post(&full), "sem_post full");
    }
    return NULL;
}

static void *consumer(void *arg)
{
    (void)arg;
    for (int i = 0; i < TOTAL_ITENS; ++i) {
        wait_sem(&full); /* Espera item ANTES de adquirir mutex. */
        wait_sem(&mutex);
        int item = remove_item();
        /* count e lido dentro da mesma regiao que protege suas escritas. */
        printf("Consumido: %d | ocupacao: %d/%d\n", item, count, N);
        check_sem(sem_post(&mutex), "sem_post mutex");
        check_sem(sem_post(&empty), "sem_post empty");
        /* Processamento do item, se necessario, pode ocorrer aqui fora do lock. */
    }
    return NULL;
}

int main(void)
{
    srand((unsigned)time(NULL));
    check_sem(sem_init(&mutex, 0, 1), "sem_init mutex");
    check_sem(sem_init(&empty, 0, N), "sem_init empty");
    check_sem(sem_init(&full, 0, 0), "sem_init full");
    pthread_t produtor, consumidor;
    check_thread(pthread_create(&produtor, NULL, producer, NULL), "pthread_create");
    check_thread(pthread_create(&consumidor, NULL, consumer, NULL), "pthread_create");
    /* join espera o termino; nao impede uma thread de terminar antes. */
    check_thread(pthread_join(produtor, NULL), "pthread_join");
    check_thread(pthread_join(consumidor, NULL), "pthread_join");
    assert(count == 0);
    check_sem(sem_destroy(&mutex), "sem_destroy mutex");
    check_sem(sem_destroy(&empty), "sem_destroy empty");
    check_sem(sem_destroy(&full), "sem_destroy full");
    printf("Fim: %d itens produzidos e consumidos; buffer vazio.\n", TOTAL_ITENS);
    return EXIT_SUCCESS;
}
