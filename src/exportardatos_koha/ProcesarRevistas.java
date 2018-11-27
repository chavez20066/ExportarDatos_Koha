/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exportardatos_koha;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 *
 * @author jbasurco
 */
public class ProcesarRevistas {

    /*private Statement stmt;
    private ResultSet varResultado;
    private Connection con;*/
    clsConexion conexion2 = new clsConexion();
    clsConexion conexion3 = new clsConexion();

    public void procesando() throws SQLException, FileNotFoundException, IOException {

        try {

            String varSql = "select distinct  r.Registro,r.Cpri_cod_1,r.Cpri_cod_2,r.Cpri_cod_3,r.Cpri_cod_4,r.ISSN,r.Cpri_cod_1+'.'+r.Cpri_cod_2+'.'+ r.Cpri_cod_3+'.'+r.Cpri_cod_4 as '082$a',r.Autores," +
"r.Mtitulo,r.Edicion,r.Ciudad,r.Editorial,r.Anyo, r.Paginas as 'pages',CONVERT(varchar(10), r.Tamano)+' cm'  as 'size'," +
"r.Contenidos as '505$a',r.Temas as '650$a',r.url  from Revistas r where r.Estado_eliminacion='N' ";

            System.out.println("SQL=>" + varSql);

            clsConexion conexion1 = new clsConexion();
            conexion1.abrirConsulta();
            conexion1.varResultado = conexion1.stmt.executeQuery(varSql);

            //List<String> ResultTesis=new ArrayList<>();           
            Biblio_revista biblio;
            String[] palabrasClaves;

            conexion2.abrirConsultaMysql();
            conexion3.abrirConsulta();
            int cont=1;
            while (conexion1.varResultado.next()) {
                biblio = new Biblio_revista();
                biblio.setCod1(conexion1.varResultado.getString("Cpri_cod_1"));
                biblio.setCod2(conexion1.varResultado.getString("Cpri_cod_2"));
                biblio.setCod3(conexion1.varResultado.getString("Cpri_cod_3"));
                biblio.setCod4(conexion1.varResultado.getString("Cpri_cod_4"));
                biblio.setEnlace(conexion1.varResultado.getString("registro"));
                biblio.setIssn(conexion1.varResultado.getString("issn"));
                biblio.setC082$a(conexion1.varResultado.getString("082$a"));

                String[] Autores = conexion1.varResultado.getString("Autores").split("\r\n");
                biblio.setAutores(Autores);

                biblio.setTitulo(conexion1.varResultado.getString("Mtitulo"));
                biblio.setEdicion(conexion1.varResultado.getString("edicion"));
                biblio.setLugar(conexion1.varResultado.getString("Ciudad"));
                biblio.setEditorial(conexion1.varResultado.getString("editorial"));
                biblio.setAnyo(conexion1.varResultado.getString("anyo"));
                biblio.setPages(conexion1.varResultado.getString("pages"));
                biblio.setSize(conexion1.varResultado.getString("size"));
                biblio.setC505$a(conexion1.varResultado.getString("505$a"));
                biblio.setC650$a(conexion1.varResultado.getString("650$a"));
                biblio.setUrl(conexion1.varResultado.getString("url"));

                if(isNumeric(biblio.getPages())){
                    biblio.setPages(biblio.getPages()+" PP");
                }
                
                palabrasClaves = biblio.getC650$a().split("-");

                System.out.print(cont++ + " => "+ biblio.getC082$a());
                
                int biblionumber = guardarBiblio(biblio);
                print(biblionumber, "biblio");
                if(biblionumber!=0){
                    
                    int biblioitemnumber = guardarBiblioitems(biblio, biblionumber);
                    print(biblioitemnumber, "biblioitems");
                    
                    if(biblioitemnumber!=0){  
                        
                        int id=guardarBiblio_metadata(biblio,biblionumber,palabrasClaves);
                        print(id, "biblio_metadata");
                        
                        if(id!=0){
                            List<Inventario_revista> ListaInventarios = new ArrayList<>();
                            //System.out.print(index + " = " + enlace + " => ");
                            ListaInventarios = getInventario(biblio);

                            //System.out.println("===" + ListaInventarios.size());  
                            for (Inventario_revista Inventario : ListaInventarios) {
                                 int itemnumber = guardarItems(biblio,biblionumber,biblioitemnumber,Inventario);
                                 print(itemnumber, "items");
                            }  
                            System.out.println();
                        }
                        
                        
                    }
                }                
               
            }

            conexion1.cerrarConsulta();
            conexion2.cerrarConsulta();
            conexion3.cerrarConsulta();

        } catch (SQLException ex) {
            System.out.print(" export_data_dspace_sql.Proceso.procesando() " + ex.getMessage());
            Logger.getLogger(ExportarDatos_Koha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void print(int resultado,String tablename){
        if(resultado==0){
            System.out.print(" => Error ("+tablename+")");
        }
        else{
            System.out.print(" => Correcto (" + tablename+")");
        }
    }

    public int guardarBiblio(Biblio_revista biblio) throws SQLException {
        /*insert into biblio(author,title,serial,copyrightdate,datecreated) values ('Chavez Zuñiga Diego','JAVA SPRING',0,'2018','2018-11-06')*/

        //System.out.println(pw_hash);
        String insert = "insert into biblio(author,title,serial,copyrightdate,datecreated) values(?,?,?,?,?)";

        PreparedStatement ps = null;
        int generatedkey = 0;

        try {
            //stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            //ps = conexion4.con.prepareStatement(insert);
            ps = conexion2.con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, biblio.getAutores()[0]);
            ps.setString(2, biblio.getTitulo());
            ps.setInt(3, 0);
            if(isNumeric(biblio.getAnyo())){
                ps.setString(4, biblio.getAnyo());
            }
            else{
                ps.setString(4, null);
            }
            ps.setString(5, "2018-11-06");

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
            }

        } catch (Exception ex) {
            System.out.print(" guardarBiblio() " + ex.getMessage());
            printStackTrace();
        } finally {
            try {
                ps.close();

            } catch (Exception ex) {
                System.out.print(" guardarBiblio() " + ex.getMessage());
                printStackTrace();
            }
        }
        return generatedkey;
    }
    public boolean isNumeric(String cadena) {
        boolean resultado;
        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException excepcion) {
            resultado = false;
        }
        return resultado;
    }
    public int guardarBiblioitems(Biblio_revista biblio, int biblionumber) throws SQLException {
        /*insert into biblioitems(biblionumber,itemtype,isbn,publishercode,editionstatement,pages,size,place)
values(27500,'TM001','9972973190','UNIVERSIDAD CATOLICA DE SANTA MARIA','3A ed.','758 páginas','22.0 cm','Arequipa')*/

        //System.out.println(pw_hash);
        String insert = "insert into biblioitems(biblionumber,itemtype,issn,publishercode,editionstatement,pages,size,place) values(?,?,?,?,?,?,?,?)";

        PreparedStatement ps = null;
        int generatedkey = 0;

        try {
            //stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            //ps = conexion4.con.prepareStatement(insert);
            ps = conexion2.con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, biblionumber);
            if(biblio.getCod3().equals("SUSC") || biblio.getCod3().equals("REVI")){
                ps.setString(2, "TM003");
            }
            else{
                ps.setString(2, "TM004");
            
            }
            ps.setString(3, biblio.getIssn());
            ps.setString(4, biblio.getEditorial());
            ps.setString(5, biblio.getEdicion());
            ps.setString(6, biblio.getPages());
            ps.setString(7, biblio.getSize());
            ps.setString(8, biblio.getLugar());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
            }

        } catch (Exception ex) {
            System.out.print(" guardarBiblioitems() " + ex.getMessage());
            printStackTrace();
        } finally {
            try {
                ps.close();

            } catch (Exception ex) {
                System.out.print(" guardarBiblioitems() " + ex.getMessage());
                printStackTrace();
            }
        }
        return generatedkey;
    }

    public List<Inventario_revista> getInventario(Biblio_revista biblio) {

        List<Inventario_revista> ListaInventarios = new ArrayList<>();
        try {
            String varSql = "select ri.Biblioteca,ri.Inventario,ri.Ejemplares,ri.Coleccion from Revistas_Inventarios ri where ri.Enlace='" + biblio.getEnlace() + "'";

            conexion3.varResultado = conexion3.stmt.executeQuery(varSql);

            Inventario_revista objInventario;

            while (conexion3.varResultado.next()) {
                objInventario = new Inventario_revista();
                objInventario.setBiblioteca(conexion3.varResultado.getString("Biblioteca"));                
                objInventario.setInventario(conexion3.varResultado.getString("inventario"));
                objInventario.setEjemplares(conexion3.varResultado.getString("ejemplares"));
                objInventario.setColeccion(conexion3.varResultado.getString("coleccion"));

               // System.out.print(objInventario.getA952$p() + ",");
                ListaInventarios.add(objInventario);
            }
            //System.out.println(ListaInventarios.toArray().toString());
            return ListaInventarios;

        } catch (SQLException ex) {
            Logger.getLogger(ProcesarRevistas.class.getName()).log(Level.SEVERE, null, ex);
            return ListaInventarios;
        }
    }
    public int guardarItems(Biblio_revista biblio, int biblionumber, int biblioitemnumber, Inventario_revista inv) throws SQLException {
        /*insert into items(biblionumber,biblioitemnumber,barcode,dateaccessioned,homebranch,
replacementpricedate,datelastseen,itemcallnumber,holdingbranch,location,permanent_location,cn_sort,ccode,itype,copynumber)
values(27500,27499,273942,'2018-11-06','BIBCE','2018-11-06','2018-10-22','531.52.CHA.00','BIBCE','BIBCE','BIBCE','531.52.CHA.00','CSSOC','TM001','1e.')*/

        //System.out.println(pw_hash);
        String insert = "insert into items(biblionumber,biblioitemnumber,barcode,dateaccessioned,homebranch," +
"replacementpricedate,datelastseen,itemcallnumber,holdingbranch,location,permanent_location,cn_sort,ccode,itype,copynumber) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement ps = null;
        int generatedkey = 0;

        try {
            //stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            //ps = conexion4.con.prepareStatement(insert);
            ps = conexion2.con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, biblionumber);
            ps.setInt(2, biblioitemnumber);
            ps.setString(3, inv.getInventario());
            ps.setString(4, "2018-11-06");
            ps.setString(5, inv.getBiblioteca());
            ps.setString(6, "2018-11-06");
            ps.setString(7, "2018-11-06");
            ps.setString(8, biblio.getC082$a());
            ps.setString(9, inv.biblioteca);
            ps.setString(10, inv.biblioteca);
            ps.setString(11, inv.biblioteca);
            ps.setString(12, biblio.getC082$a());
            ps.setString(13, inv.getColeccion());
           
            if(biblio.getCod3().equals("SUSC") || biblio.getCod3().equals("REVI")){             
                ps.setString(14, "TM003");
            }
            else{
                ps.setString(14, "TM004");
            
            }
            ps.setString(15, inv.ejemplares);
            

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
            }

        } catch (Exception ex) {
            System.out.print(" guardarItems() " + ex.getMessage());
            printStackTrace();
        } finally {
            try {
                ps.close();

            } catch (Exception ex) {
                System.out.print(" guardarItems() " + ex.getMessage());
                printStackTrace();
            }
        }
        return generatedkey;
    }
    public int guardarBiblio_metadata(Biblio_revista biblio,int biblionumber, String[] palabrasClaves) throws SQLException {

        String terminos="";
        for (int i=0; i<palabrasClaves.length;i++){
            terminos += " <datafield tag=\"650\" ind1=\" \" ind2=\" \">\n" +
                  "     <subfield code=\"a\">"+saltarCaracteres(palabrasClaves[i])+"</subfield>\n" +
                  "  </datafield>\n";
        
        }
        String autores="";
        if(biblio.getAutores().length>1){           
            for (int j=1; j<biblio.getAutores().length;j++){
                autores+="<datafield tag=\"700\" ind1=\" \" ind2=\" \">  \n" +
                        "    <subfield code=\"a\">"+saltarCaracteres(biblio.getAutores()[j])+"</subfield>\n" +
                        "</datafield>\n";
        
        }
        }
        String metadata="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<record\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\"\n" +
        "    xmlns=\"http://www.loc.gov/MARC21/slim\">\n" +
        "\n" +
        "  <leader>00619nam a2200193Ia 4500</leader>   \n" +
        "  <datafield tag=\"999\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"c\">"+biblionumber+"</subfield>\n" +
        "    <subfield code=\"d\">"+biblionumber+"</subfield>\n" +
        "  </datafield>\n" +
        "  <controlfield tag=\"003\">OSt</controlfield>\n" +
        "  <controlfield tag=\"005\">20181023043908.0</controlfield>\n" +
        "  <controlfield tag=\"008\">181022s9999||||xx |||||||||||||| ||spa||</controlfield>\n" +
        "  <datafield tag=\"040\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"c\">Transcribing agency </subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"082\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">"+biblio.getC082$a()+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"100\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">" + saltarCaracteres(biblio.getAutores()[0]) + "</subfield>\n" +
        "  </datafield>\n" +      
        "  <datafield tag=\"245\" ind1=\" \" ind2=\"0\">\n" +
        "    <subfield code=\"a\">"+saltarCaracteres(biblio.getTitulo()) +"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"250\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">"+saltarCaracteres(biblio.getEdicion())+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"260\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">"+saltarCaracteres(biblio.getLugar())+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"260\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"b\">"+saltarCaracteres(biblio.getEditorial())+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"260\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"c\">"+saltarCaracteres(biblio.getAnyo())+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"300\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">"+biblio.getPages()+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"300\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"c\">"+biblio.getSize()+"</subfield>\n" +
        "  </datafield>\n" +
        "  <datafield tag=\"505\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"a\">"+saltarCaracteres(biblio.getC505$a())+"</subfield>\n" +
        "  </datafield>\n" + terminos + autores +
        "  <datafield tag=\"942\" ind1=\" \" ind2=\" \">\n" +
        "    <subfield code=\"c\">"+TipoMaterial(biblio)+"</subfield>\n" +
        "    <subfield code=\"2\">ddc</subfield>\n" +
        "  </datafield>\n" +
        "</record>";

        //System.out.println(pw_hash);
        String insert = "insert into biblio_metadata(biblionumber,format,marcflavour,metadata) values(?,?,?,?)";

        PreparedStatement ps = null;
        int generatedkey = 0;

        try {
            //stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            //ps = conexion4.con.prepareStatement(insert);
            ps = conexion2.con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, biblionumber);
            ps.setString(2, "marcxml");
            ps.setString(3, "MARC21");
            ps.setString(4, metadata);
          
            

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
            }

        } catch (Exception ex) {
            System.out.print(" guardarBiblio_metadata() " + ex.getMessage());
            printStackTrace();
        } finally {
            try {
                ps.close();

            } catch (Exception ex) {
                System.out.print(" guardarBiblio_metadata() " + ex.getMessage());
                printStackTrace();
            }
        }
        return generatedkey;
    }
    public String TipoMaterial(Biblio_revista biblio){
        if(biblio.getCod3().equals("SUSC") || biblio.getCod3().equals("REVI")){
              return "TM003";
          }
          else{
             return "TM004";

          }
    }
    public String saltarCaracteres(String cadena){
    
        /*&amp; = &
        &lt; = <
        &gt; = >
        &apos; = '
        &quot; = "*/     
        if(cadena!=null){
            cadena=cadena.replace("&", "&amp;");
            cadena=cadena.replace("<", "&lt;");
            cadena=cadena.replace(">", "&gt;");
            cadena=cadena.replace("'", "&apos;");
            cadena=cadena.replace("\"", "&quot;");
        }
        else{
            cadena="";
        }
        return cadena;   
    }
}

