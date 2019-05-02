/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137

 */
package com.example.DistributedSystems;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class ConsumerA {

    private static int port;
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        int i = 0;
        while (true) {
            boolean temp = true;
            System.out.println("Please choose between our three brokers: ");
            System.out.println("For BrokerA press: A");
            System.out.println("For BrokerA press: B");
            System.out.println("For BrokerA press: C");
            String broker = input.nextLine();

            if (broker.toUpperCase().equals("A")) port = 4321;
            if (broker.toUpperCase().equals("B")) port = 5432;
            if (broker.toUpperCase().equals("C")) port = 7654;


        }
    }
}
