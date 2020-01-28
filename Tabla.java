/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbfiller;

/**
 *
 * @author SERGI
 */
public class Tabla {

    final String name;    
    private final int numCol;
    Columna[] columnas;
    
    public Tabla(String name, int numCol){
        
        this.name = name;
        this.numCol = numCol;
        columnas  = new Columna[numCol];
    }
    
    public void add(Columna cool){        
        int i=0;        
        for(Columna c:columnas){
            if(c==null){
                columnas[i]=cool;
                break;
            }i++;
        } 
    }
    public String inventRow(){
        
        String toReturn="(";
        
        for (Columna cool:columnas) toReturn+=cool.inventCol();
        
        return toReturn.substring(0,toReturn.length()-1)+")";
    }
    public String columnsNotAutoIncrement(){
        String toReturn="(";
        for(Columna cool: columnas){ toReturn += (cool.autoIncr)? "":cool.nombreCol+"," ;
            
        }
        return toReturn.substring(0,toReturn.length()-1)+")";
    }
    
    @Override
    public String toString(){
        String toReturn = "Nombre tabla: "+name+"\n";
        for (Columna columna : columnas) {
            toReturn += "columna: "+columna.nombreCol+" / tipo datos: "+columna.type+"\n";
        }        
        return toReturn;
    }
}
