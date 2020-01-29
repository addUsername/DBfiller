package dbfiller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SERGI
 */
public class DBfiller {
    
    public static Statement query;
    public static Statement query2,query3,query4,query5;
    public static Tabla[] allTables;
    public static String nombreDB="";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner input = new Scanner(System.in);
        
            
        System.out.println("Introduzca el SGBD, compatible con mySQL, postgreSql y Oracle");
        String driver = input.nextLine();
        System.out.println("Introduzca el hostName/datebaseName, ex 127.0.0.1/mydb");
        String hostname = input.nextLine();
        String str[]=hostname.split("/");
        nombreDB=str[1];
        System.out.println("Introduzca el user");
        String user = input.nextLine();
        System.out.println("Introduzca el pass");
        String pass = input.nextLine();          
            
                
        
        try{
            //connect(driver,hostname,user,pass);
            //solo es necesario una query extra
            Connection conexion = connect(driver,hostname,user,pass);            
            query = conexion.createStatement();
            query2 = conexion.createStatement();
            
            ResultSet rs = query.executeQuery("SHOW TABLES");
            ResultSetMetaData metaData=rs.getMetaData();
            
            //contamos cuantas tablas que hay y almacenamos su nombre
            List <String> tablas = new ArrayList <String>();
            while(rs.next())tablas.add(rs.getString(1));
            describeTables(tablas.toArray());
            
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Introduzca el numero de filas a insertar");
        int numRows = (int) input.nextInt();
        for(Tabla tabla:allTables){
            if(tabla != null){
                for (int i = 0; i < numRows; i++) insertRow(tabla.inventRow(),tabla);
            }
        }
        //insertRow(allTables[0].inventRow(),allTables[0]);
    }
    public static Connection connect(String driver,String hostname,String user,String pass) throws ClassNotFoundException, SQLException{//{
        System.out.println("Cargando driver..");
        String sgbd, conexionStr="";
        //Fix these
        switch (driver.toLowerCase()) {
            case "postgresql":
                sgbd="org.postgresql.jdbc.Driver";
                conexionStr = "";
                break;
            case "oracle":
                sgbd="oracle.jdbc.driver.OracleDriver";
                break;
            default:
                sgbd="com.mysql.jdbc.Driver";
                conexionStr = "jdbc:mysql://"+hostname;
                break;
        }
        
        Class.forName(sgbd);  //"jdbc:mysql://DESKTOP-SKFLI33/leer"
        System.out.println("Estableciendo conexion..");
        Connection conexion = DriverManager.getConnection(conexionStr, user, pass);
        if(conexion!=null)System.out.println("Conectado con exito");
        return conexion;
    }
    public static void describeTables(Object[] tablas) throws SQLException{
        
        //Creamos un array que contendra todos las tablas con sus correspondientes columnas, utilidad aqui??
        allTables = new Tabla[tablas.length];
        for(int i = 0; i<tablas.length;i++){ 
            System.out.println("Analizando tabla "+tablas[i]);    
            ResultSet rs = query.executeQuery("DESCRIBE "+tablas[i]);
            //ResultSetMetaData metaData=rs.getMetaData();   
            
            allTables[i] = new Tabla((String)tablas[i],getNumColumns(""+tablas[i]));
            while(rs.next()){
                
                String nombreCol=rs.getString(1);
                String type = rs.getString(2);
                Boolean nul = (rs.getString(3).equals("YES"))? true : false;
                int clave=0;
                if(rs.getString(4).equals("PRI")){
                    clave = 1;
                }else if(rs.getString(4).equals("MUL")){
                   //esta query nos devolvera las relaciones que tiene FK de esta tabla y la siguiente
                   //pasara un array con los valores posibles para esa FK
                   ResultSet rs3 = query2.executeQuery("select "
                            + "concat(table_name, '.', column_name) as 'foreign key',"
                           + "concat(referenced_table_name, '.', referenced_column_name) as 'references' "
                          + "from "
                          + "information_schema.key_column_usage "
                          + "where "
                           + "referenced_table_name is not null "
                            + "and table_schema = '"+nombreDB+"'");
                    while (rs3.next()){
                        //almacenamos la info tabla(columna) con la que se relaciona esta columan
                        if (rs3.getString(1).matches("^.*"+nombreCol))type += " "+rs3.getString(2)+"FK";
                    }
                    clave = 2;
                }else if (rs.getString(4).equals("UNI")){
                    
                    clave=3;
                }
                Boolean autoIncr = (rs.getString(6).equals("auto_increment"))? true : false;
                
                allTables[i].add(new Columna(nombreCol,type,nul,clave,autoIncr,tablas[i].toString()));                
            }
        }
        
        for(Tabla tabla:allTables){
            if(tabla != null) System.out.println(tabla.toString()+"\n");
        }
    }
    private static int getNumColumns (String nameColumn){
        int numCol=1;
        //dara error?? NAH funciona bien
        ResultSet nc;
        try {
            nc = query2.executeQuery("SELECT count(*) " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = '"+nombreDB+"' " +
                    "AND table_name = '"+nameColumn+"'");
            numCol = 1;
            if(nc.next()) numCol = nc.getInt(1);                
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        return numCol;
}
    private static void insertRow(String row, Tabla tabla){        
        
        try {
            
            System.out.println(" VALUES "+row+";");
            query2.executeUpdate("INSERT INTO "+tabla.name+" "+tabla.columnsNotAutoIncrement()+
                    " VALUES "+row+";");
                    
        } catch (SQLException ex) {
            Logger.getLogger(DBfiller.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    static String[] getConstraintWords(String[] herency){
        
        //venimos del tema FOREIGN KEY, vamos a SELECT * FROM
        //herency  = {table,column}        
        List<String> keysPK = new ArrayList<String>();
        
        try {
            //ResultSet rs5 = query5.executeQuery("SELECT "+herency[1].substring(0,herency[1].length() -1)+" FROM "+herency[0]);
            ResultSet rs5 = query2.executeQuery("SELECT "+herency[1]+" FROM "+herency[0]);
            
            while(rs5.next()){
                keysPK.add(rs5.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBfiller.class.getName()).log(Level.SEVERE, null, ex);
        }
//como coño evito estooo, asi:
        String [] toReturn = new String[keysPK.size()];
        int i=0;
        for (Object key:keysPK.toArray()){
             toReturn [i] = key.toString();
             i++;
        }
        return toReturn;
    }
    static Boolean isUnique(Columna cool,String value){
        //return null si EX
        try {
            //ResultSet rs7 = query2.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME LIKE '%"+cool.nombreCol+"%'");
            ResultSet rs6 = query2.executeQuery("SELECT * FROM "+cool.tablaName+" WHERE "+cool.nombreCol+"="+value+"");
            if(rs6.next()){
                System.out.println(cool.nombreCol+"It is not unique");
                return false;
                
            }else return true;
        } catch (SQLException ex) {
            Logger.getLogger(DBfiller.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
