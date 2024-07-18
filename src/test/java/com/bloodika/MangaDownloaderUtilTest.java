package com.bloodika;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.bloodika.MangaDownloaderUtil.isValidMode;
import static com.bloodika.MangaDownloaderUtil.isValidUrl;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MangaDownloaderUtilTest {
    @Test
    public void testIsValidMode() {
        assertTrue(isValidMode(MangaDownloaderUtil.SINGLE_CHAPTER_MODE));
        assertTrue(isValidMode(MangaDownloaderUtil.MULTIPLE_CHAPTER_MODE));
        assertFalse(isValidMode(null));
        assertFalse(isValidMode(""));
        assertFalse(isValidMode("3"));
    }

    @Test
    public void testIsValidUrl() {
        assertTrue(isValidUrl("https://google.com"));
        assertFalse(isValidUrl(null));
        assertFalse(isValidUrl(""));
        assertFalse(isValidUrl("faff"));
    }

}
