/*****************************************************************************
Aula de SO.

Maycon Leone M. Peixoto

 *****************************************************************************/

#include <sys/sem.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <math.h>
#include <sys/types.h>

int main(int argc, char *argv[])
{

        int       fd_1[2], fd_2[2], fd_3[2], fd_4[2], fd_5[2], nbytes, contador=0, i, val;
        char      string[10];
        char      readbuffer[80];
        pid_t     son1, son2, son3, son4, son5;

    
        pipe(fd_1);
        pipe(fd_2);
        pipe(fd_3);
        pipe(fd_4);
        pipe(fd_5);
           
        if((son1 = fork()) == -1)
        {
                perror("fork");
                exit(1);
        }
     
        if(son1 == 0)
        {
                double y, w, z, res1, res2;
          
                //Receiving (-b)
 		close(fd_1[1]);
		read(fd_1[0], readbuffer, sizeof(readbuffer));
		y = atoi (readbuffer);

                //Receiving (2a)
                close(fd_2[1]);
                read(fd_2[0], readbuffer, sizeof(readbuffer));
                z = atoi (readbuffer);

                //Receiving sqrt(delta)
                close(fd_3[1]);
		read(fd_3[0], readbuffer, sizeof(readbuffer));
		w = atoi (readbuffer);

                //Calculating x1

                res1 = ( y - sqrt(w) ) / (z);
                sprintf(string, "%f ", res1);
                close(fd_4[0]);
 		write(fd_4[1], string, (strlen(string)+1));

                //Calculating x2

                res2 = ( y + sqrt(w) ) / (z);
                sprintf(string, "%f ", res2);
                close(fd_5[0]);
 		write(fd_5[1], string, (strlen(string)+1));

                exit(0);
        }
      
        if((son5 = fork()) == -1)
        {
                perror("fork");
                exit(1);
        }
     
        if(son5 == 0)
        {    
                if((son2 = fork()) == -1)
                {
                       perror("fork");
                       exit(1);
                }
                if(son2 == 0)
                {
		       printf("Digite o valor de --- a --- para o Processo %d \n", getpid());
		       scanf("%d",&val);
		       fflush(stdin);
		       sprintf(string, "%d ", 2*val);
		       close(fd_2[0]);
		       // Send "string" through the output side of pipe 
		       write(fd_2[1], string, (strlen(string)+1));
		       // Child process closes up input side of pipe 
		       exit(1);
                }       
                
                if((son3 = fork()) == -1)
                {
                       perror("fork");
                       exit(1);
                }
                if (son3 == 0) 
                {
                       printf("Digite o valor de --- b --- para o Processo %d \n", getpid());
                       scanf("%d",&val);
		       fflush(stdin);
		       sprintf(string, "%d ", -val);
		       close(fd_1[0]);
		       // Send "string" through the output side of pipe 
		       write(fd_1[1], string, (strlen(string)+1));
		       // Child process closes up input side of pipe 
                       exit(1); 
                 }  
        
                 if((son4 = fork()) == -1)
                 {
                       perror("fork");
                       exit(1);
                 }
                 if (son4 == 0) 
                 {
                       printf("Digite o valor de --- delta --- para o Processo %d \n", getpid());
	               scanf("%d",&val);
    		       fflush(stdin);
   		       sprintf(string, "%d ", val);
		       close(fd_3[0]);
		       // Send "string" through the output side of pipe 
		       write(fd_3[1], string, (strlen(string)+1));
		       // Child process closes up input side of pipe 
		       exit(1);
                  }
        exit(1);
        }
        else 
        {
                char x1[50], x2[50];

                /* Parent process closes up output side of pipe */
                close(fd_4[1]);
                /* Read in a string from the pipe */
                read(fd_4[0], readbuffer, sizeof(readbuffer));

                strcpy (x1, readbuffer);
         
                /* Parent process closes up output side of pipe */
                close(fd_5[1]);
                /* Read in a string from the pipe */
                read(fd_5[0], readbuffer, sizeof(readbuffer));

                strcpy (x2, readbuffer);         

                printf("\n\nValor da formula de Bhaskara eh x': %s e x'': %s \n\n\n", x1, x2);
        }
        return(0);
}


