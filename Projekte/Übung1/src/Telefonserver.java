import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Diese Klasse implementiert den Telefonserver.
 * Es realisiert eine nebenlaeufige Suchfunktion.
 *
 * @author Stephan Dünkel
 * @fileName Telefonserver.java
 * @date 09-11-2016
 */
public class Telefonserver {

    // (System.in)                 - IDE
    // (System.in, "windows-1252") - Windows Encodierungsverfahren
    // (System.in, "Cp850")        - Windows DOS Kommandozeilen-Encodierung
    private static final Scanner scn = new Scanner(System.in);

    // aktuell gesuchte Strings
    private String suchName = "";
    private String suchNummer = "";

    // Die Threads tragen Ergebnisse in den StringBuffer ein
    private final StringBuffer suchBuffer = new StringBuffer();

    // 2 String-Listen für das Telefonverzeichnis
    public final List<String> names;
    public final List<String> numbers;

    /**
     * Instanziiert 2 Arraylisten für Namen und Nummern.
     */
    public Telefonserver() {
        names = new ArrayList<>();
        numbers = new ArrayList<>();
    }

    /**
     * Sucht alle mit einem Suchstring uebereinstimmenden Tabelleneintraege
     *
     * @param str gesuchter String
     * @param in  'name': Suche nach Namen; 'tel': Suche nach Telefonnummer
     */
    public void search(String str, String in) {

        String currentThread = Thread.currentThread().getName(); // aktuell aufgerufener Thread
        List<String> searchList; // in dieser Liste wird gesucht
        boolean results = false; // Gefundene Suchergebnisse

        switch (in) {
            case "name":
                searchList = names;
                break;
            case "nummer":
                searchList = numbers;
                break;
            default:
                return;
        }

        // Iteriere ueber die Liste
        for (int i = 0; i < searchList.size(); i++) {
            String s = searchList.get(i); // Aktueller Listeneintrag

            // Schreibt uebereinstimmende Tabellenzeilen in den Ausgabebuffer
            if (s.equals(str)) {
                switch (in) {
                    case "name":
                        suchBuffer.append(currentThread + ": Name: " + s + ", Nummer: " + numbers.get(i) + "\n");
                        break;
                    case "nummer":
                        suchBuffer.append(currentThread + ": Name: " + names.get(i) + ", Nummer: " + s + "\n");
                        break;
                }
                results = true;
            }
        }
        if (!results) {
            System.out.println(currentThread + ": Die Suche nach '" + str + "' war erfolglos.");
        }
    }

    /**
     * Startet Programm, nimmt Benutzereingaben entgegen und tätigt Ausgaben
     *
     * @param args Befehlszeilen Argument
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        Thread t1;
        Thread t2;

        Telefonserver server = new Telefonserver();

        // erzeugt Telefonserver Testdaten
        server.addLine("Meier", "4711");
        server.addLine("Schmitt", "0815");
        server.addLine("Müller", "4711");
        server.addLine("Meier", "0816");
        server.addLine("von Schulze", "1234");

        String in;
        System.out.println("Was willst du machen?");
        System.out.println("Namen suchen (name), Nummer suchen (nummer), beides suchen (beides), Beenden (quit)");

        while (true) { // Endlosschleife

            // Leere den Ausgabebuffer
            server.suchBuffer.delete(0, server.suchBuffer.length());

            System.out.print(">> ");
            in = scn.nextLine().trim();

            if (in.toLowerCase().equals("quit")) {
                System.out.println("Programm wird beendet...");
                break; // Beendet das Programm
            }

            switch (in.toLowerCase()) {
                case "name": {
                    if (!in.isEmpty()) {
                        System.out.print("Gib einen Namen ein: ");
                        String inName = scn.nextLine();
                        inName.trim();
                        // inName.matches("[a-zA-ZäüöÄÖÜ]+[a-zA-ZäöüÄÖÜ\\s]*")
                        // !inName.matches("[\\s]+")
                        if (!inName.isEmpty() && !inName.matches("[\\s]+")) {
                            server.suchName = inName.trim();

                            // Initialisiere Thread 1
                            t1 = server.new ThreadNamenSuche();
                            t1.setName("threadName");

                            t1.start();
                            t1.join();
                        } else {
                            System.err.println("Gib einen Namen ein!");
                        }
                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;
                }
                case "nummer": {
                    if (!in.isEmpty()) {
                        System.out.print("Gib eine Nummer ein: ");
                        String inNummer = scn.nextLine();

                        // inNummer.matches("\\d+")
                        // !inNummer.matches("[\\s]+")
                        if (!inNummer.isEmpty() && !inNummer.matches("[\\s]+")) {
                            server.suchNummer = inNummer.trim();

                            // Initialisiere Thread 1
                            t1 = server.new ThreadNummerSuche();
                            t1.setName("threadNummer");

                            t1.start();
                            t1.join();
                        } else {
                            System.err.println("Gib eine Nummer ein!");
                        }
                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;
                }
                case "beides": {
                    if (!in.isEmpty()) {
                        System.out.print("Gib einen Namen ein: ");
                        String inName = scn.nextLine();

                        System.out.print("Gib eine Nummer ein: ");
                        String inNummer = scn.nextLine();

                        if (!inName.isEmpty() && !inName.matches("[\\s]+")) {
                            server.suchName = inName.trim();

                            if (!inNummer.isEmpty() && !inNummer.matches("[\\s]+")) {
                                server.suchNummer = inNummer.trim();

                                // Initialisierung der Threads
                                t1 = server.new ThreadNamenSuche();
                                t1.setName("threadName");
                                t2 = server.new ThreadNummerSuche();
                                t2.setName("threadNummer");

                                t1.start();
                                t2.start();
                                t1.join();
                                t2.join();
                            } else {
                                System.err.println("Gib eine Nummer ein!");
                            }
                        } else {
                            System.err.println("Gib einen Namen ein!");
                        }
                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;
                }
                default:
                    System.err.println("Treffe eine Auswahl! "
                            + "Namen suchen (name), Nummer suchen (nummer), beides suchen (beides), Beenden (quit)");
                    break;
            }

            // Gibt alle gefundenen Zeilen aus
            System.out.println(server.suchBuffer);
        }
    }

    /**
     * Konkateniert 2 Arraylisten miteinander.
     *
     * @param name
     * @param number
     */
    private void addLine(String name, String number) {
        names.add(name);
        numbers.add(number);
    }

    /**
     * Implementiert die Namenssuche.
     */
    private class ThreadNamenSuche extends Thread {

        @Override
        public void run() {
            search(suchName, "name");
        }
    }

    /**
     * Implementiert die Nummernsuche.
     */
    private class ThreadNummerSuche extends Thread {

        @Override
        public void run() {
            search(suchNummer, "nummer");
        }
    }
}