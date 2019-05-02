/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137

 */
package com.example.distributedandroid;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public  class BrokerC {
    private static ArrayList<Topic> topics = new ArrayList<>();


    public static void main(String[] args) throws IOException{
        BroUtilities.CreateBusLines(topics);
        ServerSocket providerSocket = new ServerSocket(7654, 3);
        try {
            while (true) {
                Socket connection = providerSocket.accept();
                Thread t = new Thread(new ComunicationWithConsumerThread(connection));
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Not able to open the port", e);
        }
    }

    public static class ComunicationWithConsumerThread implements Runnable {
        private Socket connected;
        private volatile static HashMap<Topic,HashMap<String, Value>> output = new HashMap<>();

        ComunicationWithConsumerThread(Socket connected) {
            this.connected = connected;
        }

        public void run() {
            synchronized (this) {
                try {
                    ObjectInputStream in = new ObjectInputStream(connected.getInputStream());
                    Object inFromServer = in.readObject();
                    if (inFromServer.toString().equals("Consumer")) {
                        while (true) {
                            System.out.println("THE CLIENT" + " " + connected.getInetAddress() + ":" + connected.getPort() + " IS CONNECTED ");
                            PrintWriter outToClient = new PrintWriter(connected.getOutputStream(), true);
                            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));
                            HashMap<String, ArrayList<Topic>> hashed = BroUtilities.MD5(topics);
                            outToClient.println("\n--------------------------------------------------------------------------\n");
                            outToClient.println("I am broker C and I am responsible for these keys");
                            for (Topic topic : hashed.get("BrokerC")) outToClient.println(topic.getLineId());
                            outToClient.println("Broker A is responsible for these Keys");
                            for (Topic topic : hashed.get("BrokerA")) outToClient.println(topic.getLineId());
                            outToClient.println("Broker B is responsible for these Keys");
                            for (Topic topic : hashed.get("BrokerB")) outToClient.println(topic.getLineId());
                            outToClient.println("Done");
                            String inputLineId = inFromClient.readLine();
                            boolean temp2 = false;
                            for (Topic topic : hashed.get("BrokerC")) if (topic.getLineId().equals(inputLineId)) temp2 = true;

                            if (temp2) {
                                HashMap<String, Value> values;
                                if (output.size() != 0) {
                                    for (Topic topic : output.keySet()) {
                                        if (topic.getLineId().equals(inputLineId)) {
                                            values = output.get(topic);
                                            for (Value bus_2_ : values.values())
                                                outToClient.println("The bus with id " + bus_2_.getBus().getVehicleId() + " was last spotted at [" + bus_2_.getBus().getTime() + "] at \nLatitude: " + bus_2_.getLatitude() + "\nLongitude: " + bus_2_.getLongitude() + "\nRoute: " + bus_2_.getBus().getLineName() + "\n-----------------------------------------------------------\n");
                                            if (values.size() == 0)
                                                outToClient.println("We couldn't find any buses on that line, please try other broker.");
                                        }
                                    }
                                } else {
                                    outToClient.println("We couldn't find any buses on that line, please try other broker.");
                                }
                                outToClient.println("next");
                            } else if (!inputLineId.toLowerCase().equals("bye")) {
                                outToClient.println("I don't have information for the specific line, try a different broker.");
                                outToClient.println("next");
                            }
                        }
                    } else if (inFromServer.toString().equals("Publisher")) {
                        try {
                            ObjectOutputStream out = new ObjectOutputStream(connected.getOutputStream());
                            out.writeObject("BrokerC");
                            output = BroUtilities.pull(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


