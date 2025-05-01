package io.github.apocRogue.database;

//No real storage but structure for when cloud database is set up
public class DBManager {


    public static void connect() { }

    //Temporarily logs in any user who types something
    public static boolean authenticate(String username, String password) {
        return username != null && !username.isEmpty()
            && password != null && !password.isEmpty();
    }

    //Temporarily registers any user who types something
    public static boolean register(String username, String password) {
        return username != null && !username.isEmpty()
            && password != null && !password.isEmpty();
    }


    public static void close() { }
}
