package com.bloodika;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Slf4j
public final class MangaDownloaderUtil {
    public static final String SINGLE_CHAPTER_MODE = "1";
    public static final String MULTIPLE_CHAPTER_MODE = "2";
    public static final Path MANGA_DOWNLOADER_PATH = Path.of("MangaDownloader");

    private MangaDownloaderUtil() {
    }


    public static void fillPdfNameIfNecessary(final UserInputs userInputs, final Document document) {
        if (userInputs.getPdfName() == null) {
            userInputs.setPdfName(document.getElementsByClass("entry-title").get(0).text());
        }
    }

    public static boolean isValidMode(final String mode) {
        return mode != null && List.of(SINGLE_CHAPTER_MODE, MULTIPLE_CHAPTER_MODE).contains(mode);
    }

    public static boolean isValidUrl(final String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            URI.create(url).toURL();
            return true;
        } catch (IllegalArgumentException | MalformedURLException exception) {
            return false;
        }
    }

    static void createDirectoryIfNecessary() {
        try {
            if (!Files.exists(MANGA_DOWNLOADER_PATH) || Files.isDirectory(MANGA_DOWNLOADER_PATH)) {
                Files.createDirectory(MANGA_DOWNLOADER_PATH);
            }
        } catch (IOException exception) {
            log.error("Couldn't create directory [MangaDownloader] to location!");
        }
    }

}
