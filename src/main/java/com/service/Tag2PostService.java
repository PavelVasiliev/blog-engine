package com.service;

import com.model.entity.Post;
import com.model.entity.Tag;
import com.repo.Tag2PostRepository;
import com.repo.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class Tag2PostService {
    private final Tag2PostRepository tag2PostRepository;
    private final TagRepository tagRepository;

    @Autowired
    public Tag2PostService(Tag2PostRepository tag2PostRepository, TagRepository tagRepository) {
        this.tag2PostRepository = tag2PostRepository;
        this.tagRepository = tagRepository;
    }

    public Set<Tag> updateTagsToPost(List<String> tagsNames, Post post) {
        Set<Tag> tags = new HashSet<>();
        Tag tag;
        tag2PostRepository.deleteAllByPostId(post.getId());
        for (String tagName : tagsNames) {
            tagName = tagName.toLowerCase();
            Optional<Tag> t = tagRepository.findTagByName(tagName);
            String finalTagName = tagName;
            tag = t.orElseGet(() -> new Tag(finalTagName));
            tag.getPosts().add(post);
            tags.add(tag);
            if (t.isEmpty()) {
                tagRepository.save(tag);
            }
        }
        return tags;
    }
}
