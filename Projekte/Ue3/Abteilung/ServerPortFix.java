// Port-Fix
import java.rmi.server.*;
import java.net.*;
import java.io.*;

/**
 * Diese Klasse loest das Server-Port-Problem fuer den Uebungsraum
 * 
 * @author Werner Brecht
 * @date 22-01-2017
 * @fileName ServerPortFix.java
 */
public class ServerPortFix extends RMISocketFactory {
    int myPort = 0;
    ServerPortFix(int port) { myPort = port; }
    
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }
    
    public ServerSocket createServerSocket(int port) throws IOException {
        if(port==0) port = myPort;
        return new ServerSocket(port);
    }
}