package br.edu.fatecpg.reviu.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UploadService {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("[a-zA-Z0-9]{1,10}");
    private final BlobContainerClient blobContainerClient;

    public UploadService(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename == null ? null : originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        String blobName = UUID.randomUUID().toString();
        if (extension != null && SAFE_EXTENSION.matcher(extension).matches()) {
            blobName += "." + extension;
        }

        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        String contentType = file.getContentType();
        if (contentType != null) {
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        }

        return blobClient.getBlobUrl();
    }
}

