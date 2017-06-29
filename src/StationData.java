/**
 * Created by aksch_000 on 6/15/2017.
 */
public class StationData {

    private String id;
    private float latitude;
    private float longitude;
    private float elevation;
    private String state;
    private String name;

    public String getId(){return id;}
    public float getLatitude() {return latitude;}
    public float getLongitude() {return longitude;}
    public float getElevation() { return elevation;}
    public String getState() {return state;}
    public String getName() {return name;}

    private StationData(){}

    public StationData(String s){
        id = s.substring(0,11);
        latitude = Float.valueOf(s.substring(12,20).trim());
        longitude = Float.valueOf(s.substring(21,30).trim());
        elevation = Float.valueOf(s.substring(31,37).trim());
        state = s.substring(38,40);
        name = s.substring(41,71).trim();
    }

    public StationData(String id, float latitude, float longitude, float elevation, String state, String name){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.state = state;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("ID: %s\tName: %s\tState: %s\tLatitude: %.4f\tLongitude: %.4f\tElevation: %.1f",
                id, name, state, latitude, longitude, elevation);
    }
}
