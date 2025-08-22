package com.lk.photopredict.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStore {
    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);

    private final com.google.cloud.storage.Storage storage;
    private final Bucket bucket;

    public DataStore(Storage storage, Bucket bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    public String save(InputStream data) {
        return this.save(data, makeId());
    }

    public String save(InputStream data, String fileId) {
        try {
            logger.debug("Saving data to bucket {} with ID {}", bucket.root(), fileId);
            BlobId blobId = BlobId.of(bucket.root(), fileId);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(bucket.contentType())
                    .build();
            // Upload the input stream to Google Cloud Storage
            storage.createFrom(blobInfo, data);
            logger.debug("Successfully saved data to bucket {} with ID {}", bucket.root(), fileId);
            return fileId;
        } catch (Exception e) {
            logger.error("Failed to save data to bucket {} with ID {}", bucket.root(), fileId, e);
            throw new RuntimeException(e);
        }
    }

    public String save(String data) {
        return this.save(data, makeId());
    }

    public String save(String data, String id) {
        byte[] decode = data.getBytes();
        return this.save(new ByteArrayInputStream(decode), id);
    }

    public List<String> save(Collection<String> data) {
        return data.stream().map(this::save).collect(Collectors.toList());
    }

    public List<String> ids() {
        logger.debug("Listing all IDs in bucket {}", bucket.root());
        List<String> ids = new ArrayList<>();
        Page<Blob> list = storage.list(bucket.root());
        Iterable<Blob> values = list.getValues();
        values.forEach(blob -> ids.add(blob.getName()));
        logger.debug("Found {} IDs in bucket {}", ids.size(), bucket.root());
        return ids;
    }

    // Decode

    public List<String> decodeAndSaveAll(Collection<String> data) {
        return data.stream().map(this::decodeAndSave).collect(Collectors.toList());
    }

    public String decodeAndSave(String data) {
        return this.decodeAndSave(data, makeId());
    }
    public String decodeAndSave(String data, String id) {
        byte[] decode = Base64.getDecoder().decode(data);
        return this.save(new ByteArrayInputStream(decode), id);
    }

    public String get(String id) {
        logger.debug("Getting content from bucket {} with ID {}", bucket.root(), id);
        Blob blob = storage.get(bucket.root(), id);
        if (blob == null) {
            logger.warn("No blob found in bucket {} with ID {}", bucket.root(), id);
            return null;
        }
        try {
            byte[] content = blob.getContent();
            logger.debug("Successfully retrieved content from bucket {} with ID {}", bucket.root(), id);
            return new String(content);
        } catch (Exception e) {
            logger.error("Failed to get content from bucket {} with ID {}", bucket.root(), id, e);
            throw new RuntimeException("Failed to get content from blob", e);
        }
    }

    public byte[] getBytes(String id) {
        logger.debug("Getting bytes from bucket {} with ID {}", bucket.root(), id);
        Blob blob = storage.get(bucket.root(), id);
        if (blob == null) {
            logger.warn("No blob found in bucket {} with ID {}", bucket.root(), id);
            return null;
        }
        try {
            byte[] content = blob.getContent();
            logger.debug("Successfully retrieved bytes from bucket {} with ID {}", bucket.root(), id);
            return content;
        } catch (Exception e) {
            logger.error("Failed to get bytes from bucket {} with ID {}", bucket.root(), id, e);
            throw new RuntimeException("Failed to get bytes from blob", e);
        }
    }

    private String makeId() {
        return UUID.randomUUID().toString();
    }

    public void delete(String id) {
        storage.delete(bucket.root(), id);
    }
}
