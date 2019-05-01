/*
Ονο/υμο                 Αμ
Πάνος Ευάγγελος         3150134
Μορφιαδάκης Εμμανουήλ   3150112
Μπρακούλιας Φίλιππος    3140137
 */
package com.example.DistributedSystems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class PubUtilities {
    private static ArrayList<RouteHelper> helpers = new ArrayList<>();
    static void CreateBuses() throws IOException, ParseException {
        BufferedReader in = new BufferedReader(new FileReader("PubSub\\src\\main\\java\\com\\example\\DistributedSystems\\busPositionsNew.txt"));

        String line = in.readLine();
        String [] characteristics = new String[6];
        while(line != null){
            int i = 0;
            for(String word : line.split(",")){
                characteristics[i] = word;
                i++;
            }
            String string = characteristics[5];
            DateFormat format = new SimpleDateFormat("MMM  d yyyy HH:mm:ss:SSSa", Locale.ENGLISH);
            Date date = format.parse(string);
            for(RouteHelper helper: helpers){
                if(helper.getLineCode().equals(characteristics[0])&&helper.getRouteCode().equals(characteristics[1])){
                    Bus bus = new Bus(characteristics[0], characteristics[1], characteristics[2], helper.getDesc(), helper.getLineId(), date);
                    Publisher.values.add(new Value(bus,Double.parseDouble(characteristics[3].trim()),Double.parseDouble(characteristics[4])));
                }
            }
            line = in.readLine();
        }
        in.close();
    }

    static void CreateNames() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("PubSub\\src\\main\\java\\com\\example\\DistributedSystems\\RouteCodesNew.txt"));
        String line = in.readLine();
        String [] characteristics = new String[4];
        while(line != null){
            int i = 0;
            for (String word : line.split(",")) {
                characteristics[i] = word;
                i++;
            }
            helpers.add(new RouteHelper(characteristics[1],characteristics[0],characteristics[3]));
            line = in.readLine();
        }
        in.close();

        in = new BufferedReader(new FileReader("PubSub\\src\\main\\java\\com\\example\\DistributedSystems\\busLinesNew.txt"));
        line = in.readLine();
        while(line != null){
            int i = 0;
            for (String word : line.split(",")) {
                characteristics[i] = word;
                i++;
            }
            for (RouteHelper helper:helpers) if(helper.getLineCode().equals(characteristics[0])) helper.setLineId(characteristics[1]);
            line = in.readLine();
        }
        in.close();
    }
}