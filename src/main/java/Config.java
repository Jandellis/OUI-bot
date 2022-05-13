import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
    Properties prop;

    public Config() {
        prop = new Properties();
        String fileName = "app.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
     // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) {

        }
    }

    public String get(String property) {
        return prop.getProperty(property);
    }
}
