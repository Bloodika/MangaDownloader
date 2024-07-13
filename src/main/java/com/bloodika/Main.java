package com.bloodika;
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
     * @param args
     */
    public static void main(final String[] args) {
        try {
            final UserInputs userInputs = readInputs();
            final Document document = Jsoup.parse(URI.create(userInputs.url).toURL(), 10000);
            final Elements links = document.getElementsByTag(A_TAG);
            final List<Element> parsedUrls = links.stream().filter(link -> link.attr(HREF_ATTRIBUTE).contains(USER_CONTENT)).toList();
            try (PDDocument pdf = new PDDocument()) {
                for (int i = 0; i < parsedUrls.size(); i++) {
                    log.info("Working on {}. page", i + 1);
                    final Element parsedUrl = parsedUrls.get(i);
                    downloadPage(parsedUrl, pdf);
                }
                pdf.save(userInputs.pdfName + ".pdf");
            }
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

    public record UserInputs(String url, String pdfName) {
    }


    private static UserInputs readInputs() {
        final Scanner sc = new Scanner(System.in);
        log.info("URL: ");
        final String url = sc.nextLine();
        log.info("PDF Name: ");
        final String pdfName = sc.nextLine();
        return new UserInputs(url, pdfName);
    }
}