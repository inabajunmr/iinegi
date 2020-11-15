package work.inabajun.iinegi.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    private String id;
    private String description;
    private String imagePath;
    private LocalDateTime createTimestamp;

    public Negi() {
        // for jackson
    }

    public Negi(String description, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }

    public Negi(String id, String description, String imagePath, LocalDateTime createTimestamp) {
        this.id = id;
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = createTimestamp;
    }

    public Negi(String id, String description, String imagePath, String createTimestamp) {
        this.id = id;
        this.description = description;
        this.imagePath = imagePath;
        this.createTimestamp = LocalDateTime.parse(createTimestamp);
    }

    public String getCreateTimestampWithId() {
        return this.getCreateTimestamp().toString() + "_" + this.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Negi negi = (Negi) o;
        return Objects.equals(id, negi.id) &&
                Objects.equals(description, negi.description) &&
                Objects.equals(imagePath, negi.imagePath) &&
                Objects.equals(createTimestamp, negi.createTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, imagePath, createTimestamp);
    }

    @Override
    public String toString() {
        return "Negi{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", createTimestamp=" + createTimestamp +
                '}';
    }
}
