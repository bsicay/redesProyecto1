package com.example;

import java.util.ArrayList;
import java.util.Scanner;



public class Menu {
    private Scanner scanner;
    public Menu() {
        scanner = new Scanner(System.in);
    }

    public int showInitialMenu() {
        boolean optionChosen = false;
        int option = 0;
        while (!optionChosen) {
            try {

                System.out.println("Ingresa la opcion que deseas:");
                System.out.println("1. Registrarse en el servidor");

                System.out.print("> ");
                option = Integer.parseInt(scanner.nextLine().trim());
                optionChosen = true;
            } catch (Exception e){
                System.out.println("Ingresa una opcion correcta");
            }
        }
        return option;
    }


}
