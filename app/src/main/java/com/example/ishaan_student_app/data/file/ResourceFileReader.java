package com.example.ishaan_student_app.data.file;

import android.content.Context;
import android.util.Log;

import com.example.ishaan_student_app.data.model.CampusResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses bundled campus resource CSV/TXT files at runtime using Java I/O.
 * Implements compliant CSV tokenization supporting quoted strings, escaped quotes, and commas inside quotes.
 */
public class ResourceFileReader {

    private static final String TAG = "ResourceFileReader";

    public static List<CampusResource> readResourcesFromAsset(Context context, String fileName) {
        List<CampusResource> resources = new ArrayList<>();
        if (context == null || fileName == null || fileName.trim().isEmpty()) {
            return resources;
        }

        try (InputStream is = context.getAssets().open(fileName.trim())) {
            return parseCsvContent(is);
        } catch (Exception e) {
            Log.e(TAG, "Error reading asset file: " + fileName, e);
        }

        return resources;
    }

    public static List<CampusResource> parseCsvContent(InputStream inputStream) {
        List<CampusResource> resources = new ArrayList<>();
        if (inputStream == null) {
            return resources;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> tokens = parseCsvLine(line);
                if (tokens.isEmpty()) {
                    continue;
                }

                // Skip header line if first token is "Title"
                if (isHeader && "title".equalsIgnoreCase(tokens.get(0).trim())) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                String title = tokens.size() > 0 ? tokens.get(0) : "";
                String category = tokens.size() > 1 ? tokens.get(1) : "General";
                String description = tokens.size() > 2 ? tokens.get(2) : "";

                if (!title.isEmpty()) {
                    resources.add(new CampusResource(title, category, description));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV stream", e);
        }

        return resources;
    }

    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) {
            return tokens;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }
}
