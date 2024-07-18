package com.bloodika;

import org.junit.jupiter.api.Test;

import static com.bloodika.Main.downloadMultipleChapter;

public class MangaDownloaderTest {
    @Test
    public void testMultiple() {
        downloadMultipleChapter(new UserInputs("https://w11.bleach-read.com", "f", "2"));
    }
}
