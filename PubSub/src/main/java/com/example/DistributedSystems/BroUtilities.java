/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137

 */
package com.example.DistributedSystems;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;


class BroUtilities {
    static void CreateBusLines(ArrayList<Topic> topics) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("PubSub\\src\\main\\java\\com\\example\\DistributedSystems\\busLinesNew.txt"));
        String line = in.readLine();
        String [] characteristics = new String[3];
        while(line != null){
            int i = 0;
            for (String word : line.split(",")) {
                characteristics[i] = word;
                i++;
            }
            topics.add(new Topic(characteristics[1].trim()));
            line = in.readLine();
        }
        in.close();
    }

    static HashMap<String, ArrayList<Topic>> MD5(ArrayList<Topic> topics) {
        HashMap<String,ArrayList<Topic>> hashed = new HashMap<>();
        ArrayList<Topic> A = new ArrayList<>();
        ArrayList<Topic> B = new ArrayList<>();
        ArrayList<Topic> C = new ArrayList<>();
        int temp = 0;
        for (Topic topic: topics){
            try {
                temp = ipToLong(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            int num = Integer.parseInt(topic.getLineId()) + temp + 4321;

            if(num%3 == 0){
                A.add(topic);
            }else if(num%3 == 1){
                B.add(topic);
            }else{
                C.add(topic);
            }
        }
        hashed.put("BrokerA",A);
        hashed.put("BrokerB",B);
        hashed.put("BrokerC",C);
        return hashed;
    }

    static HashMap<Topic,HashMap<String,Value>> pull(ObjectInputStream in) throws IOException, ClassNotFoundException{
        HashMap<String,Value> input;
        HashMap<Topic,HashMap<String,Value>> output = new HashMap<>();
        try {
            while (true) {
                try {
                    Object inFromServer;
                    inFromServer = in.readObject();
                    if(!inFromServer.equals("Stop")){
                        Topic topic = (Topic) inFromServer;
                        input = (HashMap<String,Value>) in.readObject();
                        output.put(topic,input);
                    }else{
                        break;
                    }
                } catch (EOFException ignored) {

                }
            }
        }catch (BindException | ConnectException e){
            System.out.println("Couldn't connect to server");
        }
        return output;
    }

    private static int ipToLong(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");
        int result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }
        return result;
    }

}