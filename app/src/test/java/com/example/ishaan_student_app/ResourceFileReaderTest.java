package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.file.ResourceFileReader;
import com.example.ishaan_student_app.data.model.CampusResource;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResourceFileReaderTest {

    @Test
    public void testParseCsvContentValid() {
        String csvContent = "Title,Category,Description\n" +
                "\"Academic Programs Guide\",\"Guide\",\"Overview of STME, SOC, and SOL programs\"\n" +
                "\"Campus Services\",\"Information\",\"Official student portal, administration\"\n";

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        List<CampusResource> resources = ResourceFileReader.parseCsvContent(inputStream);

        assertNotNull(resources);
        assertEquals(2, resources.size());

        assertEquals("Academic Programs Guide", resources.get(0).getTitle());
        assertEquals("Guide", resources.get(0).getCategory());
        assertEquals("Overview of STME, SOC, and SOL programs", resources.get(0).getDescription());

        assertEquals("Campus Services", resources.get(1).getTitle());
        assertEquals("Information", resources.get(1).getCategory());
    }

    @Test
    public void testCsvLineTokenizerWithQuotedCommas() {
        String line = "\"Title with, comma\",\"Category, Subcategory\",\"Description with \"\"quoted\"\" text\"";
        List<String> tokens = ResourceFileReader.parseCsvLine(line);

        assertEquals(3, tokens.size());
        assertEquals("Title with, comma", tokens.get(0));
        assertEquals("Category, Subcategory", tokens.get(1));
        assertEquals("Description with \"quoted\" text", tokens.get(2));
    }

    @Test
    public void testParseCsvContentEmptyOrNull() {
        List<CampusResource> resources = ResourceFileReader.parseCsvContent(null);
        assertNotNull(resources);
        assertTrue(resources.isEmpty());
    }
}
