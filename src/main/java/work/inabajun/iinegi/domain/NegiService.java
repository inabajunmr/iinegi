package work.inabajun.iinegi.domain;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;

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
}
