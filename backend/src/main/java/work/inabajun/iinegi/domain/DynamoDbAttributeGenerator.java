package work.inabajun.iinegi.domain;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbAttributeGenerator {

    public static AttributeValue s(String value) {
        return AttributeValue.builder().s(value).build();
    }

    public static AttributeValue n(long value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

}
