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

public class Publisher{
    static ArrayList<Value> values = new ArrayList<>();
    private static ArrayList<Topic> topics = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParseException {
        String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println(current);
        InputStream inputStream = new FileInputStream("app\\src\\main\\assets\\busLinesNew.txt");
        topics = BroUtilities.CreateBusLines(inputStream);
        PubUtilities.CreateNames();
        PubUtilities.CreateBuses();
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
                        for (Topic topic: topics){
                            out.writeObject(topic);
                            HashMap<String,Value> temp2 = new HashMap<>();
                            for (Value value : values) {
                                if (topic.getLineId().equals(value.getBus().getBuslineId())){

                                }
//                                    if (temp2.containsKey(value.getBus().getVehicleId())) {
//                                        if (temp2.get(value.getBus().getVehicleId()).getBus().getTime().compareTo(value.getBus().getTime()) < 0)
//                                            temp2.put(value.getBus().getVehicleId(), value);
//                                    } else {
//                                        temp2.put(value.getBus().getVehicleId(), value);
//                                    }
                            }
                            out.writeObject(temp2);
                            Thread.sleep(1000);
                        }
                    }
                    out.writeObject("Stop");
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    System.out.println("Connection with server timed out, we couldn't find what you asked for.");
                }
            }
        }
    }
}