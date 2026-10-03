#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

int main(){

	int tamanho=3, i=0, soma=0;
	int *v = (int *) malloc (sizeof(int)*tamanho);
	long int pid;

	long int mypid= (long) getpid();

	for(i=0; i<tamanho; i++){
		pid = fork();
		if(pid == 0){
			v[i] = i+5;
		}
	}
	
	for(i=0; i<tamanho; i++) 
		soma += v[i];

	if(mypid == (long) getpid()) {
		//sleep(5);
		printf("Soma total: %d\n", soma);
	}

	free(v);

	return EXIT_SUCCESS;
}
