package com.config;


import com.model.entity.Post;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Component
public class BlogStreams {
    private JinqJPAStreamProvider streams;

    @PersistenceUnit
    public void setEntityManagerFactory(
            EntityManagerFactory emf) throws Exception {
        streams = new JinqJPAStreamProvider(emf);
    }

    public <U> JPAJinqStream<U> streamAll(
            EntityManager em, Class<U> entity) {
        return streams.streamAll(em, entity);
    }

    public JPAJinqStream<Post> posts(EntityManager em) {
        return streamAll(em, Post.class);
    }
}