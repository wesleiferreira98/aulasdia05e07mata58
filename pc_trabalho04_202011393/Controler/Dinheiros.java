package Controler;
/*
    Classe: ControllerPrincipal
    Descrição:Esta é a classe responsavel pro conter os metodos de inserção e remocao do buffer
*/
public class Dinheiros{
    
    public Dinheiros(){
        
    }
    static int[] dinheiro = new int[10];//o buffer dos valores
    static int  di =0,retDi=0,i=0,j=0;//variaves de controle
    /* ***************************************************************
        * Metodo: produzDindin
        * Funcao: produiz o dinheiro
        * Parametros: nao recebe parametros
        * Retorno: retorna o dinheiro produzido
        *************************************************************** */
    public static int produzDindin(){
        if(di >=10)
            di =0;
        return di = di+1;

    }//fim do metodo produzDindin

    /* ***************************************************************
        * Metodo:colocarDindin
        * Funcao: colocar o dinheiro produzido no buffer
        * Parametros:  recebe parametros int
        * Retorno: nao retorna valores
        *************************************************************** */
    static void colocarDindin(int di){
        if(i >= dinheiro.length){
            i=0;
        }
        
            dinheiro[i] =di;
            Controle.vagas--;
            Controle.cheio++;
            
        
        i++;
        
    }//fim do metodo colocarDindin

    /* ***************************************************************
        * Metodo:retiraDindin
        * Funcao: retira o dinheiro produzido no buffer
        * Parametros: nao recebe parametros
        * Retorno: retorna o dinheiro que foi retirado
        *************************************************************** */
    static int retiraDindin(){
        retDi = Dinheiros.dinheiro[j];
           Controle.vagas++;
            Controle.cheio--;
            Dinheiros.dinheiro[j] = 0;
            j++;

            if(j>=10){
                j=0;
            }
               
        
        
        return retDi;

    }//fim do metodo retiraDindin
   
    
}// fim da classe Dinheiros
