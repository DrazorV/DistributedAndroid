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

            try (Socket clientSocket = new Socket("localhost", port)) {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.writeObject("Consumer");
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line = inFromServer.readLine();

                while (temp) {
                    PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);

                    while (!line.equals("Done")) {
                        System.out.println(line);
                        line = inFromServer.readLine();
                    }

                    System.out.println("Type the bus lines you re interested in or type 'bye' to change broker.");

                    String busline = input.nextLine();

                    if (!busline.toLowerCase().equals("bye")) {
                        outToServer.println(busline);
                        String answer = inFromServer.readLine();

                        while (!answer.equals("next")) {
                            System.out.println(answer);
                            answer = inFromServer.readLine();
                        }
                        line = inFromServer.readLine();
                    } else {
                        temp = false;
                    }
                }
            } catch (ConnectException e) {
                if (i == 10) {
                    System.out.println("Connection with broker timed out, we couldn't find any broker.");
                    break;
                }
                System.out.println("Broker is not online try again in a moment!");
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
