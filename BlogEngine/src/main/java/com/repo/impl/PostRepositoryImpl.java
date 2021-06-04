package com.repo.impl;

import com.config.BlogStreams;
import com.model.blog_enum.BlogGlobalSettings;
import com.model.blog_enum.PostStatus;
import com.model.entity.GlobalSettings;
import com.model.entity.Post;
import com.model.entity.Tag;
import com.repo.PostRepository;
import com.repo.SettingsRepository;
import org.jinq.jpa.JPAJinqStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.model.entity.Post.PostCommentsSort;

@Component(value = "postRepositoryImpl")
public class PostRepositoryImpl implements PostRepository {
    @PersistenceContext
    private final EntityManager entityManager;
    private final BlogStreams streams;
    private final SettingsRepository settingsRepository;

    @Autowired
    public PostRepositoryImpl(EntityManager entityManager, BlogStreams streams,
                              SettingsRepository settingsRepository) {
        this.entityManager = entityManager;
        this.streams = streams;
        this.settingsRepository = settingsRepository;
    }

    private JPAJinqStream<Post> getPostsStream() {
        return streams.posts(entityManager);
    }

    private boolean getStatusPremoderation(){
        GlobalSettings setting = settingsRepository
                .findByCode(BlogGlobalSettings.valueOf(BlogGlobalSettings.POST_PREMODERATION.name()).name());
        return setting.getBooleanValue();
    }

    @Override
    public List<Post> postsByUserId(int id) {
        return getPostsStream()
                .filter(p -> p.getUser().getId() == id)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> postsByUserId(int userId, PostStatus status, int offset, int limit) {
        if (status == null) {
            return getPostsStream()
                    .filter(post -> post.getUser().getId() == userId & !post.isPostActive())
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        return getPostsStream().filter(post -> post.getUser().getId() == userId & post.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> postsActiveCurrent() {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .filter(Post::isPostActive)
                .filter(p -> p.getPublicationDate().before(new Date()))
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> postsByCurrent(int offset, int limit) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .sortedDescendingBy(Post::getPublicationDate)
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> postsByOld(int offset, int limit) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .sortedBy(Post::getPublicationDate)
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> postsByScore(int offset, int limit) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .sorted()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }


    @Override
    public List<Post> postsByPopularity(int offset, int limit) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;

        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .sorted(PostCommentsSort)
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Post> streamByStatus(PostStatus status) {
        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status));
    }

    @Override
    public Stream<Post> streamByDateBetween(Date after, Date before) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .filter(Post::isPostActive)
                .filter(post -> post.getPublicationDate().after(after))
                .filter(post -> post.getPublicationDate().before(before))
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED));
    }

    @Override
    public Stream<Post> streamByQuery(String query) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .filter(p -> p.getText().contains(query) || p.getTitle().contains(query));
    }

    @Override
    public Stream<Post> streamByTag(String tag) {
        PostStatus status = getStatusPremoderation() ? PostStatus.ACCEPTED : PostStatus.NEW;
        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getStatus().equals(status) || p.getStatus().equals(PostStatus.ACCEPTED))
                .filter(post -> new ArrayList<>(
                        post.getTags()).stream()
                        .map(Tag::getName)
                        .anyMatch(t -> t.contains(tag)));
    }

    @Override
    public Stream<Post> streamByModeratorIdAndStatus(int moderatorId, PostStatus status) {
        return getPostsStream()
                .filter(p -> p.getPublicationDate().before(new Date()) && p.isPostActive())
                .filter(p -> p.getModerator() != null && p.getModerator().getId() == moderatorId)
                .filter(p -> p.getStatus().equals(status));
    }

    @Override
    public Optional<Post> optionalPostById(int id) {
        return getPostsStream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    @Override
    public List<Post> findAll() {
        return getPostsStream().collect(Collectors.toList());
    }

    @Override
    public List<Post> findAll(Sort sort) {
        List<Post> result = findAll();
        Collections.sort(result);
        return result;
    }

    @Override
    public long count() {
        return findAll().size();
    }

    @Transactional
    @Override
    public <S extends Post> S save(S post) {
        if (post.getUser() == null) {
            entityManager.persist(post);
            return post;
        }
        return entityManager.merge(post);
    }

    @Override
    public <S extends Post> List<S> saveAll(Iterable<S> iterable) {
        List<S> result = new ArrayList<>();
        for (S p : iterable) {
            result.add(p);
            save(p);
        }
        return result;
    }

    @Override
    public Optional<Post> findById(Integer id) {
        return getPostsStream().filter(post -> post.getId() == id).findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    @Override
    public Post getOne(Integer id) {
        return findById(id).orElseThrow();
    }

    //FixMe default methods if needed
    @Override
    public <S extends Post> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }


    @Override
    public Page<Post> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Post> findAllById(Iterable<Integer> iterable) {
        return null;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(Post post) {

    }

    @Override
    public void deleteAll(Iterable<? extends Post> iterable) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Post> S saveAndFlush(S s) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Post> iterable) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public <S extends Post> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Post> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Post> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Post> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Post> boolean exists(Example<S> example) {
        return false;
    }
}