class Biblio_revista {

    String enlace;
    String cod1;
    String cod2;
    String cod3;
    String cod4;
    String issn;
    String c082$a;
    String[] autores;
    String titulo;
    String edicion;
    String lugar;
    String editorial;
    String anyo;
    String pages;
    String size;
    String c505$a; // contenido
    String c650$a; // palabras claves
    String url;

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public String getCod1() {
        return cod1;
    }

    public void setCod1(String cod1) {
        this.cod1 = cod1;
    }

    public String getCod2() {
        return cod2;
    }

    public void setCod2(String cod2) {
        this.cod2 = cod2;
    }

    public String getCod3() {
        return cod3;
    }

    public void setCod3(String cod3) {
        this.cod3 = cod3;
    }

    public String getCod4() {
        return cod4;
    }

    public void setCod4(String cod4) {
        this.cod4 = cod4;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String isbn) {
        this.issn = isbn;
    }

    public String getC082$a() {
        return c082$a;
    }

    public void setC082$a(String c082$a) {
        this.c082$a = c082$a;
    }

    public String[] getAutores() {
        return autores;
    }

    public void setAutores(String[] autores) {
        this.autores = autores;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEdicion() {
        return edicion;
    }

    public void setEdicion(String edicion) {
        this.edicion = edicion;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getAnyo() {
        return anyo;
    }

    public void setAnyo(String anyo) {
        this.anyo = anyo;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getC505$a() {
        return c505$a;
    }

    public void setC505$a(String c505$a) {
        this.c505$a = c505$a;
    }

    public String getC650$a() {
        return c650$a;
    }

    public void setC650$a(String c650$a) {
        this.c650$a = c650$a;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "biblio{" + "enlace=" + enlace + ", cod1=" + cod1 + ", cod2=" + cod2 + ", cod3=" + cod3 + ", cod4=" + cod4 + ", issn=" + issn + ", c082$a=" + c082$a + ", autores=" + autores.length + ", titulo=" + titulo + ", edicion=" + edicion + ", lugar=" + lugar + ", editorial=" + editorial + ", anyo=" + anyo + ", pages=" + pages + ", size=" + size + ", c505$a=" + c505$a + ", c650$a=" + c650$a + ", url=" + url + '}';
    }
}

class Inventario_revista {

    String biblioteca;
    String inventario;
    String ejemplares;
    String coleccion;

    public String getBiblioteca() {
        return biblioteca;
    }

    public void setBiblioteca(String biblioteca) {
        this.biblioteca = biblioteca;
    }

    public String getInventario() {
        return inventario;
    }

    public void setInventario(String inventario) {
        this.inventario = inventario;
    }

    public String getEjemplares() {
        return ejemplares;
    }

    public void setEjemplares(String ejemplares) {
        this.ejemplares = ejemplares;
    }

    public String getColeccion() {
        return coleccion;
    }

    public void setColeccion(String coleccion) {
        this.coleccion = coleccion;
    }

    @Override
    public String toString() {
        return "InventarioSQL{" + "biblioteca=" + biblioteca + ", inventario=" + inventario + ", ejemplares=" + ejemplares + ", coleccion=" + coleccion + '}';
    }
    
}
