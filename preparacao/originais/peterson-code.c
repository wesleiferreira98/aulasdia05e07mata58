#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <time.h>

#define TRUE 1
#define FALSE 0
#define N 2

struct timespec t;

int turn = 0;
int interested[N];
char cr[8];

void *thread0(void *p_arg);
void *thread1(void *p_arg);


int main(void)
{
	pthread_t thd0, thd1;
	pthread_create(&thd0, 0, (void *) thread0, NULL);
	pthread_create(&thd1, 0, (void *) thread1, NULL);

	pthread_join(thd0,0);
	pthread_join(thd1,0);

	exit(0);
}

void enter_region(int thread)
{
	int other;  // numero de outro processo
		
	other = 1 - thread;	// o oposto do processo 
	interested[thread] = TRUE; // mostra que vc esta interessado
	turn = thread;
	while(turn == thread && interested[other] == TRUE)
        ;
        //in
}

void leave_region(int thread)
{
	interested[thread] = FALSE;
}

void *thread0(void *p_arg)
{

	while(TRUE)
	{
		while(turn != 0);
                    //printf("Esperando....Thread0\n");
		enter_region(0);
		strcpy(cr,"thread0");
		printf("\n%s",cr);
              	leave_region(0);
		turn = 1;
		sleep(1); 
	}
}

void *thread1(void *p_arg)
{
	while(TRUE)
	{
		while(turn != 1);
		enter_region(1);
		strcpy(cr,"thread1#######");
		printf("\n%s",cr);
		leave_region(1);
		turn = 0;
                sleep(1); 
	}
}
