/**
 * Created by Andrew on 6/19/2017.
 */
public class WeatherData implements Comparable<WeatherData>{
    private String id;
    private int year;
    private int month;
    private int day;
    private String element;
    private int value;
    private String qflag;

    private WeatherData(){}

    public WeatherData(String id, int year, int month, int day, String element, int value, String qflag){
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.element = element;
        this.value = value;
        this.qflag = qflag;
    }

    public String getId() { return id; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() {return day; }
    public String getElement() { return element; }
    public int getValue() { return value; }
    public String getQflag() {return qflag; }

    @Override
    public int compareTo(WeatherData o) {
        if (value > o.value) return 1;
        else if (value == o.value) return 0;
        else return -1;
    }

    @Override
    public String toString() {
        return String.format("ID: %s\tDate: %02d/%02d/%d\tElement: %s\tValue: %.1fC\tqflag: %s",
                id, month, day, year, element, (float)value/10, qflag);
    }
}
