package work.inabajun.iinegi.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public class Negi {

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    private String id;
    private String description;
    private String imagePath;
    private long createTimestamp;

    public Negi() {
        // for jackson
    }

    public Negi(String description, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = LocalDateTime.now()
                .atZone(ZoneId.of("Asia/Tokyo")).toInstant().toEpochMilli();
    }

    public Negi(String id, String description, String imagePath, long createTimestamp) {
        this.id = id;
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = createTimestamp;
    }

    public String getCreateTimestampWithId() {
        return this.getCreateTimestamp() + "_" + this.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Negi negi = (Negi) o;
        return createTimestamp == negi.createTimestamp &&
                Objects.equals(id, negi.id) &&
                Objects.equals(description, negi.description) &&
                Objects.equals(imagePath, negi.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, imagePath, createTimestamp);
    }
}
