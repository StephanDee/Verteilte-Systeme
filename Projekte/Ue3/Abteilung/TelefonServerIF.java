// RMI-Interface
import java.rmi.*;
import java.util.Date;

/**
 * Diese Klasse dient als Schnittstelle des Telefon-Servers fuer den zentralen Server
 * 
 * @author Stephan Duenkel
 * @date 22-01-2017
 * @fileName TelefonServerIF.java
 */
public interface TelefonServerIF extends Remote {
    void exit() throws Exception;
    String suche(String a, String b) throws Exception;
}