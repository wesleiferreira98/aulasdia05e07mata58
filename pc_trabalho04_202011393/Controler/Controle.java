package Controler;

/*
    Classe: Controlee
    Descrição:Esta é a classe onde é ocorre controle de buffer e entrada e saida da regiao critica
*/
import java.util.concurrent.Semaphore;//importa o semafaro do java

public class Controle {
    static Semaphore mutex = new Semaphore(1);//controla a entrada e saida do buffer
    /* os dois semafaros abaixo controla o estados do buffer*/
    static Semaphore cheioS= new Semaphore(0);
    static Semaphore vazioM= new Semaphore(1);
    static int vagas = 10;//contem a quantidade de vagas disponiveis
    static int cheio = 0;//contem a quantidade de vagas oculpadas
    static double velocidadePro =2.0;//armazema a velocidade do produtor
    static double velocidadeCom =2.0;//armazema a velocidade do comsumidor
   
   
      
    /* ***************************************************************
    * Metodo: EntrandoRC
    * Funcao: autoriza ou barra a entrada de processos na região critica
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */
    static void  EntrandoRC(){
        try {
            mutex.acquire();//libera o acesso a regiao critica caso nao haja processos nela
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
     /* ***************************************************************
    * Metodo: saindoRC
    * Funcao: libera o acesso da regiao para que outros processos possam entrar na região critica
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */
    static void saindoRC(){
        mutex.release();//libera o acesso da regiao para que outros processos possam entrar na região critica
    }
    /* ***************************************************************
    * Metodo: setVazio
    * Funcao: diminui o valor do semafaro vazio para que o produtor possa nao produzir o dinheiro 
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */
    static void setVazio(){
        try {
            vazioM.acquire();// diminui o valor do semafaro vazio para que o produtor nao possa produzir o dinheiro 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /* ***************************************************************
    * Metodo: decVazio
    * Funcao: aumenta o valor do semafaro vazio 
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */

    static void decVazio(){
        vazioM.release();//aumenta o valor do semafaro vazio 
    }
    /* ***************************************************************
    * Metodo: decCheio
    * Funcao: aumenta o valor do semafaro vazio 
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */
    static void decCheio(){
        cheioS.release();//aumenta o valor do semafaro cheio
    }
    /* ***************************************************************
    * Metodo: setCheio
    * Funcao: diminui o valor do semafaro vazio 
    * Parametros: nao recebe parametros
    * Retorno: nao retorna valores
    *************************************************************** */
    static void setCheio(){
        try {
            cheioS.acquire();//diminui o valor do semafaro vazio 
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
    }
    
    
}
