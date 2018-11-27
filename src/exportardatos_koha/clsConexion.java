/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exportardatos_koha;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class clsConexion {

    public Connection con;

    //public String errString = "";
    private String serverName = "10.0.2.62";//UCSM00230
    private String portNumber = "1433";
    private String databaseName = "BIBLIO_UCSM";
    private String url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + databaseName;
    // String url = "jdbc:sqlserver://UCSM00230;databaseName=BIBLIO_UCSM";

    private String userName = "admin_sql";
    private String password = "admin_sql4U";

    public Statement stmt;
    public ResultSet varResultado;

    public Connection Conectar() {
        con = null;
        String URL_bd = "jdbc:sqlserver://UCSM00230;databaseName=BIBLIO_UCSM";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(url, userName, password);
            //con = DriverManager.getConnection(URL_bd, "admin", "admin");
            //stmt = con.createStatement();
            //System.out.println("Conectado");
        } catch (Exception e) {
            //errString = "Error Mientras se conectaba a la Base de Datos";
            //System.out.println(errString);
            e.printStackTrace();
            return null;
        }
        return con;
    }

    public Connection ConectarPostgres() {
        con = null;
        String url = "jdbc:postgresql://10.0.6.57:5432/dspace";
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(url, "dspace", "dspace");
            System.out.println("Conectado");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(clsConexion.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error conexion postgresql: " + ex.getMessage());
            return null;
        }
        return con;
    }
    public Connection ConectarMysql() {
        con = null;
       // String url = "jdbc:mysql://10.0.113.46/koha_library";
        String url = "jdbc:mysql://10.0.6.58/koha_library";
        try {
            Class.forName("com.mysql.jdbc.Driver");
           // cnx = DriverManager.getConnection("jdbc:mysql://localhost/java_mysql", "root", "");
           
            con = DriverManager.getConnection(url, "root", "root");
           // stmt = con.createStatement();
          //  System.out.println("Conectado mysql");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(clsConexion.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error conexion mysql: " + ex.getMessage());
            return null;
        }
        return con;
    }

    public void Desconectar() {
        try {
            //stmt.close();
            con.close();
            System.out.println("Desonectado");
        } catch (SQLException e) {
            //errString = "Error Mientras se Cerraba la Conexion a la Base de Datos";
        }
    }

    public void abrirConsulta() throws SQLException {
        if (stmt == null) {
            stmt = Conectar().createStatement();
        }
    }

    public void abrirConsultaPostgresql() throws SQLException {
        if (stmt == null) {
            stmt = ConectarPostgres().createStatement();
        }
    }
     public void abrirConsultaMysql() throws SQLException {
        if (stmt == null) {
            stmt = ConectarMysql().createStatement();
        }
    }

    public void cerrarConsulta() throws SQLException {
        if (varResultado != null) {
            varResultado.close();
            varResultado = null;
        }
        if (stmt != null) {
            stmt.close();
            stmt = null;
        }

        /* if (varClsConexion != null) {
            varClsConexion.Desconectar();
        }*/
    }

}
