package com.service;

import com.api.response.TagResponse;
import com.dto.TagDTO;
import com.model.entity.Tag;
import com.repo.PostRepository;
import com.repo.Tag2PostRepository;
import com.repo.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    @Autowired
    public TagService(TagRepository tagRepository, PostRepository postRepository) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
    }

    public TagResponse getTagWeight(String query) {
        TagResponse tagResponse = new TagResponse();
        List<TagDTO> result = new ArrayList<>();
        Map<String, Double> temp = new TreeMap<>();

        int postsAmount = postRepository.postsActiveCurrent().size();
        int mostPopularTagAmount = 0;
        List<String> tagNames = makeTagsList(query);
        for (String tagName : tagNames) {
            int postsWithTag = (int) postRepository.streamByTag(tagName).count();
            if (postsWithTag > mostPopularTagAmount) {
                mostPopularTagAmount = postsWithTag;
            }
            if (postsWithTag > 0) {
                temp.put(tagName, normalize(postsWithTag * 1. / postsAmount));
            }
        }
        double dWeightMax = normalize(1. * mostPopularTagAmount / postsAmount);
        double k = normalize(1 / dWeightMax);
        for (String name : temp.keySet()) {
            double value = temp.get(name);
            if (value != 0.) {
                result.add(new TagDTO(name, normalize(value * k)));
            }
        }
        Collections.sort(result);
        tagResponse.setTags(result);
        deleteUnusedTags();
        return tagResponse;
    }

    private void deleteUnusedTags() {
        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            if (tag.getPosts().isEmpty()) {
                tagRepository.deleteTagByName(tag.getName());
            }
        }
    }

    private List<String> makeTagsList(String query) {
        Pattern p = Pattern.compile(query + "[\\.]?");

        List<String> queryTagNames = new ArrayList<>();
        List<String> allTagNames = tagRepository.findAll()
                .stream().map(Tag::getName).collect(Collectors.toList());
        for (String tagName : allTagNames) {
            Matcher m = p.matcher(tagName);
            if (m.find()) {
                queryTagNames.add(tagName);
            }
        }

        return queryTagNames.size() > 0 ? queryTagNames : allTagNames;
    }

    private double normalize(double d) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return Double.parseDouble(df.format(d).replace(",", "."));
    }
}
