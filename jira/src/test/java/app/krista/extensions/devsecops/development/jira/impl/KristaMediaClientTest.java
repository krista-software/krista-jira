/*
 * JIRA Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for KristaMediaClient class.
 * Tests file upload, download, and compression functionality.
 */
@DisplayName("KristaMediaClient Tests")
class KristaMediaClientTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileHandle fileHandle;

    private KristaMediaClient kristaMediaClient;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kristaMediaClient = new KristaMediaClient();
        // Use reflection to inject the mock fileRepository
        try {
            java.lang.reflect.Field field = KristaMediaClient.class.getDeclaredField("fileRepository");
            field.setAccessible(true);
            field.set(kristaMediaClient, fileRepository);
        } catch (Exception e) {
            fail("Failed to inject mock fileRepository: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should upload supported file format to Krista")
    void testToKristaFile_SupportedFormat() throws IOException {
        // Arrange
        java.io.File testFile = createTempFile("test.txt", "Test content");
        File kristaFile = mock(File.class);
        when(kristaFile.getFileName()).thenReturn("test.txt");

        when(fileRepository.createNewFileByName("test.txt")).thenReturn(fileHandle);
        when(fileHandle.getFile()).thenReturn(kristaFile);
        doNothing().when(fileHandle).setContent(any(InputStream.class));
        doNothing().when(fileHandle).close();

        // Act
        File result = kristaMediaClient.toKristaFile(testFile);

        // Assert
        assertNotNull(result);
        assertEquals("test.txt", result.getFileName());
        verify(fileRepository).createNewFileByName("test.txt");
        verify(fileHandle).setContent(any(FileInputStream.class));
        verify(fileHandle).getFile();
        verify(fileHandle).close();
    }

    @Test
    @DisplayName("Should compress and upload unsupported file format (html)")
    void testToKristaFile_UnsupportedFormat_Html() throws IOException {
        // Arrange
        java.io.File testFile = createTempFile("test.html", "<html>Test</html>");
        File kristaFile = mock(File.class);
        when(kristaFile.getFileName()).thenReturn("test.zip");

        when(fileRepository.createNewFileByName(anyString())).thenReturn(fileHandle);
        when(fileHandle.getFile()).thenReturn(kristaFile);
        doNothing().when(fileHandle).setContent(any(InputStream.class));
        doNothing().when(fileHandle).close();

        // Act
        File result = kristaMediaClient.toKristaFile(testFile);

        // Assert
        assertNotNull(result);
        verify(fileRepository).createNewFileByName(contains(".zip"));
        verify(fileHandle).setContent(any(FileInputStream.class));
        verify(fileHandle).close();
    }

    @Test
    @DisplayName("Should compress and upload unsupported file format (exe)")
    void testToKristaFile_UnsupportedFormat_Exe() throws IOException {
        // Arrange
        java.io.File testFile = createTempFile("test.exe", "Binary content");
        File kristaFile = mock(File.class);
        when(kristaFile.getFileName()).thenReturn("test.zip");

        when(fileRepository.createNewFileByName(anyString())).thenReturn(fileHandle);
        when(fileHandle.getFile()).thenReturn(kristaFile);
        doNothing().when(fileHandle).setContent(any(InputStream.class));
        doNothing().when(fileHandle).close();

        // Act
        File result = kristaMediaClient.toKristaFile(testFile);

        // Assert
        assertNotNull(result);
        verify(fileRepository).createNewFileByName(contains(".zip"));
    }

    @Test
    @DisplayName("Should download file from Krista")
    void testToJavaFile_Success() throws IOException {
        // Arrange
        File kristaFile = mock(File.class);
        when(kristaFile.getFileName()).thenReturn("download.txt");

        String content = "Downloaded content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        when(fileRepository.getFile(kristaFile)).thenReturn(fileHandle);
        when(fileHandle.getContent()).thenReturn(inputStream);
        doNothing().when(fileHandle).close();

        // Act
        java.io.File result = kristaMediaClient.toJavaFile(kristaFile);

        // Assert
        assertNotNull(result);
        assertEquals("download.txt", result.getName());
        assertTrue(result.exists());
        verify(fileRepository).getFile(kristaFile);
        verify(fileHandle).getContent();
        verify(fileHandle).close();

        // Cleanup
        result.delete();
    }



    @Test
    @DisplayName("Should compress file to zip")
    void testCompressFile() throws IOException {
        // Arrange
        java.io.File sourceFile = createTempFile("source.txt", "Content to compress");
        String zipFilePath = tempDir.resolve("output.zip").toString();

        // Act
        KristaMediaClient.compressFile(zipFilePath, sourceFile.getAbsolutePath());

        // Assert
        java.io.File zipFile = new java.io.File(zipFilePath);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        // Cleanup
        zipFile.delete();
    }

    /**
     * Helper method to create a temporary file with content
     */
    private java.io.File createTempFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
}

