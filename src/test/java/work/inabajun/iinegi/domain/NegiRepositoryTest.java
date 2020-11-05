package work.inabajun.iinegi.domain;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class NegiRepositoryTest {

    @Inject
    NegiRepository sut;

    @Inject
    S3Client s3;

    @Test
    public void testCreate() {
        // setup
        final Negi negi = new Negi("desc", "testpath");

        // exercise
        sut.create(negi);

        // verify
        assertEquals(negi, sut.find(negi.getId()));
    }

    @Test
    public void testCreate_Duplicate() {
        // setup
        final Negi negi = new Negi("desc", "testpath");
        sut.create(negi);

        // exercise & verify
        assertThrows(NegiAlreadyExistException.class, () -> sut.create(negi));
    }

    @Test
    public void testFind_NotFound() {
        // exercise & verify
        assertThrows(NegiNotFoundException.class, () -> sut.find(UUID.randomUUID().toString()));
    }

    @Test
    public void testUploadImage() throws IOException {
        // setup
        final URL imageUrl = new URL("https://avatars2.githubusercontent.com/u/10000393?s=460&u=133f00742666dd39fc8ab2f4f6cd3a1b00f16f4e&v=4");
        final BufferedImage image = ImageIO.read(imageUrl);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        final InputStream is = new ByteArrayInputStream(os.toByteArray());

        // exercise
        final String imageKey = sut.uploadImage(is);
        // if bucket doesn't have object, throw exception.
        s3.getObject(GetObjectRequest.builder().bucket(NegiSchema.BUCKET_NAME).key(imageKey).build());
    }

    @Test
    public void testUploadImage_NotImage() {
        String value = "aaa";
        InputStream input = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        assertThrows(NotImageException.class, () -> sut.uploadImage(input));
    }

}