package com.vinfern.functions;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.apache.commons.fileupload.MultipartStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class DataStorageTrigger {

    @FunctionName("dataStorage")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<byte[]>> request, final ExecutionContext context) {
        context.getLogger().info("Processing file.");

        if (!request.getHeaders().containsKey("file-type")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("File type is mandatory").build();
        }

        String containerName = request.getHeaders().get("file-type");
        context.getLogger().info(containerName);
        try {
            BlobContainerClient containerClient = getBlobContainerClient(containerName);
            byte[] body = request.getBody().orElseThrow(() -> new IllegalArgumentException("Request body is empty"));
            String contentType = validateAndGetContentType(request);
            String boundary = extractBoundary(contentType);


            List<UploadedFile> uploadedFiles = processAndUploadFiles(body, boundary, containerClient, context);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Files uploaded successfully");
            response.put("uploadedFiles", uploadedFiles);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response).build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing file upload: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading files: " + e.getMessage()).build();
        }


    }

    private String extractBoundary(String contentType) {
        String[] parts = contentType.split("boundary=");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid multipart content type: no boundary found");
        }
        return parts[1];
    }

    private String extractFileNameFromHeaders(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.startsWith("Content-Disposition") && line.contains("filename=\"")) {
                return line.substring(line.indexOf("filename=\"") + 10, line.lastIndexOf("\""));
            }
        }
        return null;
    }
    private CompletableFuture<String> uploadFileToBlobStorageAsync(String fileName, byte[] fileData, BlobContainerClient containerClient, ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream fileStream = new ByteArrayInputStream(fileData)) {
                BlobClient blobClient = containerClient.getBlobClient(fileName);
                blobClient.upload(fileStream, fileData.length, true);
                context.getLogger().info("File uploaded: " + fileName);
                return blobClient.getBlobUrl();
            } catch (Exception e) {
                context.getLogger().severe("Error uploading file " + fileName + ": " + e.getMessage());
                return null;
            }
        });
    }
    private BlobContainerClient getBlobContainerClient(String containerName) {
        String connectionString = System.getenv("AzureWebJobsStorage");

        if (connectionString == null || containerName == null) {
            throw new RuntimeException("Missing environment variables for Azure Storage connection");
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            containerClient.create();
            containerClient.setAccessPolicy(PublicAccessType.CONTAINER, null);
        }

        return containerClient;
    }

    private String validateAndGetContentType(HttpRequestMessage<Optional<byte[]>> request) {
        String contentType = request.getHeaders().get("content-type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new IllegalArgumentException("Request does not contain multipart content");
        }
        return contentType;
    }

    private List<UploadedFile> processAndUploadFiles(byte[] body, String boundary, BlobContainerClient containerClient, ExecutionContext context) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        MultipartStream multipartStream = new MultipartStream(inputStream, boundary.getBytes(), 1024, null);

        boolean nextPart = multipartStream.skipPreamble();
        List<CompletableFuture<UploadedFile>> futures = new ArrayList<>();

        while (nextPart) {
            String headers = multipartStream.readHeaders();
            context.getLogger().info("Headers: " + headers);

            String fileName = extractFileNameFromHeaders(headers);
            if (fileName != null) {
                byte[] fileData = readFileData(multipartStream);

                CompletableFuture<String> blobUriFuture = uploadFileToBlobStorageAsync(fileName, fileData, containerClient, context);
                futures.add(blobUriFuture.thenApply(blobUri -> {
                    if (blobUri != null) {
                        return new UploadedFile(fileName, blobUri);
                    } else {
                        return null;
                    }
                }));
            }

            nextPart = multipartStream.readBoundary();
        }

        // Wait for all files to be uploaded
        List<UploadedFile> uploadedFiles = new ArrayList<>();
        for (CompletableFuture<UploadedFile> future : futures) {
            UploadedFile uploadedFile = future.get();  // This blocks but waits for each future to complete
            if (uploadedFile != null) {
                uploadedFiles.add(uploadedFile);
            }
        }

        return uploadedFiles;
    }

    private byte[] readFileData(MultipartStream multipartStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        multipartStream.readBodyData(outputStream);
        return outputStream.toByteArray();
    }

    private String uploadFileToBlobStorage(String fileName, byte[] fileData, BlobContainerClient containerClient, ExecutionContext context) throws Exception {
        try (InputStream fileStream = new ByteArrayInputStream(fileData)) {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(fileStream, fileData.length, true);
            context.getLogger().info("File uploaded: " + fileName);
            context.getLogger().info("File uploaded: " + fileName);
            return blobClient.getBlobUrl(); // Return the Blob URI
        }
    }

    public record UploadedFile(String fileName, String blobUri) {
    }

}
