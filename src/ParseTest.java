import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by aksch_000 on 6/15/2017.
 */
public class ParseTest {
private static final String stationsFileString = "ghcnd-stations.txt";

public static void main(String[] args){
        File stationsFile = new File(stationsFileString);
        System.out.println(String.format("Current directory: %s", System.getProperty("user.dir")));
        System.out.println(String.format("File exists: %s", stationsFile.exists()? "yes" : "no"));

        List<StationData> stationList = new ArrayList<>();
        String temp;

        try (Stream<String> stream = Files.lines(Paths.get(stationsFileString))){
        stream.forEach(s -> { stationList.add(new StationData(s)); });
        } catch (IOException e){ System.out.println("Unable to parse all lines");}

        System.out.println(String.format("Parsed %d lines",stationList.size()));

        for (int i = 0; i < 5; i++) System.out.println(stationList.get(i));

        System.out.println("\n");

        File dataFolder = new File("ghcnd_hcn");
        System.out.println(String.format("Data folder found: %s", dataFolder.exists() ? "Yes" : "No"));
        System.out.println(String.format("Data folder location: %s", dataFolder.getPath()));
        File[] dataFiles = dataFolder.listFiles();

        System.out.println(String.format("File name: %s", dataFiles[0].getName()));
        WeatherProcessor.setParameters(Main.DataChoice.TMAX, 1950, 2000, 1, 12);
        WeatherProcessor weatherParser = new WeatherProcessor(dataFiles[0]);
        List<WeatherData> output = null;
        try {
                output= weatherParser.call();
        } catch (Exception e) {
                e.printStackTrace();
        }

        if (output != null) output.forEach(o -> System.out.println(o));
        else System.out.println("Error reading data file");

/*
        try (BufferedReader reader = new BufferedReader(new FileReader(stationsFile))){
            for (String line = reader.readLine(); line != null;){

            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){e.printStackTrace();}
 */
        }

}
