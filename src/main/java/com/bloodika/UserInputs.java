package com.bloodika;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.net.URL;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserInputs {
    String url;
    String pdfName;
    String mode;

    public boolean isTheRightMode(final String mode) {
        return this.mode.equals(mode);
    }

    public String getPdfNameWithExtension() {
        return this.pdfName + ".pdf";
    }

    @SneakyThrows
    public URL getURLAsURL() {
        return URI.create(this.url).toURL();
    }
}
