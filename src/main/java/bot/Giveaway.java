package bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class Giveaway {


    protected static final Logger logger = LogManager.getLogger("ouiBot");


    public static void main(String[] args) throws Exception {



        HashMap<String, Integer> total = gift(new File(args[0]));




        logger.info("__________________________________________");

        total.forEach((key, value) -> logger.info("Total gift to "+ key + " is $" + value));
        logger.info("__________________________________________");


    }


    private static HashMap<String, Integer> gift(File work) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(work));

        String line;
        HashMap<String, Integer> totals = new HashMap<String, Integer>();

        while ((line = br.readLine()) != null) {
            if (line.contains(" You have sent a gift of $")) {

                String amount = line.replace(" You have sent a gift of ", "");
                int index = amount.indexOf("$");
                amount = amount.substring(index + 1);
                amount = amount.replace(",", "");
                String[] split = amount.split(" to ");
                amount = split[0];
                String shop = split[1];
                shop = shop.replace("!", "");
                if (totals.containsKey(shop)) {
                    totals.put(shop, totals.get(shop) + Integer.parseInt(amount));
                } else {
                    totals.put(shop, Integer.parseInt(amount));
                }
            }

        }
        return totals;
    }


}

