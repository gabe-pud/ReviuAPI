package br.edu.fatecpg.reviu.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadServiceTest {

    @Test
    void uploadsBlobWithContentTypeAndReturnsItsUrl() throws Exception {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://account.blob.core.windows.net/reviu-app-images/file.png");
        UploadService uploadService = new UploadService(containerClient);
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3});

        String url = uploadService.uploadFile(file);

        assertEquals("https://account.blob.core.windows.net/reviu-app-images/file.png", url);
        verify(blobClient).upload(any(), eq(3L), eq(true));
        ArgumentCaptor<BlobHttpHeaders> headers = ArgumentCaptor.forClass(BlobHttpHeaders.class);
        verify(blobClient).setHttpHeaders(headers.capture());
        assertEquals("image/png", headers.getValue().getContentType());
        verify(containerClient).getBlobClient(anyString());
    }

    @Test
    void doesNotUseAnUnsafeOriginalFilenameAsBlobName() throws Exception {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        UploadService uploadService = new UploadService(containerClient);
        MockMultipartFile file = new MockMultipartFile("file", "bad name.png;other", "image/png", new byte[]{1});

        uploadService.uploadFile(file);

        var blobName = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(containerClient).getBlobClient(blobName.capture());
        assertTrue(blobName.getValue().matches("[0-9a-f-]{36}"));
    }
}
