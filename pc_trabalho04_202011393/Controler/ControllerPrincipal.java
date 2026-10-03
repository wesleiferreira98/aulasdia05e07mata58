/*
    Classe: ControllerPrincipal
    Descrição:Esta é a classe responsavel gerir todo o controle do programa
*/
package Controler;
import java.net.URL;
import java.util.ResourceBundle;

/* imports necessarios para controlar os componentes da tela*/ 
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ControllerPrincipal implements Initializable{
    public ControllerPrincipal(){}
    Image image = new Image(getClass().getResourceAsStream("/Visao/sedulas.png"));//armazena a imagem cedulas
    Image image2 = new Image(getClass().getResourceAsStream("/Visao/fundo.jpg"));//armazena a imagem fundo que representa a falta de dinheiro
    
    private int estado =0;//armazena o estado das threads
    public ImageView[] iv =  new ImageView[10]; 
    /*
        Classe: Colocar
        Descrição:Esta é a classe responsavel por produzir o dinheiro e coloca-lo no buffer
    */
    public class Colocar extends Thread{
        public Colocar(){}
        /* ***************************************************************
        * Metodo: run
        * Funcao: controla toda a rotina do produtor
        * Parametros: nao recebe parametros
        * Retorno: nao retorna valores
        *************************************************************** */
        public void run(){
            while(true){
                int din = Dinheiros.produzDindin();//prodiz o dinheiro
                Controle.setVazio();// diminui o valor do semafaro vazio para que o produtor nao possa produzir o dinheiro 
                Controle.EntrandoRC();// entra na regiao critica se possivel
                Dinheiros.colocarDindin(din);//coloca o dinherio produzido no buffer
                ColocarImagem(din);//atualiza a imagem na tela
                Controle.decCheio();//aumenta o valor do semafaro cheio
                Controle.saindoRC();//libera o acesso da regiao para que outros processos possam entrar na região critica
                
                try {
                    Thread.sleep((long) (1000*10/Controle.velocidadePro));// controla a velocidade do produtor
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*
        Classe: Tirar
        Descrição:Esta é a classe responsavel por comsumir o dinheiro produzido
    */

    public class Tirar extends Thread{
        public Tirar(){}
        /* ***************************************************************
        * Metodo: run
        * Funcao: controla toda a rotina do consumidor
        * Parametros: nao recebe parametros
        * Retorno: nao retorna valores
        *************************************************************** */
        public void run(){
            while(true){
                Controle.setCheio();//diminui o valor do semafaro vazio
                Controle.EntrandoRC();// entra na regiao critica se possivel
                int rer = Dinheiros.retiraDindin();// obtem o valor retirado do buffer
                tirarImagem(rer);//retirar a imagem de acordo com o valor obitido
                Controle.decVazio();//aumenta o valor do semafaro vazio 
                Controle.saindoRC();//libera o acesso da regiao para que outros processos possam entrar na região critica
                try {
                    Thread.sleep((long) (1000*10/Controle.velocidadeCom));//controla o velocidade do consumidor
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    Colocar at;//instacia a classe produtor
    Tirar ti;//instancia a classe consumidor
    
    
    /*os proximos atributos instacia as imagens os textos e os controladores de velocidade */
    @FXML
    private Label din1;

    @FXML
    private ImageView dindin1;

    @FXML
    private ImageView dindin10;

    @FXML
    private ImageView dindin2;

    @FXML
    private ImageView dindin3;

    @FXML
    private ImageView dindin4;

    @FXML
    private ImageView dindin5;

    @FXML
    private ImageView dindin6;

    @FXML
    private ImageView dindin7;

    @FXML
    private ImageView dindin8;

    @FXML
    private ImageView dindin9;

    @FXML
    private Slider velociadaColeta;

    @FXML
    private Slider velociadadePro;

    @FXML
    private Label velCom;

    @FXML
    private Label velPro;
    
    
     /* ***************************************************************
        * Metodo: Iniciar
        * Funcao: inicia e controla os estados da threads
        * Parametros: recebe parametros ActionEvent
        * Retorno: nao retorna valores
    *************************************************************** */

    @FXML
    void Iniciar(ActionEvent event) {
        switch(estado){
            case 0:{//caso o estado seja 0 inicia as threads pela primeira vez
                at.start();
                ti.start();
                estado = 1;//seta o estado para 1
                break;
            }
            case 1:{//caso o estado seja 1 chama o metodo parar
                parar(event);
                break;
            }
            case 2:{// caso o estado seja 2 retoma a execussão das threads e joga o estado para 1
                at.resume();
                ti.resume();
                estado =1;
            }
            
        }

    }
    /* ***************************************************************
        * Metodo: parar
        * Funcao:  para a execussao das threads e joga o esta para 1
        * Parametros: recebe parametros ActionEvent
        * Retorno: nao retorna valores
    *************************************************************** */

    @FXML
    void parar(ActionEvent event) {
        at.suspend();
        ti.suspend();
        estado =2;

    }
    
    
    /* ***************************************************************
        * Metodo: initialize
        * Funcao:  inicializa na execussao do programa
        * Parametros: recebe parametros URL e ResourceBundle
        * Retorno: nao retorna valores
    *************************************************************** */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        at = new Colocar(); //coloca a thread produtor em estado de pronto
        ti = new Tirar(); //coloca a thread consumidor em estado de pronto
        /*
            atualiza a velocidade do produtor e imprime na tela o novo valor setado no slider
        */
       velociadadePro.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                Controle.velocidadePro = velociadadePro.getValue();//atualiza a velocidade do produtor
                
                velPro.setText(String.valueOf(Controle.velocidadePro));//imprime na tela o novo valor setado no slider
               
                
                
                
            }
        });
        /*
            atualiza a velocidade do consumidor e imprime na tela o novo valor setado no slider
        */
        velociadaColeta.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                Controle.velocidadeCom = velociadaColeta.getValue();//atualiza a velocidade do consumidor
                velCom.setText(String.valueOf(Controle.velocidadeCom));// imprime na tela o novo valor setado no slider
               
                
                
                
            }
        });
        
        
        
        
       
    }
    /* ***************************************************************
        * Metodo: ColocarImagem
        * Funcao:  colocar a imagem cedula no ImageView
        * Parametros: recebe parametros int
        * Retorno: nao retorna valores
    *************************************************************** */
    public  void ColocarImagem(int com){
        int yu = com;
        switch(yu){
            case 1:{
                dindin1.setImage(image);
                break;
            }
            case 2:{
                dindin2.setImage(image);
                break;
            }
            case 3:{
                dindin3.setImage(image);
                break;
            }
            case 4:{
                dindin4.setImage(image);
                break;
            }
            case 5:{
                dindin5.setImage(image);
                break;
            }case 6:{
                dindin6.setImage(image);
                break;
            }case 7:{
                dindin7.setImage(image);
                break;
            }
            case 8:{
                dindin8.setImage(image);
                break;
            }
            case 9:{
                dindin9.setImage(image);
                break;
            }
            case 10:{
                dindin10.setImage(image);
                break;
            }
        }//fim do switch case
    }// fim do metodo ColocarImagem

     /* ***************************************************************
        * Metodo: tirarImagem
        * Funcao:  colocar a imagem fundo no ImageView
        * Parametros: recebe parametros int
        * Retorno: nao retorna valores
    *************************************************************** */
    public void tirarImagem(int com){
        int yu = com;
        switch(yu){
            case 1:{
                dindin1.setImage(image2);
                break;
            }
            case 2:{
                dindin2.setImage(image2);
                break;
            }
            case 3:{
                dindin3.setImage(image2);
                break;
            }
            case 4:{
                dindin4.setImage(image2);
                break;
            }
            case 5:{
                dindin5.setImage(image2);
                break;
            }case 6:{
                dindin6.setImage(image2);
                break;
            }case 7:{
                dindin7.setImage(image2);
                break;
            }
            case 8:{
                dindin8.setImage(image2);
                break;
            }
            case 9:{
                dindin9.setImage(image2);
                break;
            }
            case 10:{
                dindin10.setImage(image2);
                break;
            }
        }//fim do switch case

    }// fim do metodo tirarImagem
    

}//fim da classe ControllerPrincipal
