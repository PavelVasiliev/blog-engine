package com.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import com.cloudinary.StoredFile;
import com.cloudinary.utils.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class BlogImageService extends StoredFile {
    private static final Logger logger = LogManager.getLogger(BlogImageService.class);
    private static Cloudinary cloudinary;

    private static void initCloudinary() {
        if (cloudinary == null) {
            cloudinary = Singleton.getCloudinary();
        }
    }

    public static String save(MultipartFile file, String path, boolean isAvatar) {
        String original = file.getOriginalFilename();
        if (original == null) {
            String log = "No file to upload: " + path;
            logger.warn(log);
            return log;
        }
        String name = original.substring(0, original.lastIndexOf("."));
        String format = original.substring(original.lastIndexOf(".") + 1);
        byte[] image;
        try {
            image = isAvatar ? makeAvatar(file.getInputStream(), format) : file.getBytes();
            path = upload(image, path, name);
        } catch (IOException e) {
            logger.error("Cant get multipart file input stream." + e);
        }

        return path;
    }

    private static String upload(byte[] image, String path, String name) throws IOException {
        initCloudinary();
        return (String) cloudinary.uploader().upload(image,
                ObjectUtils.asMap("resource_type", "auto",
                        "folder", path,
                        "public_id", name)).get("url");
    }

    private static byte[] makeAvatar(InputStream is, String format) {
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(is);
        } catch (IOException e) {
            logger.error("Cant read image input stream." + e);
        }
        if (originalImage == null) {
            String log = "No file for avatar selected.";
            logger.warn(log);
            return new byte[0];
        }
        int height;
        int width = height = 36;
        Image resultingImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return toByteArray(outputImage, format);
    }

    private static byte[] toByteArray(BufferedImage bi, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, format, baos);
        } catch (IOException e) {
            logger.error("Cant write image." + e);
        }
        return baos.toByteArray();
    }
}
