package pl.edu.atar.airplanemanagement.events;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.Instant;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
public class LocationEvent {
    private String airplaneId;
    private double latitude;
    private double longitude;
    private long timestamp;

    public LocationEvent() {
    }

    public LocationEvent(String airplaneId, double latitude, double longitude, long timestamp) {
        this.airplaneId = airplaneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getAirplaneId() {
        return airplaneId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDateTime() {
        Instant date = Instant.ofEpochSecond(this.timestamp/1000);
        return date.toString();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}