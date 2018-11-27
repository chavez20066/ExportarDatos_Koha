/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exportardatos_koha;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
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

/**
 *
 * @author jbasurco
 */
public class Proceso {

    /*private Statement stmt;
    private ResultSet varResultado;
    private Connection con;*/
    public void procesando() throws SQLException, FileNotFoundException, IOException {

        try {

            String varSql = "select distinct  l.enlace,cod1,cod2,cod3,cod4,CONVERT(varchar(20), l.Pri_isbn) as'020$a',l.cod1+'.'+l.cod2+'.'+l.cod3+'.'+l.cod4 as '082$a',"
                    + "Autor as '100$a','Autor' as '100$e', \n"
                    + "titulo as '245$a',\n"
                    + "Edicion as '250$a', Lugar as '260$a', Editorial as '260$b', Anyo as '260$c',\n"
                    + "CONVERT(varchar(20), Paginas)+' páginas' as '300$a', CONVERT(varchar(10), Longitud)+' cm'  as '300$c',\n"
                    + "/*'' as '490$a', '' as '490$v',*/\n"
                    + "Contenido as '505$a',\n"
                    + "PalabrasClave as '650$a',\n"
                    + "/*URL as '856$u',*/\n"
                    + "'TM001' as '942$c' \n"
                    /* + "'BIBIM' as '952$a',\n"
                    + "'BIBIM' as '952$b',\n"
                    + "'BIBIM' as '952$c',\n"
                    + "cod1+'.'+cod2+'.'+cod3+'.'+cod4 as '952$o',\n"
                    + "il.Inventario as '952$p',\n"
                    + "il.Ejemplares as '952$t',\n"
                    + "'BK' as '952$y'\n"*/
                    + "from libros l \n"
                    + "inner join idiomas i on l.idioma=i.cIdIdioma \n"
                    + "inner join Inventario_libros il on l.enlace=il.enlace \n"
                    + "where l.enlace !='10751' and l.Enlace!='29334'";

            System.out.println("SQL=>" + varSql);

            clsConexion conexion1 = new clsConexion();
            conexion1.abrirConsulta();
            conexion1.varResultado = conexion1.stmt.executeQuery(varSql);

            //List<String> ResultTesis=new ArrayList<>();
            JSONObject varJsonObjectP = new JSONObject();
            JSONArray varJsonArrayP = new JSONArray();

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("Excel Sheet");
            HSSFRow rowhead = sheet.createRow((short) 0);
            rowhead.createCell((short) 0).setCellValue("020$a");
            rowhead.createCell((short) 1).setCellValue("082$a");
            rowhead.createCell((short) 2).setCellValue("100$a");
            rowhead.createCell((short) 3).setCellValue("100$e");
            rowhead.createCell((short) 4).setCellValue("245$a");
            rowhead.createCell((short) 5).setCellValue("250$a");
            rowhead.createCell((short) 6).setCellValue("260$a");
            rowhead.createCell((short) 7).setCellValue("260$b");
            rowhead.createCell((short) 8).setCellValue("260$c");
            rowhead.createCell((short) 9).setCellValue("300$a");
            rowhead.createCell((short) 10).setCellValue("300$c");
            rowhead.createCell((short) 11).setCellValue("505$a");
            //PALABRAS CLAVES MAXIMO 5
            rowhead.createCell((short) 12).setCellValue("650$a"); //PALABRAS CLAVES
            rowhead.createCell((short) 13).setCellValue("650$a");
            rowhead.createCell((short) 14).setCellValue("650$a");
            rowhead.createCell((short) 15).setCellValue("650$a");
            rowhead.createCell((short) 16).setCellValue("650$a");
            // FIN PALABRAS CLAVES
            rowhead.createCell((short) 17).setCellValue("942$c");
            /*inventario*/
 /*         rowhead.createCell((short) 18).setCellValue("952$a");
            rowhead.createCell((short) 19).setCellValue("952$b");
            rowhead.createCell((short) 20).setCellValue("952$c");
            rowhead.createCell((short) 21).setCellValue("952$o");
            rowhead.createCell((short) 22).setCellValue("952$p");
            rowhead.createCell((short) 23).setCellValue("952$t");
            rowhead.createCell((short) 24).setCellValue("952$y");*/

            for (int i = 18; i <= (7 * 31) + 17; i++) { //7 * 5 =35   17+35=52  (maximo 5 items)
                rowhead.createCell((short) i).setCellValue("952$a");//18 ,
                i++;
                rowhead.createCell((short) i).setCellValue("952$b");
                i++;
                rowhead.createCell((short) i).setCellValue("952$c");
                i++;
                rowhead.createCell((short) i).setCellValue("952$o");
                i++;
                rowhead.createCell((short) i).setCellValue("952$p");
                i++;
                rowhead.createCell((short) i).setCellValue("952$t");
                i++;
                rowhead.createCell((short) i).setCellValue("952$y");//24

            }

            String c020$a, c082$a, c100$a, c100$e, c245$a, c250$a, c260$a, c260$b, c260$c, c300$a, c300$c, c505$a, c650$a, c942$c;
            String c952$a, c952$b, c952$c, c952$o, c952$p, c952$t, c952$y;
            String cod1, cod2, cod3, cod4;
            String enlace;

            String palabrasClaves[];

            int index = 1, limite = 20;
           
            while (conexion1.varResultado.next()) {
                
                /*codigoTesis = conexion1.varResultado.getString("codigoTesis");*/
                cod1 = conexion1.varResultado.getString("cod1");
                cod2 = conexion1.varResultado.getString("cod2");
                cod3 = conexion1.varResultado.getString("cod3");
                cod4 = conexion1.varResultado.getString("cod4");

                c020$a = conexion1.varResultado.getString("020$a");
                c082$a = conexion1.varResultado.getString("082$a");
                c100$a = conexion1.varResultado.getString("100$a");
                c100$e = conexion1.varResultado.getString("100$e");
                c245$a = conexion1.varResultado.getString("245$a");
                c250$a = conexion1.varResultado.getString("250$a");
                c260$a = conexion1.varResultado.getString("260$a");
                c260$b = conexion1.varResultado.getString("260$b");
                c260$c = conexion1.varResultado.getString("260$c");
                c300$a = conexion1.varResultado.getString("300$a");
                c300$c = conexion1.varResultado.getString("300$c");
                c505$a = conexion1.varResultado.getString("505$a");
                c650$a = conexion1.varResultado.getString("650$a"); //PALABRAS CLAVES
                c942$c = conexion1.varResultado.getString("942$c");
                enlace = conexion1.varResultado.getString("enlace");
                /*                c952$a = conexion1.varResultado.getString("952$a");
                c952$b = conexion1.varResultado.getString("952$b");
                c952$c = conexion1.varResultado.getString("952$c");
                c952$o = conexion1.varResultado.getString("952$o");
                c952$p = conexion1.varResultado.getString("952$p");
                c952$t = conexion1.varResultado.getString("952$t");
                c952$y = conexion1.varResultado.getString("952$y");*/
                palabrasClaves = c650$a.split("-");
                HSSFRow row = sheet.createRow((short) index);
                int j = 0;
                row.createCell((short) j).setCellValue(c020$a);
                j++;
                row.createCell((short) j).setCellValue(c082$a);
                j++;
                row.createCell((short) j).setCellValue(c100$a);
                j++;
                row.createCell((short) j).setCellValue(c100$e);
                j++;
                row.createCell((short) j).setCellValue(c245$a);
                j++;
                row.createCell((short) j).setCellValue(c250$a);
                j++;
                row.createCell((short) j).setCellValue(c260$a);
                j++;
                row.createCell((short) j).setCellValue(c260$b);
                j++;
                row.createCell((short) j).setCellValue(c260$c);
                j++;
                row.createCell((short) j).setCellValue(c300$a);
                j++;
                row.createCell((short) j).setCellValue(c300$c);
                j++;
                row.createCell((short) j).setCellValue(c505$a);
                j++;
                //12,13,14,15,16
                if (palabrasClaves.length > 1) {
                    for (int i = 0; i < palabrasClaves.length && palabrasClaves.length < 5; i++) {
                        row.createCell((short) j).setCellValue(palabrasClaves[i]);
                        j++;
                    }
                } else {
                    row.createCell((short) j).setCellValue(c650$a);
                    j++; //palabras claves
                }
                j = 17;
                row.createCell((short) j).setCellValue(c942$c);
                j++;

                //inventario
                /*
                row.createCell((short) j).setCellValue(c952$a);
                j++;
                row.createCell((short) j).setCellValue(c952$b);
                j++;
                row.createCell((short) j).setCellValue(c952$c);
                j++;
                row.createCell((short) j).setCellValue(c952$o);
                j++;
                row.createCell((short) j).setCellValue(c952$p);
                j++;
                row.createCell((short) j).setCellValue(c952$t);
                j++;
                row.createCell((short) j).setCellValue(c952$y);
                j++;*/
                List<Inventario> ListaInventarios = new ArrayList<>();
                System.out.print(index + " = " + enlace + " => ");
                ListaInventarios = getInventario(cod1, cod2, cod3, cod4, enlace);
                System.out.println("==="+ListaInventarios.size());

                if (ListaInventarios.size() == 1) {
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$a());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$b());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$c());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$o());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$p());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$t());
                    j++;
                    row.createCell((short) j).setCellValue(ListaInventarios.get(0).getA952$y());
                    j++;
                } else {
                    for (int y = 0; y < ListaInventarios.size(); y++) {
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$a());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$b());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$c());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$o());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$p());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$t());
                        j++;
                        row.createCell((short) j).setCellValue(ListaInventarios.get(y).getA952$y());
                        j++;
                    }
                }
                index++;
            }
            FileOutputStream fileOut = new FileOutputStream("e:\\excelFile.xls");
            wb.write(fileOut);
            fileOut.close();
            System.out.println("Data is saved in excel file.");

            conexion1.cerrarConsulta();

        } catch (SQLException ex) {
            System.out.println("export_data_dspace_sql.Proceso.procesando()" + ex.getMessage());
            Logger.getLogger(ExportarDatos_Koha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Inventario> getInventario(String cod1, String cod2, String cod3, String cod4, String enlace) {

        List<Inventario> ListaInventarios = new ArrayList<>();
        try {
            String varSql = "select "
                    + "l.enlace, "
                    + "il.Biblioteca as '952$a',\n"
                    + "il.Biblioteca as '952$b',\n"
                    + "il.Biblioteca as '952$c',\n"
                    + "cod1+'.'+cod2+'.'+cod3+'.'+cod4 as '952$o',\n"
                    + "il.Inventario as '952$p',\n"
                    + "il.Ejemplares as '952$t',\n"
                    + "'TM001' as '952$y'\n"
                    + "from libros l \n"
                    + "inner join idiomas i on l.idioma=i.cIdIdioma \n"
                    + "inner join Inventario_libros il on l.enlace=il.enlace \n"
                    + "where cod1='" + cod1 + "' and cod2='" + cod2 + "' and cod3='" + cod3 + "' and cod4='" + cod4 + "';";

            //System.out.println(varSql);
            clsConexion conexion2 = new clsConexion();
            conexion2.abrirConsulta();
            conexion2.varResultado = conexion2.stmt.executeQuery(varSql);

          

            Inventario objInventario;

            while (conexion2.varResultado.next()) {
                objInventario = new Inventario();
                objInventario.setA952$a(conexion2.varResultado.getString("952$a"));
                objInventario.setA952$b(conexion2.varResultado.getString("952$b"));
                objInventario.setA952$c(conexion2.varResultado.getString("952$c"));
                objInventario.setA952$o(conexion2.varResultado.getString("952$o"));
                objInventario.setA952$p(conexion2.varResultado.getString("952$p"));
                objInventario.setA952$t(conexion2.varResultado.getString("952$t"));
                objInventario.setA952$y(conexion2.varResultado.getString("952$y"));                
                System.out.print(objInventario.getA952$p()+",");                
                ListaInventarios.add(objInventario);
            }
            //System.out.println(ListaInventarios.toArray().toString());
            return ListaInventarios;

        } catch (SQLException ex) {
            Logger.getLogger(Proceso.class.getName()).log(Level.SEVERE, null, ex);
            return ListaInventarios;
        }

    }
}

class Inventario {

    String a952$a;
    String a952$b;
    String a952$c;
    String a952$o;
    String a952$p;
    String a952$t;
    String a952$y;

    public String getA952$a() {
        return a952$a;
    }

    public void setA952$a(String a952$a) {
        this.a952$a = a952$a;
    }

    public String getA952$b() {
        return a952$b;
    }

    public void setA952$b(String a952$b) {
        this.a952$b = a952$b;
    }

    public String getA952$c() {
        return a952$c;
    }

    public void setA952$c(String a952$c) {
        this.a952$c = a952$c;
    }

    public String getA952$o() {
        return a952$o;
    }

    public void setA952$o(String a952$o) {
        this.a952$o = a952$o;
    }

    public String getA952$p() {
        return a952$p;
    }

    public void setA952$p(String a952$p) {
        this.a952$p = a952$p;
    }

    public String getA952$t() {
        return a952$t;
    }

    public void setA952$t(String a952$t) {
        this.a952$t = a952$t;
    }

    public String getA952$y() {
        return a952$y;
    }

    public void setA952$y(String a952$y) {
        this.a952$y = a952$y;
    }

    @Override
    public String toString() {
        return "Inventario{" + "a952$p=" + a952$p + '}';
    }
    
    

}
