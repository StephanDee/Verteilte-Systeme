import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
//import java.util.Scanner;

/**
 * Diese Klasse demonstriert eine nebenlaeufige Suche in einer Tabelle.
 * 
 * @author Stephan Dünkel
 * @date 07-11-2016
 * @fileName VS_Ue2.java
 */
public class VS_Ue2 {
    private static final String ILLEGAL_INPUT = "Ungueltige Eingabe!";
    
    // aktuelle Strings, nach denen gesucht wird.
    private String suchName = "";
    private String suchNummer = "";
    
    // Stringbuffer, in die die Threads ihre Ergebnisse eintragen.
    private final StringBuffer suchBuffer = new StringBuffer();
    
    // Telefonverzeichnis als 2 String-Listen
    public final List<String> names;
    public final List<String> numbers;

    public VS_Ue2() {
        names = new ArrayList<>();
        numbers = new ArrayList<>();
    }
    
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
    
    /**
     * Sucht alle mit einem Suchstring uebereinstimmenden Tabelleneintraege
     * @param str String, nach dem gesucht wird
     * @param ch 'n': Suche nach Namen; 't': Suche nach Telefonnummer
     */
    public void search(String str, char ch) {
        
        String curThread = Thread.currentThread().getName(); // Thread, der diese Methode aufruft
        List<String> searchList; // Liste, in der gesucht wird
        boolean anyResult = false; // Gibt an, ob Suchergebnisse gefunden wurden.
        
        switch(ch) {
            case 'n' : searchList = names; break;
            case 't' : searchList = numbers; break;
            default : return;
        }
        
        // Iteriere ueber die Liste
        for(int i = 0; i < searchList.size(); i++) {
            String s = searchList.get(i); // Aktueller Listeneintrag
            
            // Schreibt die uebereinstimmen Tabellenzeilen in den Ausgabebuffer.
            if(s.equals(str)) {
                switch(ch) {
                    case 'n' : suchBuffer.append(curThread + ": Name: " + s + "; Nummer: " + numbers.get(i) + "\n"); break;
                    case 't' : suchBuffer.append(curThread + ": Name: " + names.get(i) + "; Nummer: " + s + "\n"); break;
                }
                anyResult = true;
            }
        }
        if(!anyResult) {
			suchBuffer.append(curThread + ": Keine Treffer bei der Suche nach " + str + "!");
        }
    }
    
    public static void main(String[] args) throws InterruptedException, IOException {
        
        // === Programmlogik ===
        Thread t1;
        Thread t2;
        
        VS_Ue2 ue1 = new VS_Ue2();
        
        // Erzeuge Testdaten
        ue1.addLine("Meyer", "4711");
        ue1.addLine("Schmidt", "0815");
        ue1.addLine("B\u00f6\u00df", "0815");
        ue1.addLine("Schulze", "4711");
        ue1.addLine("Meyer", "0816");
        ue1.addLine("von Goethe", "1524");
        ue1.addLine("Gl\u00f6\u00f6ckler", "4711");
        ue1.addLine("Gl\u00f6\u00f6ckler", "0816");
        
        // === Server-Client-Logik ===
        Socket cs;         // Client-Socket
        BufferedReader br; // liest User-Requests
        PrintWriter pw;    // schreibt die HTML-Seite
        String zeile;      // Eine Zeile aus dem Socket
        
        // Hostname und Portnummer
        final String hostname = InetAddress.getLocalHost().getHostName();
        final int port = 9871;
      
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
      + "<tr> <td valign=top>Name:</td>    <td><input name=A></td>    <td></td> </tr>\n"
      + "<tr> <td valign=top>Nummer:</td> <td><input name=B></td>    <td></td> </tr>\n"
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
                break;
            }
            
             // Leere den Ausgabebuffer
            ue1.suchBuffer.delete(0, ue1.suchBuffer.length());
            System.out.println(a + "; " + b + "; " + o);
            
            // Suche
            if(o != null && o.equals("search")) {
                boolean aValid = (a != null && !a.trim().equals(""));
                boolean bValid = (b != null && !b.trim().equals(""));
                
                // Suche nur nach Namen
                if(aValid && !bValid) {
                    ue1.suchName = a;
                    
                    // Initialisiere Thread 1
                    t1 = ue1.new ThreadNamenSuche();
                    t1.setName("NAME");
                    
                    t1.start();
                    t1.join();
                }
                
                // Suche nur nach Nummer
                if(!aValid && bValid) {
                    ue1.suchNummer = b;
                    
                    // Initialisiere Thread 1
                    t1 = ue1.new ThreadNummerSuche();
                    t1.setName("NUMMER");
                    
                    t1.start();
                    t1.join();
                }
                
                // Suche Namen und Nummer
                if(aValid && bValid) {
                    ue1.suchName = a;
                    ue1.suchNummer = b;
                    
                    // Initialisiere Threads
                    t1 = ue1.new ThreadNamenSuche();
                    t1.setName("NAME");
                    t2 = ue1.new ThreadNummerSuche();
                    t2.setName("NUMMER");
                    
                    t1.start();
                    t2.start();
                    t1.join();
                    t2.join();
                }
                
                // Suche nichts
                if(!aValid && !bValid) {
                    ue1.suchBuffer.append("Geben Sie mindestens einen Namen oder eine Telefonnummer ein!");
                }
            }
            
            // Gebe alle gefundenen Zeilen aus.
            System.out.println(ue1.suchBuffer);

            String html =
                "HTTP/1.1 200 OK\n"
              + "Content-Type: text/html\n"
              + "\n"
              + "<html>\n"
              + "<body>\n"
              + "<h1><font color=green>\n"
              + utf8ToHtml(ue1.suchBuffer.toString()) + "\n"
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
    
    private void addLine(String name, String number) {
        names.add(name);
        numbers.add(number);
    }
    
    private class ThreadNamenSuche extends Thread {
        
        @Override
        public void run() {
            search(suchName, 'n');
        }
    }
    
    private class ThreadNummerSuche extends Thread {
        
        @Override
        public void run() {
            search(suchNummer, 't');
        }
    }
}