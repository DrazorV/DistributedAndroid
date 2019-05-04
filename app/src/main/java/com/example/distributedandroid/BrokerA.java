/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137
 */
package com.example.distributedandroid;

import java.io.*;
import java.net.*;
import java.util.HashMap;


public  class BrokerA {
        public static void main(String[] args) throws IOException{
        ServerSocket providerSocket = new ServerSocket(4322, 3);
        System.out.println("Waiting for consumers to connect...");
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
                        ObjectOutputStream outToClient = new ObjectOutputStream(connected.getOutputStream());
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));
                        String inputLineId = inFromClient.readLine();
                        HashMap<String, Value> values;
                        if (output.size() != 0) {
                            for (Topic topic : output.keySet()) {
                                if (topic.getLineId().equals(inputLineId)){
                                    values = output.get(topic);
                                    if (values.size() == 0) outToClient.writeObject("We couldn't find any buses on that line, please try other broker.");
                                    else outToClient.writeObject(values);
                                }
                            }
                        } else outToClient.writeObject(null);
                    } else if (inFromServer.toString().equals("Publisher")) {
                        try {
                            ObjectOutputStream out = new ObjectOutputStream(connected.getOutputStream());
                            out.writeObject("BrokerA");
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


