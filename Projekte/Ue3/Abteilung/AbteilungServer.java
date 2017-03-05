// RMI-Server
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;

/**
 * Diese Klasse realisiert den Abteilungs-Server
 * 
 * @author Werner Brecht
 * @date 22-01-2017
 * @fileName AbteilungServer.java
 */
public class AbteilungServer {
    public AbteilungServer() throws Exception {}
    
    public static void main(String[] args) throws Exception {
        
        // Parameter auswerten: Das ist der serverPort
        if(args.length!=1) {
            System.out.println("Parameter-Fehler");
            System.exit(0);
        }
        
        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(args[0]);
        }
        catch(Exception e) {
            System.out.println("Parameter ist keine Portnummer");
            System.exit(0);
        }
        
        if(serverPort < 9860 || serverPort > 9880) {
            System.out.println("Portnummer im falschen Bereich");
            System.exit(0);
        }
        
        // serverPort setzen
        RMISocketFactory.setSocketFactory(new ServerPortFix(serverPort));
        
        // serverHost ermitteln
        String serverHost = InetAddress.getLocalHost().getHostName();
        
        // Registry starten am Standard-Port 1099
        LocateRegistry.createRegistry(1099);
        
        // Referenz auf die Methodenimplementierung erzeugen
        VS_Ue3_TelefonServer server = new VS_Ue3_TelefonServer();
        
        // Referenz unter dem Namen AbtServer1 anmelden
        Naming.rebind("//"+serverHost+"/AbtServer1", server);
        
        // In Endlosschleife auf RMIs warten
        System.out.println("Der Server wartet auf RMIs");
    }
}