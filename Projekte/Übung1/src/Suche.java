import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kann von einem Thread aufgerufen werden und durchsucht eine Liste
 *
 * @author Stephan Dünkel
 * @className Suche
 * @date 2016-10-25
 */
public class Suche implements Runnable {

    private Telefonserver server = new Telefonserver();

    private String str;
    private int column;
    private CopyOnWriteArrayList<String[]> results;

    /**
     * Konfiguriert die Suche durch setzen den Suchstring und die Spalte in welcher dieser gesucht werden soll.
     *
     * @param str
     * @param column
     * @param results
     */
    public Suche(String str, int column, CopyOnWriteArrayList<String[]> results) {
        this.str = str;
        this.column = column;
        this.results = results;
    }

    @Override
    public void run() {
        suchen();
    }

    /**
     * Iteriert über den Telefonserver und vergleicht die Einträge mit dem definierten Such-String, werden
     * Übereinstimmungen gefunden so werden diese als String zurückgeliefert
     *
     * @return
     */
    private void suchen() {

        for (int i = 0; i < server.size(); i++) {
            if (server.getEntry(i)[column].equals(str)) {
                results.add(server.getEntry(i));
            }
        }

        if (results.isEmpty()) {
            results.add(new String[]{"Die Suche nach \"" + str + "\" war erfolglos"});
        }

    }

}
