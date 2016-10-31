package Uebung1;

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

        server.add(new String[]{"Meier", "4711"});
        server.add(new String[]{"Schmitt", "0815"});
        server.add(new String[]{"Müller", "4711"});
        server.add(new String[]{"Meier", "0816"});
        server.add(new String[]{"von Stockmeier", "1234"});
    }

    public String[] getEntry(int column) {
        return server.get(column);
    }

    public int size() {
        return server.size();
    }

}
