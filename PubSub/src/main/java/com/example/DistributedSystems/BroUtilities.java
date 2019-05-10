/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137

 */
package com.example.DistributedSystems;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class BroUtilities {
    public static ArrayList<Topic> CreateBusLines(InputStream inputStream) {
        ArrayList<Topic> topics = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public static HashMap<String, ArrayList<Topic>> MD5(ArrayList<Topic> topics) {
        HashMap<String,ArrayList<Topic>> hashed = new HashMap<>();
        ArrayList<Topic> A = new ArrayList<>();
        ArrayList<Topic> B = new ArrayList<>();
        ArrayList<Topic> C = new ArrayList<>();
        int temp = 0;
        for (Topic topic: topics){
            temp = ipToLong("10.0.2.2");
            int num = Integer.parseInt(topic.getLineId()) + temp;
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