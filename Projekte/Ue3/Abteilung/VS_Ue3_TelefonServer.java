import java.util.List;
import java.util.ArrayList;
import java.io.*;
//import java.util.Scanner;

import java.net.*;
import java.rmi.server.*;
import java.rmi.*;

/**
 * Diese Klasse realisiert die nebenläufige Namensuche in 2 Threads
 * 
 * @author Stephan Duenkel
 * @date 22-01-2017
 * @fileName VS_Ue3_TelefonServer.java
 */
public class VS_Ue3_TelefonServer extends UnicastRemoteObject implements TelefonServerIF {
    private static final String ILLEGAL_INPUT = "Ungueltige Eingabe!";
    
    // aktuelle Strings, nach denen gesucht wird.
    private String suchName = "";
    private String suchNummer = "";
    
    // Threads
    Thread t1;
    Thread t2;
    
    // Stringbuffer, in die die Threads ihre Ergebnisse eintragen.
    private final StringBuffer suchBuffer = new StringBuffer();
    
    // Telefonverzeichnis als 2 String-Listen
    public final List<String> names;
    public final List<String> numbers;

    public VS_Ue3_TelefonServer() throws Exception {
        names = new ArrayList<>();
        numbers = new ArrayList<>();
        
        // Erzeuge Testdaten
        this.addLine("Meyer", "4711");
        this.addLine("Schmidt", "0815");
        this.addLine("B\u00f6\u00df", "0815");
        this.addLine("Schulze", "4711");
        this.addLine("Meyer", "0816");
        this.addLine("von Goethe", "1524");
        this.addLine("Gl\u00f6\u00f6ckler", "4711");
        this.addLine("Gl\u00f6\u00f6ckler", "0816");
    }
    
    @Override
    public void exit() throws RemoteException {
        try {
            // serverHost ermitteln
            String serverHost = InetAddress.getLocalHost().getHostName();
            
            // Unregister ourself
            Naming.unbind("//"+serverHost+"/AbtServer1");
            
            // Unexport; this will also remove us from the RMI runtime
            UnicastRemoteObject.unexportObject(this, true);
            
            System.out.println("Abteilungsserver wird beendet!");
        }
        catch(Exception e){}
    }
    
    @Override
    public String suche(String a, String b) throws InterruptedException {
        
         // Leere den Ausgabebuffer
        suchBuffer.delete(0, suchBuffer.length());
        System.out.println(a + "; " + b);
        
        boolean aValid = (a != null && !a.trim().equals(""));
        boolean bValid = (b != null && !b.trim().equals(""));
        
        // Suche nur nach Namen
        if(aValid && !bValid) {
            suchName = a;
            
            // Initialisiere Thread 1
            t1 = new ThreadNamenSuche();
            t1.setName("NAME");
            
            t1.start();
            t1.join();
        }
        
        // Suche nur nach Nummer
        if(!aValid && bValid) {
            suchNummer = b;
            
            // Initialisiere Thread 1
            t1 = new ThreadNummerSuche();
            t1.setName("NUMMER");
            
            t1.start();
            t1.join();
        }
        
        // Suche Namen und Nummer
        if(aValid && bValid) {
            suchName = a;
            suchNummer = b;
            
            // Initialisiere Threads
            t1 = new ThreadNamenSuche();
            t1.setName("NAME");
            t2 = new ThreadNummerSuche();
            t2.setName("NUMMER");
            
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
        
        // Suche nichts
        if(!aValid && !bValid) {
            suchBuffer.append("Geben Sie mindestens einen Namen oder eine Telefonnummer ein!");
        }
        
        // Gebe alle gefundenen Zeilen aus.
        System.out.println(suchBuffer);
        
        return suchBuffer.toString();
    }
    
    /**
     * Sucht alle mit einem Suchstring uebereinstimmenden Tabelleneintraege
     * @param str String, nach dem gesucht wird
     * @param ch 'n': Suche nach Namen; 't': Suche nach Telefonnummer
     */
    private void search(String str, char ch) {
        
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