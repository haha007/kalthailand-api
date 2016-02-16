package th.co.krungthaiaxa.elife.api.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ImageGenerator {
    private static String fileName = "/Users/carnoult/git/AGS/kalthailand-api/benefitGreen.jpg";

    private static String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAjCAYAAACpZEt+AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsSAAALEgHS3X78AAAAYklEQVQ4y+3UsQ2AMAxE0e/EEjTsxmoMxQLU9IxAEcJRJBIgMQHKNZatd61tWkZhmcAJAAo8894+0kADDfwV+JAyu0MKXblY+RNGIgpcEhJIqp17SmDrNisHkPraPIjVxOxczrsifgKtD0EAAAAASUVORK5CYII=";

    public static void main(String[] args) {
        try {
            FileUtils.writeByteArrayToFile(new File(fileName), Base64.getDecoder().decode(base64Image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
