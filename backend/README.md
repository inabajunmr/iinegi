# iinegi
## Testing
Prepare docker image for local test.
```
docker pull amazon/dynamodb-local
docker run -d --name dynamodb -p 8000:8000 amazon/dynamodb-local
aws --endpoint-url http://127.0.0.1:8000/ dynamodb create-table \
    --table-name Negi \
    --attribute-definitions \
        AttributeName=Id,AttributeType=S \
        AttributeName=CreateTimestampWithId,AttributeType=S \
        AttributeName=CreateDate,AttributeType=S \
    --key-schema AttributeName=Id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --global-secondary-indexes \
        IndexName=CreateTimestamp,KeySchema=["{AttributeName=CreateDate,KeyType=HASH}","{AttributeName=CreateTimestampWithId,KeyType=RANGE}"],Projection={ProjectionType=ALL},ProvisionedThroughput="{ReadCapacityUnits=1,WriteCapacityUnits=1}"
docker run -p 9090:9090 -p 9191:9191 -t adobe/s3mock
aws --endpoint-url http://localhost:9090/ s3 mb s3://negi
```