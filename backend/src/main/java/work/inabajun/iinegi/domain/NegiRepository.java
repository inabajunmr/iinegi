package work.inabajun.iinegi.domain;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ExpectedAttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            final String createDate = negi.getCreateTimestamp().toLocalDate().toString();
            final PutItemRequest request = PutItemRequest.builder().tableName(NegiSchema.TABLE_NAME)
                    .item(
                            Map.of(NegiSchema.ID, s(negi.getId()),
                                    NegiSchema.DESCRIPTION, s(negi.getDescription()),
                                    NegiSchema.IMAGE_PATH, s(negi.getImagePath()),
                                    NegiSchema.IINEGI_COUNT, n(negi.getIinegi()),
                                    NegiSchema.CREATE_DATE, s(createDate),
                                    NegiSchema.CREATE_TIMESTAMP_WITH_ID, s(negi.getCreateTimestampWithId()),
                                    NegiSchema.CREATE_TIMESTAMP, s(negi.getCreateTimestamp().toString())))
                    .expected(Map.of(NegiSchema.ID, ExpectedAttributeValue.builder().exists(false).build())).build();
            dynamoDB.putItem(request);
            LOG.info("Complete to create new Negi:" +  negi.toString());
            return negi;
        } catch (SdkClientException e) {
            throw new AssertionError("Failed to access to AWS resource.", e);
        } catch (ConditionalCheckFailedException e) {
            throw new NegiAlreadyExistException(e.getMessage(), e);
        }
    }

    public void iinegi(String id) {
        final UpdateItemRequest request = UpdateItemRequest.builder().tableName(NegiSchema.TABLE_NAME)
                .key(Map.of(NegiSchema.ID, AttributeValue.builder().s(id).build()))
                .updateExpression("SET #na = #na + :val")
                .expressionAttributeNames(Map.of("#na", NegiSchema.IINEGI_COUNT))
                .expressionAttributeValues(Map.of(":val", AttributeValue.builder().n("1").build()))
                .build();
        try {
            dynamoDB.updateItem(request);
        } catch (SdkClientException e) {
            throw new AssertionError("Failed to access to AWS resource.", e);
        } catch (DynamoDbException e) {
            throw new NegiNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Find negi.
     * @param id Negi id
     * @return Negi
     */
    public Optional<Negi> find(String id) {
        final GetItemRequest getRequest = GetItemRequest.builder().tableName(NegiSchema.TABLE_NAME)
                .key(Map.of(NegiSchema.ID, AttributeValue.builder().s(id).build()))
                .build();
        final GetItemResponse item = dynamoDB.getItem(getRequest);
        if(!item.hasItem()){
            return Optional.empty();
        }
        return Optional.of(itemToNegi(item.item()));
    }

    /**
     * Return Negi list created after args datetime.
     * @param localDateTime datetime
     * @return Negi list
     */
    public List<Negi> listAfter(LocalDateTime localDateTime) {
        LOG.info(localDateTime.toString());
        final QueryRequest request = QueryRequest.builder()
                .tableName(NegiSchema.TABLE_NAME)
                .indexName("CreateTimestamp")
                .scanIndexForward(true)
                .keyConditionExpression("CreateDate = :createDate and CreateTimestampWithId > :createTimestamp")
                .expressionAttributeValues(Map.of(":createDate", AttributeValue.builder().s(localDateTime.toLocalDate().toString()).build(),
                        ":createTimestamp", AttributeValue.builder().s(localDateTime.toString()).build()))
                .limit(10).build();
        return dynamoDB.query(request).items().stream().map(i -> itemToNegi(i)).collect(Collectors.toList());
    }

    private Negi itemToNegi(Map<String, AttributeValue> item) {
        final String id = item.get(NegiSchema.ID).s();
        final String description = item.get(NegiSchema.DESCRIPTION).s();
        final String imagePath = item.get(NegiSchema.IMAGE_PATH).s();
        final long iinegi = Long.valueOf(item.get(NegiSchema.IINEGI_COUNT).n());
        final String createTimestamp = item.get(NegiSchema.CREATE_TIMESTAMP).s();
        return new Negi(id,description,imagePath,iinegi, createTimestamp);
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
