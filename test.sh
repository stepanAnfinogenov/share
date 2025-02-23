#!/bin/bash

# AWS S3 Buckets
BUCKET1="s3://bucket1"
BUCKET2="s3://bucket2"
BUCKET3="s3://bucket3"

# Paths in BUCKET2
PREPARED_PATH="your_prepared_path"  # Define your specific path here
DATE_YESTERDAY=$(date -d "yesterday" +"%Y%m%d")
CURRENT_DATE=$(date +"%Y%m%d")

# Get file list from BUCKET2
FILES=$(aws s3 ls ${BUCKET2}/${PREPARED_PATH}/${DATE_YESTERDAY}/ | awk '{print $4}')
FILES+=" "$(aws s3 ls ${BUCKET2}/another_path/${DATE_YESTERDAY}/ | awk '{print $4}')

# Create a list of unique names (before "_yyyymmdd_yyyymmddHHmmssSSS.parquet")
declare -A FILE_NAMES
for FILE in $FILES; do
    BASE_NAME=$(echo "$FILE" | sed -E 's/_[0-9]{8}_[0-9]{15}\.parquet//')
    FILE_NAMES["$BASE_NAME"]=1
done

# Initialize variable to store copied files
COPIED_FILES=""

# Process each name
for NAME in "${!FILE_NAMES[@]}"; do
    MATCHED_FILE=$(aws s3 ls ${BUCKET1}/ | grep "${NAME}_" | awk '{print $4}')

    if [[ -n "$MATCHED_FILE" ]]; then
        # File found in BUCKET1 -> Copy to BUCKET3 with updated date
        NEW_NAME=$(echo "$MATCHED_FILE" | sed -E "s/[0-9]{8}/$CURRENT_DATE/")
        aws s3 cp ${BUCKET1}/$MATCHED_FILE ${BUCKET3}/$NEW_NAME
        COPIED_FILES+="Copied from BUCKET1: $MATCHED_FILE -> $NEW_NAME\n"
    else
        # File not found in BUCKET1 -> Copy from BUCKET2 to both BUCKET1 and BUCKET3
        SOURCE_FILE=$(aws s3 ls ${BUCKET2}/${PREPARED_PATH}/${DATE_YESTERDAY}/ | grep "${NAME}_" | awk '{print $4}')
        if [[ -z "$SOURCE_FILE" ]]; then
            SOURCE_FILE=$(aws s3 ls ${BUCKET2}/another_path/${DATE_YESTERDAY}/ | grep "${NAME}_" | awk '{print $4}')
        fi

        if [[ -n "$SOURCE_FILE" ]]; then
            aws s3 cp ${BUCKET2}/${PREPARED_PATH}/${DATE_YESTERDAY}/$SOURCE_FILE ${BUCKET1}/$SOURCE_FILE
            aws s3 cp ${BUCKET2}/${PREPARED_PATH}/${DATE_YESTERDAY}/$SOURCE_FILE ${BUCKET3}/$SOURCE_FILE
            COPIED_FILES+="Copied from BUCKET2: $SOURCE_FILE -> BUCKET1 & BUCKET3\n"
        else
            COPIED_FILES+="No source file found for $NAME\n"
        fi
    fi
done

# Output copied files (for email sending)
echo -e "$COPIED_FILES"
