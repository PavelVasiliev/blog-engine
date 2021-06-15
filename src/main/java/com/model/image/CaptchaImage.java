package com.model.image;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cage.Cage;
import com.github.cage.image.Painter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "captcha", ignoreUnknownFields = false)
public class CaptchaImage {
    private static Painter.Quality quality = Painter.Quality.MAX;
    private static float compressRatio = 1.0f;

    private String width;
    private String height;
    private String format;
    @JsonProperty("live_time")
    private String liveTime;

    private String imageContent;
    private String url;
    private String code;

    public void makeImageData() {
        Painter p = new Painter(this.getWidth(), this.getHeight(),
                null, quality, null, null);
        Cage cage = new Cage(p, null, null, format, compressRatio, null, null);

        this.code = cage.getTokenGenerator().next();
        byte[] image = cage.draw(code);
        this.imageContent = url + " " + Base64.getEncoder().encodeToString(image);
    }

    public int getWidth() {
        return Integer.parseInt(width);
    }

    public int getHeight() {
        return Integer.parseInt(height);
    }
}

