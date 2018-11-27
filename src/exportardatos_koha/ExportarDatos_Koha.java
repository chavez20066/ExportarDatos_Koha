/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exportardatos_koha;

import java.io.IOException;
import java.sql.SQLException;
import javax.xml.bind.DatatypeConverter;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 *
 * @author jbasurco
 */
public class ExportarDatos_Koha {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
        // TODO code application logic here
        //Proceso miProceso = new Proceso();
       //   miProceso.procesando();
        
      
       
          
          
       // String pw_hash = BCrypt.hashpw("2009601051", BCrypt.gensalt()); 
        //System.out.println(pw_hash);
      
        ProcesarMigracionUsuarios pmu=new ProcesarMigracionUsuarios();
        pmu.procesando();
       
      /*********LIBROS************/ 
    //ProcesarLibros pl=new ProcesarLibros();
    //pl.procesando();
          

      /*****REVISTAS*******/
      
     // ProcesarRevistas pr=new ProcesarRevistas();
    //  pr.procesando();
     
     /****TESIS**/
   // ProcesarTesis pt=new ProcesarTesis();
    //pt.procesando();

    //ProcesarPortadaLibros librosPortadas=new ProcesarPortadaLibros();
    //librosPortadas.procesando();     
      

    }

}
