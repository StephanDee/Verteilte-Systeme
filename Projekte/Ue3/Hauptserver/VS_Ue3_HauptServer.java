import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.rmi.Naming;
//import java.util.Scanner;

/**
 * Diese Klasse realisiert den zentralen Server und die GUI fuer den Web-Browser
 * 
 * @author Stephan Duenkel
 * @date 22-01-2017
 * @fileName VS_Ue3_HauptServer.java
 */
public class VS_Ue3_HauptServer {
    
    /**
     * @param GET-String
     * @returns UTF-8-String
     */
    public static String toUtf8(String str) {
    
        str = str.replaceAll("%E4", "\u00E4");
        str = str.replaceAll("%F6", "\u00F6");
        str = str.replaceAll("%FC", "\u00FC");
        str = str.replaceAll("%C4", "\u00C4");
        str = str.replaceAll("%D6", "\u00D6");
        str = str.replaceAll("%DC", "\u00DC");
        str = str.replaceAll("%DF", "\u00DF");
        str = str.replaceAll("\\+", " ");
        
        return str;
    }
    
    /**
     * @param UTF-8-String
     * @returns HTML-String
     */
    public static String utf8ToHtml(String str) {
    
        str = str.replaceAll("\u00E4", "&auml;");
        str = str.replaceAll("\u00F6", "&ouml;");
        str = str.replaceAll("\u00FC", "&uuml;");
        str = str.replaceAll("\u00C4", "&Auml;");
        str = str.replaceAll("\u00D6", "&Ouml;");
        str = str.replaceAll("\u00DC", "&Uuml;");
        str = str.replaceAll("\u00DF", "&szlig;");
        
        return str;
    }
    
    public static void main(String[] args) throws InterruptedException, IOException {
        String suchErgebnis = "";
        
        // === Server-Client-Logik ===
        Socket cs;         // Client-Socket
        BufferedReader br; // liest User-Requests
        PrintWriter pw;    // schreibt die HTML-Seite
        String zeile;      // Eine Zeile aus dem Socket
        
        // Hostname und Portnummer
        final String hostname = InetAddress.getLocalHost().getHostName();
        final int port = 9871;
        
        TelefonServerIF abtServer;
        String serverHost;
        
        try {
            
            // Von der Registry auf serverHost mit dem Namen MeinDienst  
            // Referenz auf fernes Objekt holen
            serverHost = args[0];
            abtServer = (TelefonServerIF) Naming.lookup("//"+serverHost+"/AbtServer1");
        }
        catch(Exception e) {
            System.out.println(e);
            return;
        }
                
        final String HTML_FORM =
        "HTTP/1.1 200 OK\n"
      + "Content-Type: text/html\n"
      + "\n"
      + "<html>\n"
          + "<body>\n"
              + "<h2 align=center>Telefonverzeichnis</h2>\n"
              + "<h3>Sie k&ouml;nnen nach Name oder nach Telefonnummer oder nach beiden (nebenl&auml;ufig) suchen.</h3>\n"
              + "<form method=get action=\"http://"+hostname+":"+port+"\">\n"
                  + "<table>\n"
                      + "<tr> <td valign=top>Name:</td>   <td><input name=A></td> <td></td> </tr>\n"
                      + "<tr> <td valign=top>Nummer:</td> <td><input name=B></td> <td></td> </tr>\n"
                      + "<tr> <td valign=top><button type=\"submit\" name=O value=\"search\">Suchen</button></td>\n"
                      + "<td><input type=reset></td>\n"
                      + "<td><button type=\"submit\" name=O value=\"exit\">Beenden</button></td> </tr>\n"
                  + "</table>\n"
              + "</form>\n"
          + "</body>\n"
      + "</html>\n";
        
        ServerSocket ss = new ServerSocket(port);
        System.out.println(hostname + ":" + port);
        
        while(true) {
            cs = ss.accept();
            
            br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            zeile = br.readLine();
            System.out.println("Kontrollausgabe: "+zeile);
            
            // Favicon-Requests ignorieren
            if(zeile.startsWith("GET /favicon")) {
                System.out.println("Favicon-Request");
                br.close();
                continue;
            }
            
            // Nur GET-Requests bearbeiten
            if(!zeile.startsWith("GET ")) {
                System.out.println("Kein GET-Request");
                br.close();
                continue;
            }
            
            // bearbeite GET-Request
            pw = new PrintWriter(cs.getOutputStream());
            zeile = zeile.replaceAll("GET /", "").replaceAll("HTTP/1.1", "").trim();
            //System.out.println(zeile);
            
            // Server wird erstmals angesprochen
            if(zeile.equals("")) {
                System.out.println("Lade GUI");
                pw.print(HTML_FORM);
                pw.flush();
                br.close();
                continue;
            }
            
            // GET-Request auswerten
            zeile = toUtf8(zeile.substring(1));
            //System.out.println(zeile);
            
            String a = null;
            String b = null;
            String o = null;
            
            String[] vars = zeile.split("&");
            for(String str : vars) {
                String[] key_value = str.split("=");
                if(key_value.length < 2) continue;
                
                String key = key_value[0];
                String val = key_value[1];
                
                switch(key) {
                    case "A" : a = val; break;
                    case "B" : b = val; break;
                    case "O" : o = val; break;
                }
            }
            
            // Server beenden
            if(o != null && o.equals("exit")) {
                String exitSite = 
                    "HTTP/1.1 200 OK\n"
                  + "Content-Type: text/html\n"
                  + "\n"
                  + "<html>\n"
                      + "<body>\n"
                          + "<h1><font color=red>\n"
                          + "Auf Wiedersehen!\n"
                          + "</font></h1>\n"
                      + "</body>\n"
                  + "</html>\n";
                System.out.println("Server wird beendet!");
                pw.print(exitSite);
                pw.flush();
                br.close();
                
                // Beende den Abteilungsserver
                try {
                    abtServer.exit();
                }
                catch(Exception e) {
                    System.out.println(e);
                }
                
                break;
            }
                
            // Suche (RMI-Client)
            if(o != null && o.equals("search")) {
                System.out.println("Der Client startet");
                
                // Param auswerten: Das ist der serverHost
                if(args.length!=1) {
                    System.out.println("Parameter-Fehler");
                    System.exit(0);
                }
                
                // Der ferne Methodenaufruf
                try {
                    suchErgebnis = abtServer.suche(a, b);
                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
            
            // Gebe alle gefundenen Zeilen aus.
            System.out.println(suchErgebnis);
            
            String html =
                "HTTP/1.1 200 OK\n"
              + "Content-Type: text/html\n"
              + "\n"
              + "<html>\n"
                  + "<body>\n"
                      + "<h1><font color=green>\n"
                      + utf8ToHtml(suchErgebnis) + "\n"
                      + "</font></h1>\n"
                      + "<form method=get action=\"http://"+hostname+":"+port+"\">\n"
                          + "<button type=\"submit\">Zur&uuml;ck</button>\n"
                      + "</form>\n"
                  + "</body>\n"
              + "</html>\n";
            
            pw.print(html);
            pw.flush();
            br.close();
        }
        pw.close();
        cs.close();
        ss.close();
    }
}