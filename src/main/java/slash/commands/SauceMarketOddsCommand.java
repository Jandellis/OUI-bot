package slash.commands;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import action.sm.Utils;
import bot.Sauce;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import org.apache.commons.math3.distribution.NormalDistribution;

public class SauceMarketOddsCommand extends SlashCommand {
    @Override
    public String getName() {
        return "odds";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {


        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
        try {
            String type = getParameter("type", "all", event);
            Sauce sauce = Sauce.getSauce(type);
            long price = getParameter("price", 100L, event);
            long time = getParameter("time", 24L, event);

            HashMap<Integer, Integer> data =  Utils.loadLast(sauce, -1, false, 0);
            double odds = monteCarloOddsNormal(data, (int)price, (int)time) ;
            double oddsHistorical = monteCarloWithHistoricalPatterns(data, (int)price, (int)time, 10000, 10) ;
            double oddsChange = monteCarloAfterChangeX(data, (int)price, (int)time, 10000) ;

            int latestHour = Collections.max(data.keySet());
            int currentPrice = data.get(latestHour);
            int priceChange = data.get(latestHour) - data.get(latestHour-1);


            embed.color(discord4j.rest.util.Color.SUMMER_SKY);
            embed.title("Sauce market odds for " + sauce.getUppercaseName());
            embed.addField("Current price", "$"+currentPrice, false);
            embed.addField("Odds using all data", String.format("%.2f", odds*100) + "% chance that it will hit $"+price+ " in "+ time+ " hours.", false);
            embed.addField("Odds price is +/-$10 $"+currentPrice, String.format("%.2f", oddsHistorical*100) + "% chance that it will hit $"+price+ " in "+ time+ " hours.", false);
            embed.addField("Odds when change is $"+priceChange, String.format("%.2f", oddsChange*100) + "% chance that it will hit $"+price+ " in "+ time+ " hours.", false);
//            embed.description(String.format("%.2f", odds*100) + "% chance that it will hit $"+price+ " in "+ time+ " hours.");


            return event.reply()
                    .withEphemeral(false)
                    .withEmbeds(embed.build());

        } catch (Exception e) {
//            throw new RuntimeException(e);
            printException(e, event.getClient());
        }

        return null;


    }



    public static List<Integer> calculatePriceChanges(HashMap<Integer, Integer> priceDataMap) {
        List<Integer> priceChanges = new ArrayList<>();
        List<Integer> hours = new ArrayList<>(priceDataMap.keySet());
        Collections.sort(hours); // Ensure we process hours in ascending order

        for (int i = 1; i < hours.size(); i++) {
            int currentPrice = priceDataMap.get(hours.get(i));
            int previousPrice = priceDataMap.get(hours.get(i - 1));
            int change = currentPrice - previousPrice;
            priceChanges.add(change);
        }


        return priceChanges;
    }

    public static double calculateMean(List<Integer> changes) {
        double sum = 0;
        for (double change : changes) {
            sum += change;
        }
        return sum / changes.size();
    }

    public static double calculateStandardDeviation(List<Integer> changes, double mean) {
        double sum = 0;
        for (double change : changes) {
            sum += Math.pow(change - mean, 2);
        }
        return Math.sqrt(sum / changes.size());
    }

    public static double calculateZScore(double targetPrice, double currentPrice, double mean, double stdDev) {
        double predictedChange = targetPrice - currentPrice;
        return (predictedChange - mean) / stdDev;
    }

    public static double calculateProbability(double zScore) {
        NormalDistribution normalDist = new NormalDistribution(0, 1);
        return normalDist.cumulativeProbability(zScore);
    }


    public static double predictOdds(HashMap<Integer, Integer> priceDataMap, int targetPrice) {
        List<Integer> priceChanges = calculatePriceChanges(priceDataMap);
        double mean = calculateMean(priceChanges);
        double stdDev = calculateStandardDeviation(priceChanges, mean);

        // Get the latest hour and its corresponding price
        int latestHour = Collections.max(priceDataMap.keySet());
        int currentPrice = priceDataMap.get(latestHour);

        double zScore = calculateZScore(targetPrice, currentPrice, mean, stdDev);
        return calculateProbability(zScore);
    }

    public static List<Integer> calculateCumulativeChanges(HashMap<Integer, Integer> priceDataMap, int hours) {
        List<Integer> cumulativeChanges = new ArrayList<>();
        List<Integer> timePoints = new ArrayList<>(priceDataMap.keySet());
        Collections.sort(timePoints); // Ensure time points are in ascending order

        for (int i = 0; i <= timePoints.size() - hours; i++) {
            int startPrice = priceDataMap.get(timePoints.get(i));
            int endPrice = priceDataMap.get(timePoints.get(i + hours - 1));
            int change = endPrice - startPrice;
            cumulativeChanges.add(change);
        }
        return cumulativeChanges;
    }

    public static double predictOdds(HashMap<Integer, Integer> priceDataMap, int targetPrice, int hours) {
        // Step 1: Calculate cumulative price changes for the given number of hours
        List<Integer> cumulativeChanges = calculateCumulativeChanges(priceDataMap, hours);
        double mean = calculateMean(cumulativeChanges);
        double stdDev = calculateStandardDeviation(cumulativeChanges, mean);

        // Step 2: Get the latest price (most recent hour)
        int latestHour = Collections.max(priceDataMap.keySet());
        int currentPrice = priceDataMap.get(latestHour);

        // Step 3: Calculate Z-score for target price
        double targetChange = targetPrice - currentPrice;
        double zScore = (targetChange - mean) / stdDev;

        // Step 4: Calculate the probability using the Z-score
        return calculateProbability(zScore);
    }

    public static double monteCarloOddsNormal(HashMap<Integer, Integer> priceDataMap, int targetPrice, int hours) {
        // Calculate price differences
        List<Double> priceChanges = new ArrayList<>();

        List<Integer> timePoints = new ArrayList<>(priceDataMap.keySet());
        Collections.sort(timePoints); // Ensure time points are in ascending order

        //change this to include hours in it
        for (int i = 1; i <= timePoints.size()-1; i++) {
            double change = priceDataMap.get(i) - priceDataMap.get(i - 1);
            priceChanges.add(change);
        }


        // Calculate mean and standard deviation of price changes
        double mean = priceChanges.stream().mapToDouble(val -> val).average().orElse(0.0);
        double max = priceChanges.stream().mapToDouble(val -> val).max().orElse(0.0);
        double min = priceChanges.stream().mapToDouble(val -> val).min().orElse(0.0);
        double stdDev = Math.sqrt(priceChanges.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0));

        // Monte Carlo simulation
        int simulations = 10000;
//        int hours = 24;
        double initialPrice = priceDataMap.get(priceDataMap.size()-1);
//        double targetPrice = 4000;
        int hits = 0;

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev);

