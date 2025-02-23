aws configure --profile profile1
aws configure --profile profile2
aws configure --profile profile3


AWS Access Key ID: AKIA***************
AWS Secret Access Key: ***************
Default region name: eu-west-1
Default output format [None]: json


aws configure list --profile profile1
aws configure list --profile profile2
aws configure list --profile profile3


cat ~/.aws/credentials

aws s3 ls s3://bucket1 --profile profile1
aws s3 cp s3://bucket1/file.parquet s3://bucket3/ --profile profile3

aws s3 ls ${BUCKET1}/ --profile profile1
aws s3 ls ${BUCKET2}/ --profile profile2
aws s3 cp ${BUCKET1}/$MATCHED_FILE ${BUCKET3}/$NEW_NAME --profile profile3


