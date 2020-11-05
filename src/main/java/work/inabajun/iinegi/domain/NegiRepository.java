package work.inabajun.iinegi.domain;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.ExpectedAttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static work.inabajun.iinegi.domain.DynamoDbAttributeGenerator.n;
import static work.inabajun.iinegi.domain.DynamoDbAttributeGenerator.s;

@Singleton
public class NegiRepository {

    private static final Logger LOG = Logger.getLogger(NegiRepository.class.getName());

    @Inject
    S3Client s3;

    @Inject
    DynamoDbClient dynamoDB;

    /**
     * Persist new Negi to DynamoDB
     * @param negi new Negi
     * @return created Negi
     */
    public Negi create(Negi negi) {
        try {
            final String createDate = LocalDate.ofInstant(Instant.ofEpochMilli(negi.getCreateTimestamp()), ZoneId.of("Asia/Tokyo")).toString();
            final PutItemRequest request = PutItemRequest.builder().tableName(NegiSchema.TABLE_NAME)
                    .item(
                            Map.of(NegiSchema.ID, s(negi.getId()),
                                    NegiSchema.DESCRIPTION, s(negi.getDescription()),
                                    NegiSchema.IMAGE_PATH, s(negi.getImagePath()),
                                    NegiSchema.CREATE_DATE, s(createDate),
                                    NegiSchema.CREATE_TIMESTAMP_WITH_ID, s(negi.getCreateTimestampWithId()),
                                    NegiSchema.CREATE_TIMESTAMP, n(negi.getCreateTimestamp())))
                    .expected(Map.of(NegiSchema.ID, ExpectedAttributeValue.builder().exists(false).build())).build();
            dynamoDB.putItem(request);
            return negi;
        } catch (SdkClientException e) {
            throw new AssertionError("Failed to access to AWS resource.", e);
        } catch (ConditionalCheckFailedException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    public Negi find(String id) {
        final GetItemRequest getRequest = GetItemRequest.builder().tableName(NegiSchema.TABLE_NAME)
                .key(Map.of(NegiSchema.ID, AttributeValue.builder().s(id).build()))
                .build();
        final GetItemResponse item = dynamoDB.getItem(getRequest);
        if(!item.hasItem()){
            throw new NegiNotFoundException("Negi:" + id + " is not found.");
        }
        final String description = item.item().get(NegiSchema.DESCRIPTION).s();
        final String imagePath = item.item().get(NegiSchema.IMAGE_PATH).s();
        final Long createTimestamp = Long.valueOf(item.item().get(NegiSchema.CREATE_TIMESTAMP).n());
        return new Negi(id, description, imagePath, createTimestamp);
    }

    /**
     * Upload image data to S3.
     * @param image image
     * @return S3 object key
     * @throws NotImageException if file is not image
     */
    public String uploadImage(InputStream image) {

        final File temp;
        try {
            temp = File.createTempFile("negi", ".tmp");
        } catch (IOException e) {
            LOG.severe("Failed to create temp file for update image to S3." + e.getMessage());
            throw new AssertionError(e.getMessage(), e);
        }
        try {
            final PutObjectRequest putRequest = PutObjectRequest.builder().bucket(NegiSchema.BUCKET_NAME).key(UUID.randomUUID().toString()).build();
            Files.copy(image, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if(!isImage(temp)) {
                throw new NotImageException("This file is not image.");
            }
            s3.putObject(putRequest, RequestBody.fromFile(temp));
            return putRequest.key();
        } catch (SdkClientException e) {
            throw new AssertionError("Failed to access to AWS resource.", e);
        } catch (IOException e) {
            LOG.severe("Failed to copy file for update image to S3." + e.getMessage());
            throw new AssertionError(e.getMessage(), e);
        } finally {
            temp.delete();
        }
    }

    private boolean isImage(File file) {
        try {
            return ImageIO.read(file) != null;
        } catch (IOException e) {
            LOG.severe("Failed to read image by ImageIO.read." + e.getMessage());
            throw new AssertionError("Failed to read image by ImageIO.read.", e);
        }
    }
}
