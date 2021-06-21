package com.service;

import com.api.response.CaptchaResponse;
import com.model.image.CaptchaImage;
import com.model.entity.Captcha;
import com.repo.CaptchaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CaptchaService {
    private static final Logger logger = LogManager.getLogger(CaptchaService.class);
    private final CaptchaRepository captchaRepository;
    private final CaptchaImage captchaImage;

    @Autowired
    public CaptchaService(CaptchaRepository captchaRepository, CaptchaImage captchaImage) {
        this.captchaRepository = captchaRepository;
        this.captchaImage = captchaImage;
    }

    public CaptchaResponse getResponse() {
        deleteExpired();

        captchaImage.makeImageData();
        String code = captchaImage.getCode();
        String secret = generateSecretCode(code);
        saveCaptcha(code, secret);

        String image = captchaImage.getImageContent();
        return new CaptchaResponse(secret, image);
    }

    public String findCodeBySecret(String secretCode) {
        return captchaRepository.findCaptchaBySecretCodeIs(secretCode).getCode();
    }

    private void deleteExpired() {
        long time = Long.parseLong(captchaImage.getLiveTime());
        List<Captcha> list = captchaRepository.findByTimeIsLessThan(new Date(System.currentTimeMillis() - time));
        logger.warn(list.size() + " captcha(s) expired. deleting..");
        list.forEach(captchaRepository::delete);
    }

    private void saveCaptcha(String code, String secretCode) {
        Date date = new Date();
        captchaRepository.save(new Captcha(date, code, secretCode));
        logger.info("New captcha generated, code - " + code);
    }

    private String generateSecretCode(String code) {
        return new String(Base64.encodeBase64(code.getBytes()));
    }
}