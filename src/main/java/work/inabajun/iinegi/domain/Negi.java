package work.inabajun.iinegi.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Negi {

    private String id;
    private String description;
    private String imagePath;
    private long createTimestamp;

    public Negi(String description, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = LocalDateTime.now()
                .atZone(ZoneId.of("JST")).toInstant().toEpochMilli();
    }
}
