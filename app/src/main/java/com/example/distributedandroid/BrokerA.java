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
import java.util.Map;

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
        volatile static HashMap<String,Value> output = new HashMap<>();
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
                        HashMap<String, Value> values = new HashMap<>();
                        if (output.size() != 0) {
                            for (Map.Entry<String, Value> v : output.entrySet())
                                if (v.getValue().getBus().getBuslineId().equals(inputLineId))
                                    values.put(v.getValue().getBus().getVehicleId(),v.getValue());
                            outToClient.writeObject(values);
                        } else outToClient.writeObject(null);
                    } else if (inFromServer.toString().equals("Publisher")) {
                        ObjectOutputStream out = new ObjectOutputStream(connected.getOutputStream());
                        out.writeObject("BrokerA");
                        Value input;
                        inFromServer = in.readObject();
                        if(!inFromServer.toString().equals("Stop")) {
                            inFromServer = in.readObject();
                            while (inFromServer != null) {
                                input = (Value) inFromServer;
                                if (output.size() == 0) output.put(input.getBus().getVehicleId(),input);
                                else{
                                    Map<String,Value> temp = new HashMap<>();
                                    for(Map.Entry<String,Value> entry : output.entrySet()){
                                        if (entry.getValue().getBus().getVehicleId().equals(input.getBus().getVehicleId())) {
                                            if (entry.getValue().getBus().getTime().compareTo(input.getBus().getTime()) < 0) {
                                                temp.put(input.getBus().getVehicleId(),input);
                                            }
                                        } else temp.put(input.getBus().getVehicleId(),input);
                                    }
                                    output.putAll(temp);
                                }
                                inFromServer = in.readObject();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


