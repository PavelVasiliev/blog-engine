package com.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;


//ToDo method 36*36 avatar size
@Setter
@Getter
@NoArgsConstructor
public class BlogImage {
    public static final int MAX_SIZE = 5242880;
    private final static String PATH = "";
    private String name;
    private String path;
    private byte[] content;

    public BlogImage(String name) {
        this.name = name;
    }

    public String save(MultipartFile file, String path, boolean isAvatar) {
        this.path = path + name;
        File dir = new File(PATH + path);
        File f;
        if (!dir.exists()) {
            dir.mkdirs();
        }
        f = new File(dir.getPath() + "/" + name);
        String format = name.substring(name.lastIndexOf(".") + 1);

        try {
            ByteArrayInputStream input = new ByteArrayInputStream(file.getBytes());
            BufferedImage bi = ImageIO.read(input);
            ImageIO.write(isAvatar ? makeAvatar(bi) : bi, format, f);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.path;
    }

    public static String makePath(String root) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        String fileSeparator = System.getProperty("file.separator");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                sb.append((char) (r.nextInt(26) + 'a'));
            }
            sb.append(fileSeparator);
        }
        return root + sb;
    }

    private BufferedImage makeAvatar(BufferedImage originalImage) {
        int height = 36;
        int width = height;
        Image resultingImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}