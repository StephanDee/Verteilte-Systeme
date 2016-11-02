import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Startet Programm, nimmt Benutzereingaben entgegen und tätigt Ausgaben
 *
 * @author Stephan Dünkel
 * @className Main
 * @date 2016-10-25
 */
public class Main {

    public static CopyOnWriteArrayList<String[]> results = new CopyOnWriteArrayList<String[]>();
    // windows-1251
    // Cp850 - Windows DOS Kommandozeilen-Encodierung
    // CP437
    // Cp1252
    private static Scanner scn = new Scanner(System.in, "Cp850");
    // private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    static Thread suchThr1 = null;
    static Thread suchThr2 = null;


    public static void main(String[] args) {

        //Abfrage in einer Endlosschleife
        while (true) {

            //Was soll gemacht werden?
            System.out.println("Was willst du machen ?");
            System.out.println("Namen suchen (Name), Nummer suchen (Nummer), beides suchen (beides), Beenden (x)");
            String in = scn.nextLine();

            //Wenn x eingegeben wird, wird aus der Schleife gesprungen und das Programm beendet
            if (in.toLowerCase().equals("x")) {
                System.out.println("Programm wird beendet...");
                break;
            }

            //Entgegennehmen der Eingabe mit toLowerCase
            switch (in.toLowerCase()) {


                //Suche nach Name
                case "name":
                    if (!in.isEmpty()) {
                        System.out.print("Gib den Namen ein: ");
                        String eingabeNa = scn.nextLine();

                        //Suche starten mit Überprüfung, ob Eingabe leer ist
                        if (!eingabeNa.isEmpty()) {
                            starteName(eingabeNa);
                        } else {
                            System.err.println("Gib einen Namen ein!");
                        }

                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;

                //Suche nach Nummer
                case "nummer":
                    if (!in.isEmpty()) {
                        System.out.print("Gib die Nummer ein: ");
                        String eingabeNu = scn.nextLine();

                        //Suche starten mit Überprüfung, ob Eingabe leer ist
                        if (!eingabeNu.isEmpty()) {
                            starteNummer(eingabeNu);
                        } else System.err.println("Gib eine Nummer ein!");

                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;

                //Suche nach beidem, nacheinander
                case "beides":
                    if (!in.isEmpty()) {
                        //Name abfragen
                        System.out.print("Gib den Namen ein: ");
                        String eingabeNa = scn.nextLine();

                        //Suche starten mit Überprüfung, ob Eingabe leer ist
                        if (!eingabeNa.isEmpty()) {
                            starteName(eingabeNa);
                        } else System.err.println("Gib einen Namen ein!");

                        //Nummer abfragen
                        System.out.print("Gib die Nummer ein: ");
                        String eingabeNu = scn.nextLine();

                        //Suche starten mit Überprüfung, ob Eingabe leer ist
                        if (!eingabeNu.isEmpty()) {
                            starteNummer(eingabeNu);
                        } else System.err.println("Es muss etwas eingegeben werden!");


                    } else {
                        System.err.println("Es muss etwas eingegeben werden!");
                    }
                    break;
                default:
                    System.err.println("Treffe eine Auswahl, was getan werden soll");
                    break;
            }


            // Auf Threads warten
            try {
                if (suchThr1 != null) {
                    suchThr1.join();
                }
                if (suchThr2 != null) {
                    suchThr2.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Ausgabe des Results
            for (String[] row : results) {
                System.out.print(row[0]);
                if (row.length > 1) {
                    System.out.print(", " + row[1]);
                }
                System.out.println(); //Zeilenumbruch
            }

            // Neu initialisieren der Reuslt-Liste
            results = new CopyOnWriteArrayList<String[]>();

            System.out.println("---------------------------------------------------------------");
            System.out.println("");
        }
    }

    // Namensuche starten
    private static void starteName(String eingabeNa) {
        if ((eingabeNa.matches("^[a-zA-ZäüöÄÖÜ]+[a-zA-ZäöüÄÖÜ\\s]*"))) {
            suchThr1 = new Thread(new Suche(eingabeNa, 0, results), "search-name");
            suchThr1.start();
        } else {
            System.err.println("Nur Buchstaben eingeben!");
        }
    }

    // Nummernsuche starten
    private static void starteNummer(String eingabeNu) {
        if ((eingabeNu.matches("\\d+"))) {
            suchThr2 = new Thread(new Suche(eingabeNu, 1, results), "search-nummer");
            suchThr2.start();
        } else {
            System.err.println("Nur Zahlen eingeben!");
        }
    }
}

