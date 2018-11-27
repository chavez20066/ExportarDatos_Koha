/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exportardatos_koha;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 *
 * @author jbasurco
 */
public class ProcesarMigracionUsuarios {

    /*private Statement stmt;
    private ResultSet varResultado;
    private Connection con;*/
    
    /* tabla a insertar imagenes koha 
    
    */
    clsConexion conexion4 = new clsConexion();
    clsConexion conexion2 = new clsConexion();
          
    
        
    public void procesando() throws SQLException, FileNotFoundException, IOException {

        try {

            String varSql = "select datos,codigo,tipo_lector,fecha_registro,Fecha_venc,cod_prog,programa,direccion from MaestroBiblio where datos is not null and substring(codigo,1,4)>='2010'";

            System.out.println("SQL=>" + varSql);

            clsConexion conexion1 = new clsConexion();
            conexion1.abrirConsulta();
            conexion1.varResultado = conexion1.stmt.executeQuery(varSql);
            
                      
            String datos,codigo,tipo_lector,fecha_registro,fecha_venc,cod_prog,programa,direccion;
                    
            conexion4.abrirConsultaMysql();
            int count=1;
            while (conexion1.varResultado.next()) {
                
                
                datos =conexion1.varResultado.getString("datos");
                codigo =conexion1.varResultado.getString("codigo");
                tipo_lector =conexion1.varResultado.getString("tipo_lector");
                fecha_registro =conexion1.varResultado.getString("fecha_registro");
                fecha_venc =conexion1.varResultado.getString("fecha_venc");
                cod_prog =conexion1.varResultado.getString("cod_prog");
                programa =conexion1.varResultado.getString("programa");
                direccion=conexion1.varResultado.getString("direccion");
                if(direccion==null) direccion="";
               
                System.out.print(count++ +" = "+codigo + " => ");    
                if( guardarUsuario(datos,codigo,tipo_lector,fecha_registro,fecha_venc,cod_prog,programa,direccion)){
                     System.out.println(" == INSERTO METADATOS "); 
                }
                else{
                     System.err.println(" ==> NO INSERTO METADATOS <="); 
                }
            }
                      
           
            conexion1.cerrarConsulta();
            conexion4.cerrarConsulta();
            conexion2.cerrarConsulta();

        } catch (SQLException ex) {
            System.out.println("ProcesarPortadaLibros()" + ex.getMessage());
            Logger.getLogger(ExportarDatos_Koha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    public boolean guardarUsuario(String  datos,String codigo,String tipo_lector,String fecha_registro,
            String fecha_venc,String cod_prog,String programa,String direccion) throws SQLException{        
/*insert into borrowers(cardnumber,surname,firstname,address,branchcode,categorycode,dateenrolled,dateexpiry,password,userid,sort1,sort2)
values ('2009601051','CHAVEZ','DIEGUITO','URB. MAGISTERIAL II','BIBCE','TL01','2018-10-25','2018-12-31',
'$2a$08$NSZrJohrItwO160kra1o5ewoLLXyNJXep40Tc8EhOJiorBPk2Wn22','2009601051','71','INGENIERIA DE SISTEMAS')*/

        String pw_hash = BCrypt.hashpw(codigo, BCrypt.gensalt()); 
        //System.out.println(pw_hash);
        
	String insert = "insert into borrowers (cardnumber,surname,branchcode,categorycode,dateenrolled,dateexpiry,password,userid,sort1,sort2,address,city) values(?,?,?,?,?,?,?,?,?,?,?,?)";
	
	PreparedStatement ps = null;
        int generatedkey=0;
       
	try {   
            //stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		//ps = conexion4.con.prepareStatement(insert);
                ps = conexion4.con.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS);
                
		ps.setString(1,codigo);
		ps.setString(2, datos);
                ps.setString(3,"BIBCE");
                ps.setString(4,tipo_lector);
                ps.setString(5,fecha_registro);                
                ps.setString(6,fecha_venc);
                ps.setString(7,pw_hash);
                ps.setString(8,codigo);
                ps.setString(9,cod_prog);
                ps.setString(10,programa);
                ps.setString(11,direccion);
		ps.setString(12,"Arequipa");
                
                ps.executeUpdate();
                ResultSet rs=ps.getGeneratedKeys();
                if (rs.next()) {
                   generatedkey=rs.getInt(1);   
                  // System.out.print(" biblionumber =>" + generatedkey); 
                    if(guardarImagen(generatedkey,codigo)){
                             System.out.print(" == INSERTO IMAGEN"); 
                    }else{
                        System.out.print(" ================= ERROR IMAGEN==================");
                    }
                }
                else{
                      System.err.print(" ================= ERROR USUARIO ==================");
                }
           
		//conexion4.con.commit();
                
		return true;
	} catch (Exception ex) {
                System.out.println(" guardarImagen() " +ex.getMessage());
                printStackTrace();
	}finally{
            try {
                    ps.close();			                     

            } catch (Exception ex) {
                     System.out.println(" guardarImagen() " +ex.getMessage());
                     printStackTrace();
            }
	}        
	return false;
    }
    
      public boolean guardarImagen(int biblionumber,String codigo) throws SQLException{        

	String insert = "insert into patronimage (borrowernumber,mimetype,imagefile) values(?,?,?)";
	FileInputStream fis = null;
        FileInputStream fis2 = null;
	PreparedStatement ps = null;
       
	try {
                String anio=codigo.substring(0, 4);
                String nomImg=codigo.substring(2, 10) +".jpg";
                String ruta= "G:\\dmed\\f" + anio+"\\"+nomImg;
		
                 System.out.print(" rutaServer => " + ruta); 
                // read a jpeg from a inputFile
                BufferedImage bufferedImage = ImageIO.read(new File(ruta));
                //BufferedImage resized = resize(bufferedImage, 269, 350);
               
                
                nomImg=nomImg.replace("jpg", "png");
                String rutaLocal1="E:\\KOHA_MIGRACION_TAPAS\\imgs_usuarios\\"+nomImg;                
                       
                System.out.print(" rutaLocal => " + rutaLocal1);       
                ImageIO.write(bufferedImage, "png", new File(rutaLocal1));
                //ImageIO.write(bufferedImage, "png", new File(rutaLocal2));
                
                File file = new File(rutaLocal1);
		fis = new FileInputStream(file);              
               
                
		ps = conexion4.con.prepareStatement(insert);
                
		ps.setInt(1,biblionumber);
		ps.setString(2, "image/png");
                ps.setBinaryStream(3,fis,file.length());
		
		ps.executeUpdate();
		//conexion4.con.commit();
		return true;
	} catch (Exception ex) {
		          System.out.print(" guardarImagen " +ex.getMessage());
                         // printStackTrace();
	}finally{
		try {
			ps.close();
			fis.close();                        
                        
		} catch (Exception ex) {
			 System.out.print(" guardarImagen " +ex.getMessage());
                        // printStackTrace();
		}
	}        
	return false;
    }

   
}

   