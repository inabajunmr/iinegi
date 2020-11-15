package work.inabajun.iinegi.domain;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Singleton
public class NegiService {

    @Inject
    NegiRepository negiRepository;

    /**
     * Create new negi.
     *
     * <p>Upload image to s3 and persist data.</p>
     * @param image image stream
     * @param description image description
     * @return persisted Negi
     * @throws NotImageException file is not image
     * @throws NegiAlreadyExistException
     */
    public Negi create(InputStream image, String description) {
        final String imagePath = negiRepository.uploadImage(image);
        Negi negi = new Negi(description, imagePath);
        return negiRepository.create(negi);
    }

    /**
     * Find negi.
     * @param id id
     * @return Negi
     */
    public Optional<Negi> find(String id) {
        return negiRepository.find(id);
    }

    /**
     * Return Negi list created after args datetime.
     * @param timestamp epoch milli
     * @return Negi list
     */
    public List<Negi> listAfter(Long timestamp) {
        return negiRepository.listAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Asia/Tokyo")));
    }
}
