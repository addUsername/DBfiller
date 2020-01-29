/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbfiller;

import java.util.Random;

public class Columna {

    final String nombreCol;
    final String type;
    final Boolean autoIncr;
    final String tablaName;
    private final Boolean nul;
    private final int clave;
    
    private final Random r = new Random();
    
    //allTables[i].add(new Columna(nombreCol,type,nul,clave,autoIncr));   
    public Columna(String nombreCol, String type, Boolean nul, int clave, Boolean autoIncr, String tablaName){
        
        this.nombreCol = nombreCol;
        this.type = type;
        this.nul = nul;
        this.clave = clave;
        this.autoIncr = autoIncr;
        this.tablaName = tablaName;
    }
    public String inventCol(){
        
        //deberia este metodo recibir un ejemplo?
        //select
        
        System.out.println("Creando campo: "+this.nombreCol);
        //aqui es donde ira la logica del programa
        
        String option="", toReturn ="";
        //si la columna es de autoincremento no la generamos, hay que enviar la lista de columnas a modificar
        if (this.autoIncr) return "";
        if (this.type.matches(".*FK$")){
            //posibilidades para clave foranea
            //le pasamos como parametros la table(column) con la que esta relacionada y nos devuelve todos los valores posibles
            String [] herency = this.type.split(" ");
            herency [1] = herency[1].substring(0,herency[1].length() - 2);
            String[] choices = DBfiller.getConstraintWords(herency[1].split("\\."));
            return choices[r.nextInt(choices.length)]+",";           
        }
        
        //implementar find en un futuro..
        int value = 1;
        if (this.type.matches("varchar.*$")){
            //coge el valor entre parentesis varchar (8)
            value = Integer.parseInt(this.type.substring( 8, this.type.length()-1));
            String c="";
                for(int i = 0;i<value;i++){
                    c += (char)(r.nextInt(26) + 'a');
                }
                toReturn += "'"+c+"',";
                
        }else if (this.type.matches("int.*$")) { //"(int [(][0-9]*[)]")
            option = "int";
            toReturn +="'"+(int) (Math.random()*1000)+"',";
            //value = Integer.parseInt(this.type.substring(5 , this.type.length()-1));
        }else if (this.type.matches("date")){
            option = "date";
            System.out.println("-IMPLEMENTAR random DATE");
        }else{
                System.out.println("no rula con este ");
                toReturn += "null";
            
        }
        if(this.clave==3){
            System.out.println(toReturn.substring(0,toReturn.length()-1));//quitamos parentesis
            if(DBfiller.isUnique(this, toReturn.substring(0,toReturn.length()-1)))System.out.println("everything is good");
        }
        return toReturn;    
    }
}
