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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        assertEquals(negi, sut.find(negi.getId()).get());
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
        assertEquals(sut.find(UUID.randomUUID().toString()), Optional.empty());
    }

    @Test
    public void testList() {

        // setup
        final LocalDateTime now = LocalDateTime.now();
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(1)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(2)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(3)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(4)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(5)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(6)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(7)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(8)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(9)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(10)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(11)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(12)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(13)));
        sut.create(new Negi(UUID.randomUUID().toString(), "desc", "path", now.plusSeconds(14)));

        // exercise
        final List<Negi> list = sut.listAfter(now.plusSeconds(2));

        // verify
        assertEquals(list.size(), 10);
        assertEquals(list.get(0).getCreateTimestamp(), now.plusSeconds(2));
        assertEquals(list.get(1).getCreateTimestamp(), now.plusSeconds(3));
        assertEquals(list.get(2).getCreateTimestamp(), now.plusSeconds(4));
        assertEquals(list.get(3).getCreateTimestamp(), now.plusSeconds(5));
        assertEquals(list.get(4).getCreateTimestamp(), now.plusSeconds(6));
        assertEquals(list.get(5).getCreateTimestamp(), now.plusSeconds(7));
        assertEquals(list.get(6).getCreateTimestamp(), now.plusSeconds(8));
        assertEquals(list.get(7).getCreateTimestamp(), now.plusSeconds(9));
        assertEquals(list.get(8).getCreateTimestamp(), now.plusSeconds(10));
        assertEquals(list.get(9).getCreateTimestamp(), now.plusSeconds(11));
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