/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137

 */
package com.example.distributedandroid;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Publisher{
    static ArrayList<Value> values = new ArrayList<>();
    private static HashMap<String, ArrayList<Topic>> hashed;


    public static void main(String[] args) throws IOException, ParseException {
        String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println(current);
        PubUtilities.CreateNames();
        PubUtilities.CreateBuses();
        InputStream inputStream = new FileInputStream("app\\src\\main\\assets\\busLinesNew.txt");
        ArrayList<Topic> topics = BroUtilities.CreateBusLines(inputStream);
        hashed = BroUtilities.MD5(topics);
        System.out.println("Waiting for clients to connect...");
        int[] ports = {4322, 5432, 7654};
        for (int i = 0; i < 3; i++ ){
            Thread t = new Thread(new PubThread(ports[i]));
            t.start();
        }
    }

    public static class PubThread implements Runnable {
        private int port;

        PubThread(int port) {
            this.port = port;
        }

        public void run() {
            boolean temp = true;
            while(temp){
                try (Socket clientSocket = new Socket("localhost", port)) {
                    temp = false;
                    System.out.println("Sending to " + port);
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    out.writeObject("Publisher");
                    String broker;
                    Object inFromServer;
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    inFromServer = in.readObject();
                    if(inFromServer.toString().startsWith("Broker")) {
                        broker = inFromServer.toString().substring(6);
                        System.out.println("Got client " + broker + " !");
                        for (Value value: values){
                            switch (port){
                                case 4322:
                                    for(Topic buslines: Objects.requireNonNull(hashed.get("BrokerA"))) {
                                        if (buslines.getLineId().equals(value.getBus().getBuslineId())) out.writeObject(value);
                                    }
                                    break;
                                case 5432:
                                    for(Topic buslines: Objects.requireNonNull(hashed.get("BrokerB"))){
                                        if (buslines.getLineId().equals(value.getBus().getBuslineId())) out.writeObject(value);
                                    }
                                    break;
                                case 7654:
                                    for(Topic buslines: Objects.requireNonNull(hashed.get("BrokerC"))){
                                        if (buslines.getLineId().equals(value.getBus().getBuslineId())) out.writeObject(value);
                                    }
                                    break;
                            }
                            Thread.sleep(1000);
                        }
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    System.out.println("Connection with server timed out, we couldn't find what you asked for.");
                }
            }
        }
    }
}