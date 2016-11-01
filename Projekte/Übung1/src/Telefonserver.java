import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Telefonserver, der Datensatz bereitstellt
 *
 * @author Stephan Dünkel
 * @className Telefonserver
 * @date 2016-10-25
 */

public class Telefonserver {

    private CopyOnWriteArrayList<String[]> server;

    public Telefonserver() {

        server = new CopyOnWriteArrayList<>();

        server.add(new String[]{"Dieter", "12345"});
        server.add(new String[]{"Hansi", "67890"});
        server.add(new String[]{"von Stockmeier", "030122"});
        server.add(new String[]{"Müller", "12345"});
        server.add(new String[]{"Hansi", "030110"});
    }

    public String[] getEntry(int column) {
        return server.get(column);
    }

    public int size() {
        return server.size();
    }

}
