package com.example;

import java.util.ArrayList;


public class ClientManager {
    private Connection connection;
    private Menu menu;

    private boolean terminate;

    private boolean loggedIn = true;

    public void manage() {
        connection = new Connection();
        connection.connect("alumchat.lol");
        menu = new Menu();
        terminate = false;
        while (!terminate) {
            manageInitialOption();
        }
    }


    private void manageInitialOption() {
        boolean authorized = false;
        while (!authorized) {
            int initialOption = menu.showInitialMenu();
            int result = 0;

            if (initialOption == 1) {
                result = register();
            }
        }
    }
    private int register() {

    }

    private void reset_state() {
        terminate = false;
        loggedIn = true;
    }
}