        for (int sim = 0; sim < simulations; sim++) {
            double price = initialPrice;
            //remove the for loop and only do the price change all in one go
            for (int t = 0; t < hours; t++) {
                // add some logging in here and see what its doing
                // this should be between -15 and +15 and be an int
                double randomShock = normalDist.sample();
                price += randomShock; // Use additive price changes
                if (targetPrice > initialPrice) {
                    if (price >= targetPrice) {
                        hits++;
                        break; // Stop once it crosses the threshold
                    }
                } else {
                    if (price <= targetPrice) {
                        hits++;
                        break; // Stop once it crosses the threshold
                    }
                }
            }
        }

        // Calculate probability
        double probability = (double) hits / simulations;
        System.out.printf("Probability of reaching or exceeding $4000: %.2f%%\n", probability);
        return  probability;

    }


    public static List<Integer> filterRelevantChanges(HashMap<Integer, Integer> priceDataMap, int currentPrice, int tolerance, int hours) {
        List<Integer> relevantChanges = new ArrayList<>();
        List<Integer> timePoints = new ArrayList<>(priceDataMap.keySet());
        Collections.sort(timePoints); // Ensure time points are in ascending order

        for (int i = 0; i <= timePoints.size() - hours; i++) {
            int priceAtTime = priceDataMap.get(timePoints.get(i));
            if (Math.abs(priceAtTime - currentPrice) <= tolerance) {
                // Calculate cumulative change over the given number of hours
                int endPrice = priceDataMap.get(timePoints.get(i + hours - 1));
                relevantChanges.add(endPrice - priceAtTime);
            }
        }
        return relevantChanges;
    }

    public static double monteCarloWithHistoricalPatterns(HashMap<Integer, Integer> priceDataMap, int targetPrice, int hours, int simulations, int tolerance) {
        // Step 1: Get the latest price (most recent hour)
        int latestHour = Collections.max(priceDataMap.keySet());
        int currentPrice = priceDataMap.get(latestHour);

        // Step 2: Filter relevant historical changes for the given number of hours
        List<Integer> relevantChanges = filterRelevantChanges(priceDataMap, currentPrice, tolerance, hours);

        // Step 3: Calculate mean and standard deviation of relevant changes
        double mean = calculateMean(relevantChanges);
        double stdDev = calculateStandardDeviation(relevantChanges, mean);

        // Step 4: Run Monte Carlo simulations
        int belowTargetCount = 0;
        int aboveTargetCount = 0;

        Random rand = new Random();
        for (int i = 0; i < simulations; i++) {
            // Simulate a single cumulative change for the specified number of hours
            double randomChange = rand.nextGaussian() * stdDev + mean; // Random cumulative price change
            double futurePrice = currentPrice + randomChange;         // Simulated future price

            // Compare simulated future price to target
            if (futurePrice < targetPrice) {
                belowTargetCount++;
            } else {
                aboveTargetCount++;
            }
        }

        // Step 5: Calculate probabilities
        double belowProbability = (double) belowTargetCount / simulations;
        double aboveProbability = (double) aboveTargetCount / simulations;

//        return new double[] {belowProbability, aboveProbability};

        if (targetPrice > currentPrice) {
            return aboveProbability;
        } else
            return belowProbability;
    }

    public static List<Integer> getChangesAfterChangeX(HashMap<Integer, Integer> priceDataMap, int changeX, int hours) {
        List<Integer> futureChanges = new ArrayList<>();
        List<Integer> timePoints = new ArrayList<>(priceDataMap.keySet());
        Collections.sort(timePoints); // Ensure time points are in ascending order

        for (int i = 0; i < timePoints.size() - hours - 1; i++) {
            int currentPrice = priceDataMap.get(timePoints.get(i));
            int nextPrice = priceDataMap.get(timePoints.get(i + 1));
            int actualChange = nextPrice - currentPrice;

            // Check if the change matches X
            if (actualChange == changeX) {
                int futurePrice = priceDataMap.get(timePoints.get(i + hours));
                futureChanges.add(futurePrice - nextPrice); // Record subsequent change
            }
        }
        return futureChanges;
    }
    public static double monteCarloAfterChangeX(HashMap<Integer, Integer> priceDataMap, int targetPrice, int hours, int simulations) {
        // Step 1: Get the latest price (most recent hour)
        int latestHour = Collections.max(priceDataMap.keySet());
        int currentPrice = priceDataMap.get(latestHour);
        int priceChange = currentPrice - priceDataMap.get(latestHour-1);

        // Step 2: Get future changes following occurrences of change X
        List<Integer> observedFutureChanges = getChangesAfterChangeX(priceDataMap, priceChange, hours);

        // Step 3: Validate observed data
        if (observedFutureChanges.isEmpty()) {
            throw new IllegalStateException("No historical occurrences of change " + priceChange + " found.");
        }

        // Step 4: Calculate probabilities using Monte Carlo
        int belowTargetCount = 0;
        int aboveTargetCount = 0;
        HashMap <Integer, Integer> priceChanges = new HashMap<>();
        Random rand = new Random();

        for (int i = 0; i < simulations; i++) {
            // Randomly sample from observed future changes
            int randomChange = observedFutureChanges.get(rand.nextInt(observedFutureChanges.size()));
            int futurePrice = currentPrice + randomChange; // Apply change X and future change
            priceChanges.merge(futurePrice, 1, Integer::sum);

            // Compare simulated future price to target
            if (futurePrice < targetPrice) {
                belowTargetCount++;
            } else {
                aboveTargetCount++;
            }
        }

        // Step 5: Calculate probabilities
        double belowProbability = (double) belowTargetCount / simulations;
        double aboveProbability = (double) aboveTargetCount / simulations;

//        return new double[] {belowProbability, aboveProbability};
        if (targetPrice > currentPrice) {
            return aboveProbability;
        } else
            return belowProbability;
    }

}
