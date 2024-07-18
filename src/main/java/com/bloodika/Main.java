package com.bloodika;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static com.bloodika.MangaDownloaderUtil.*;

@Slf4j
public class Main {

    static final String HREF_ATTRIBUTE = "href";
    static final String JPG = "jpg";
    static final String JPG_MIME = "image/jpeg";
    static final String USER_CONTENT = "googleusercontent";
    static final String A_TAG = "a";

    /**
     * Downloads from <a href="https://read-hxh.com">Home Page</a>
     * Example url: <a href="https://read-hxh.com/manga/hunter-x-hunter-chapter-1/">Download Link</a>
     *
     * @param args
     */
    public static void main(final String[] args) {
        createDirectoryIfNecessary();
        final UserInputs userInputs = readInputs();
        if (userInputs.isTheRightMode(SINGLE_CHAPTER_MODE)) {
            downloadSingleChapter(userInputs);
        } else if (userInputs.isTheRightMode(MULTIPLE_CHAPTER_MODE)) {
            downloadMultipleChapter(userInputs);
        }
    }


    @SneakyThrows
    protected static void downloadMultipleChapter(final UserInputs userInputs) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
        final URL url = userInputs.getURLAsURL();
        final Document chapters = Jsoup.parse(URI.create(url.getProtocol() + "://" + url.getHost()).toURL(), 10000);
        final List<MangaChapter> chapterUrls = chapters.getElementById("Chapters_List").getElementsByTag("a").stream().map(element -> new MangaChapter(element.attr("href"), element.text())).filter(chapter -> chapter.url().contains("chapter")).toList();
        chapterUrls.parallelStream().forEach(chapter -> {
            try {
                final UserInputs currentUserInputs = new UserInputs(chapter.url(), chapter.name(), MULTIPLE_CHAPTER_MODE);
                downloadSingleChapter(currentUserInputs);
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void downloadSingleChapter(final UserInputs userInputs) {
        try {
            final Document document = Jsoup.parse(userInputs.getURLAsURL(), 10000);
            fillPdfNameIfNecessary(userInputs, document);
            final Elements links = document.getElementsByTag(A_TAG);
            final List<Element> parsedUrls = links.stream().filter(link -> link.attr(HREF_ATTRIBUTE).contains(USER_CONTENT)).toList();
            log.info("Working on {}", userInputs.getPdfName());
            try (PDDocument pdf = new PDDocument()) {
                for (final Element parsedUrl : parsedUrls) {
                    downloadPage(parsedUrl, pdf);
                }
                pdf.save(MANGA_DOWNLOADER_PATH + "/" + userInputs.getPdfNameWithExtension());
            }
            log.info("Work done for {}", userInputs.getPdfName());
        } catch (IOException ioException) {
            log.error(ioException.getMessage(), ioException);
        }
    }


    private static void downloadPage(final Element link, final PDDocument pdf) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final URL urlToImage = URI.create(link.attr(HREF_ATTRIBUTE)).toURL();
            final BufferedImage bufferedImage = ImageIO.read(urlToImage);
            ImageIO.write(bufferedImage, JPG, bos);
            addImageToPage(pdf, bufferedImage, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void addImageToPage(final PDDocument pdf, final BufferedImage bufferedImage, final ByteArrayOutputStream bos) throws IOException {
        final PDPage currentPage = new PDPage(new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight()));
        pdf.addPage(currentPage);
        final PDImageXObject img = PDImageXObject.createFromByteArray(pdf, bos.toByteArray(), JPG_MIME);
        try (PDPageContentStream contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.drawImage(img, 20, 20, img.getWidth(), img.getHeight());
        }
    }

    private static UserInputs readInputs() {
        final Scanner sc = new Scanner(System.in);
        String url = null;
        while (!isValidUrl(url)) {
            log.info("URL: ");
            url = sc.nextLine();
        }
        String mode = null;
        while (!isValidMode(mode)) {
            log.info("Mode: \r\n1 - Single Chapter\r\n2 - All Chapters");
            mode = sc.nextLine();
        }
        return new UserInputs(url, null, mode);
    }
}