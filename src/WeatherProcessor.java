import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by Andrew on 6/19/2017.
 */
public class WeatherProcessor implements Callable<List<WeatherData>> {

    private File inputFile;
    static Main.DataChoice choice;
    static int startYear;
    static int endYear;
    static int startMonth;
    static int endMonth;
    List<WeatherData> values = new LinkedList<>();
    private WeatherData dataToDrop = null;
    private enum ProcessingType {Initial, Secondary}
    private ProcessingType currentProcessingMode;
    private static Iterator<Future<List<WeatherData>>> secondaryDataSource;

    // Constructor called when setting up for initial parse of files
    public WeatherProcessor(File inputFile){
        this.inputFile = inputFile;
        currentProcessingMode = ProcessingType.Initial;
    }

    // Constructor called when making second pass
    public WeatherProcessor(){
        currentProcessingMode = ProcessingType.Secondary;
    }

    // Sets parameters for search
    public static void setParameters(Main.DataChoice c, int sy, int ey, int sm, int em){
        choice = c;
        startYear = sy;
        endYear = ey;
        startMonth = sm;
        endMonth = em;
    }

    // Sets the iterator for the second pass
    public static void setIterator(Iterator<Future<List<WeatherData>>> data){ secondaryDataSource = data; }

    // call dispatches to appropriate method depending on processing mode
    @Override
    public List<WeatherData> call() throws Exception {
        switch (currentProcessingMode){
            case Initial:
                return fileParse();
            case Secondary:
                return secondPass();
            default:
                return null;
        }
    }

    // Method parses through the file provided with the constructor,
    // only saving the data if it fits the criteria
    public List<WeatherData> fileParse() throws Exception {

        try (Stream<String> stream = Files.lines(Paths.get(inputFile.toURI()))){

            stream.forEach(s -> {
                String id = s.substring(0,11);
                int year = Integer.valueOf(s.substring(11,15).trim());
                if (year < startYear || year > endYear) return;
                int month = Integer.valueOf(s.substring(15,17).trim());
                if ((startMonth < endMonth && (month < startMonth || month > endMonth)) ||
                        (startMonth > endMonth && (month < startMonth && month > endMonth))) return;
                String element = s.substring(17,21);
                if (!element.equals(choice.toString()) ) return;
                int days = (s.length() - 21) / 8;

                for (int i = 0; i < days; i++){
                    int value = Integer.valueOf(s.substring(21 + 8 * i, 26 + 8 * i).trim());
                    String qflag = s.substring(27 + 8*i, 28 + 8*i);
                    if (value == -9999 || !qflag.equals(" ")) continue;
                    addValue(new WeatherData(id, year, month, i + 1, element, value, qflag));
                }
            });
        }

        return values;
    }

    // Method moves through an iterator shared across threads
    // to pare list down to 20 candidates
    public List<WeatherData> secondPass() {
        while (secondaryDataSource.hasNext()){
            List<WeatherData> data = null;
            try {
                data = secondaryDataSource.next().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (data != null)data.forEach(wd -> {
                addValue(wd);
            });
        }
        return values;
    }

    // method meant for serial execution that finds the final 5 relevant data entries
    public List<WeatherData> finalFilter(ArrayList<Future<List<WeatherData>>> futureArrayList){
        values = new ArrayList<>();
        futureArrayList.forEach(d ->{
            List<WeatherData> data = null;
            try {
                data = d.get();
            }catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
            data.forEach(weatherData -> {
                addValue(weatherData);
            });
        });
        return values;
    }

    // Determines whether or not to add value
    private void addValue(WeatherData weatherData){

        if (values.size() == 5){
            if (dataToDrop != null) {
                switch (choice) {
                    case TMAX:
                        if (weatherData.compareTo(dataToDrop) < 0) return;
                        break;
                    case TMIN:
                        if (weatherData.compareTo(dataToDrop) > 0) return;
                        break;
                }
            }
            values.remove(dataToDrop);
        }
        values.add(weatherData);

        findDataToDrop();
    }

    // Decides which value will be dropped should another value need to be added
    private void findDataToDrop() {
        dataToDrop = values.get(0);

        for (int i = 1; i < values.size(); i++) {
            switch (choice){
                case TMAX:
                    if (values.get(i).compareTo(dataToDrop) < 0) dataToDrop = values.get(i);
                    break;
                case TMIN:
                    if (values.get(i).compareTo(dataToDrop) > 0) dataToDrop = values.get(i);
            }
        }

    }
}
