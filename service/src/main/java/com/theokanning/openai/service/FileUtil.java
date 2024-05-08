package com.theokanning.openai.service;

import okhttp3.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年05月08 17:29
 **/
public class FileUtil {
    private static final Map<String, String> mimeMap = new HashMap<>();

    static {
        mimeMap.put(".c", "text/x-c");
        mimeMap.put(".cs", "text/x-csharp");
        mimeMap.put(".cpp", "text/x-c++");
        mimeMap.put(".doc", "application/msword");
        mimeMap.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeMap.put(".html", "text/html");
        mimeMap.put(".java", "text/x-java");
        mimeMap.put(".json", "application/json");
        mimeMap.put(".md", "text/markdown");
        mimeMap.put(".pdf", "application/pdf");
        mimeMap.put(".php", "text/x-php");
        mimeMap.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimeMap.put(".py", "text/x-python");
        mimeMap.put(".rb", "text/x-ruby");
        mimeMap.put(".tex", "text/x-tex");
        mimeMap.put(".txt", "text/plain");
        mimeMap.put(".css", "text/css");
        mimeMap.put(".js", "text/javascript");
        mimeMap.put(".sh", "application/x-sh");
        mimeMap.put(".ts", "application/typescript");
    }

    public static MediaType getFileUploadMediaType(String fileName) {
        return MediaType.parse(mimeMap.getOrDefault(getFileExtension(fileName), "text/plain"));
    }

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);  // Includes the dot
        }
        return "";  // No extension found
    }

    /**
     * Helper method to read all bytes from an InputStream
     *
     * @param inputStream the InputStream to read from
     * @return a byte array containing all the bytes read from the InputStream
     */
    public static byte[] readAllBytes(InputStream inputStream) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[2048];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error reading from InputStream", e);
        }
    }


}

