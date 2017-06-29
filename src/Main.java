import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by aksch_000 on 6/15/2017.
 */
public class Main {

    private static String dataFolderName = "ghcnd_hcn";
    private static String stationsFileName = "ghcnd-stations.txt";

    enum DataChoice {TMIN, TMAX};

    private int beginYear;
    private final int DEFAULT_BEGIN_YEAR = 0;
    private int endYear;
    private final int DEFAULT_END_YEAR = LocalDate.now().getYear();
    private int startMonth;
    private final int DEFAULT_START_MONTH = 1;
    private int endMonth;
    private final int DEFAULT_END_MONTH = 12;
    private DataChoice c;
    private final DataChoice DEFAULT_DATA_CHOICE= DataChoice.TMAX;

    ArrayList<Future<List<WeatherData>>> allData = new ArrayList<>();
    ArrayList<Future<List<WeatherData>>> filteredData = new ArrayList<>();
    List<WeatherData> finalDataSet = new ArrayList<>();
    HashMap<String, StationData> stationMap = new HashMap<>();

    private ExecutorService threadPool = null;



    public static void main(String[] args){
        Main main = new Main();
        main.requestInput();

        main.processDataFiles();
        main.printResults();

        if (main.threadPool !=null && !main.threadPool.isShutdown()) main.threadPool.shutdown();
    }

    public void requestInput(){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Leave entry blank to use default value.\n");

        String input;
        beginYear = -1;
        while (beginYear < 0){
            System.out.print(String.format("Starting Year (default %d)> ", DEFAULT_BEGIN_YEAR));
            try{
                input = scanner.nextLine();
                if (input.isEmpty()) beginYear = DEFAULT_BEGIN_YEAR;
                else beginYear = Integer.parseInt(input);
                if (beginYear < 0) System.out.println("Value must be at least 0");
                if (beginYear > LocalDate.now().getYear()) System.out.println("Value cannot be past the current year.");
            } catch (InputMismatchException e){
                System.out.println("Invalid input format, please try again.");
            }
        }

        endYear = -1;
        while (endYear < beginYear) {
            System.out.print(String.format("Ending Year (default %d)> ", LocalDate.now().getYear()));
            try {
                input = scanner.nextLine();
                if (input.isEmpty()) endYear = DEFAULT_END_YEAR;
                else endYear = Integer.parseInt(input);
                if (endYear < beginYear) System.out.println("Ending year cannot be before beginning year.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input format, please try again.");
            }
        }

        startMonth = -1;
        while (startMonth < 0){
            System.out.print(String.format("Starting Month (default %d)> ", DEFAULT_START_MONTH));
            try {
                input = scanner.nextLine();
                if (input.isEmpty()) startMonth = DEFAULT_START_MONTH;
                else startMonth = Integer.parseInt(input);
                if (startMonth < 1 || startMonth > 12) {
                    System.out.println("Must be between 1 and 12");
                    startMonth = -1;
                }
            } catch (InputMismatchException e){
                System.out.println("Invalid input. Please try again.");
            }
        }

        endMonth = -1;
        while (endMonth <0){
            System.out.print("Ending Month (default 12)> ");
            try {
                input = scanner.nextLine();
                if (input.isEmpty()) endMonth = DEFAULT_END_MONTH;
                else endMonth = Integer.parseInt(input);
                if (endMonth < 1 || endMonth > 12){
                    System.out.println("Must be between 1 and 12");
                    endMonth = -1;
                }
            }catch (InputMismatchException e){
                System.out.println("Invalid input. Please try again.");
            }
        }

        c = null;
        while (c == null){

            try {
                System.out.print(String.format("(1) TMAX or (2) TMIN (default %s> ", DEFAULT_DATA_CHOICE.toString()));
                input = scanner.nextLine();
                if (input.isEmpty()) c = DEFAULT_DATA_CHOICE;
                else {
                    switch (Integer.parseInt(input)) {
                        case 1:
                            c = DataChoice.TMAX;
                            break;
                        case 2:
                            c = DataChoice.TMIN;
                            break;
                        default:
                            System.out.println("Choice not recognized.");
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid option.");
            }
        }

        WeatherProcessor.setParameters(c, beginYear, endYear, startMonth, endMonth);
    }

    public void processDataFiles(){

        // Set up thread pool
        int numProcessors = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(numProcessors * 4);

        // Parse files
        File dataDirectory = new File(dataFolderName);
        File[] files = dataDirectory.listFiles();
        for (int i = 0; i < files.length; i++){
            allData.add(threadPool.submit(new WeatherProcessor(files[i])));
        }

        // Set iterator for second pass filter and start second filter
        WeatherProcessor.setIterator(allData.iterator());
        for (int i = 0; i < 4; i++){
            filteredData.add(threadPool.submit(new WeatherProcessor()));
        }

        // Make final pass over data
        WeatherProcessor finalFilter = new WeatherProcessor();
        finalDataSet = finalFilter.finalFilter(filteredData);

        Collections.sort(finalDataSet);

        // Parse station data
        File stationFile = new File(stationsFileName);
        try (Stream<String> stream = Files.lines(Paths.get(stationFile.toURI()))){
            stream.forEach(s -> {
                StationData data = new StationData(s);
                stationMap.put(data.getId(), data);
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void printResults(){
        System.out.println("\n===============Results===============\n");
        finalDataSet.forEach(weatherData -> {
            System.out.println(weatherData);
            System.out.println(stationMap.get(weatherData.getId()));
            System.out.println();
        });
    }
}
