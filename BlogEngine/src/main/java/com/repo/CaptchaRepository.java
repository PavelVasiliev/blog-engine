package com.repo;

import com.model.entity.Captcha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CaptchaRepository extends JpaRepository<Captcha, Integer> {
    Captcha findCaptchaBySecretCodeIs(String secretCode);

    List<Captcha> findByTimeIsLessThan(Date time);
}
