package action;

import bot.Config;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheapSaucePingOld {
    DiscordClient client;

    Config config = Config.getInstance();
    int startMin;

    int cheapPrice = 100;
    protected static final Logger logger = LogManager.getLogger("ouiBot");


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    volatile boolean isStopIssued;

    public CheapSaucePingOld(DiscordClient client) {
        this.client = client;
        startMin = 22;
    }

    /**
     * Run task once a hour
     */
    public void start() {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running task");
//                print();
            }

        };
        long delay = computeNextDelay();
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    private long computeNextDelay() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDateTime localNow = LocalDateTime.now();
        LocalDateTime future = LocalDateTime.parse(formatter.format(localNow.plusMinutes(2)),formatter);
        logger.info(formatter.format(future));
        logger.info(formatter.format(localNow));


        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);


        int min = zonedNow.getMinute();
        long delay = 60;
        if (min > startMin)
            delay = (60 + startMin) - min;
        if (min < startMin)
            delay = startMin - min;
        logger.info("running task with delay of " + delay);

        return 1;
    }

    public void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
        }
    }

    public void print() {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@&975421913818095656> we have some cheap sauce\r\n");


        getPrices().forEach((sauce, price) -> {
            if (cheapPrice > price && price != -1) {
                sb.append(" - " + sauce.getName() + " $" + price + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            //client.getChannelById(Snowflake.of("953240351743819856")).createMessage(sb.toString()).block();
            client.getChannelById(Snowflake.of("963278241479680041")).createMessage(sb.toString()).block();
    }

    public HashMap<Sauce, Integer> getPrices() {
        HashMap<Sauce, Integer> prices = new HashMap<>();
        prices.put(Sauce.salsa, getPrice(Sauce.salsa.getName()));
        prices.put(Sauce.hotsauce, getPrice(Sauce.hotsauce.getName()));
        prices.put(Sauce.pico, getPrice(Sauce.pico.getName()));
        prices.put(Sauce.chipotle, getPrice(Sauce.chipotle.getName()));
        prices.put(Sauce.guacamole, getPrice(Sauce.guacamole.getName()));
        return prices;
    }

    public int getPrice(String sauce) {
        int price = -1;
        return price;
    }

    /**
     * Get price using website
     * @param sauce
     * @return
     */
//    public int getPrice(String sauce) {
//        int price = -1;
//
//        WebDriver driver = null;
//        try {
//            String url = "https://tacoshack.online/saucemarket/";
//            WebDriverManager.chromedriver().setup();
//
//            driver = new ChromeDriver();
//            driver.get(url + sauce);
////            WebDriverWait(driver).
//            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(10000));
////            List<WebElement> elements = driver.findElements(By.id("h2"));
//            String title = driver.getTitle();
//            logger.info(title);
//            Thread.sleep(3000);
//            price = Integer.parseInt(driver.getPageSource().split("Current Price: ")[1].split("<", 2)[0].replace('$', ' ').replace(" ", ""));
//
//            logger.info(sauce + " price is " + price);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            driver.quit();
//        }
//        return price;
//    }
}
