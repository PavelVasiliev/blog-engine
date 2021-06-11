package com.service;

import com.api.response.CalendarResponse;
import com.dto.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final int BLOG_LAUNCH_YEAR = 2011;
    private final CalendarResponse calendarResponse;
    private final PostService postService;

    @Autowired
    public CalendarService(CalendarResponse calendarResponse, PostService postService) {
        this.calendarResponse = calendarResponse;
        this.postService = postService;
    }

    public CalendarResponse getCalendarResponse(String year) {
        setData();
        Map<String, Integer> result = new HashMap<>();

        Map<String, Integer> temp = getPostsDatesByYear(year);
        for (String key : temp.keySet()) {
            result.put(key, temp.get(key));
        }
        calendarResponse.setPosts(result);
        return calendarResponse;
    }

    private void setData() {
        List<Integer> years = new ArrayList<>();
        Map<String, Integer> result = new HashMap<>();

        for (int i = BLOG_LAUNCH_YEAR; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
            List<PostDTO> posts = postService.getPostsByYear(i);
            if (posts.size() > 0) {
                years.add(i);
            }
            for (PostDTO post : posts) {
                String date = FORMAT.format(new Date(post.getTimestamp() * 1000));
                result.put(date, result.containsKey(date) ? (result.get(date) + 1) : 1);
            }
        }
        calendarResponse.setYears(years);
        calendarResponse.setPosts(result);
    }

    private Map<String, Integer> getPostsDatesByYear(String year) {
        Map<String, Integer> result = calendarResponse.getPosts();
        Map<String, Integer> temp = new HashMap<>(result);
        for (String key : result.keySet()) {
            if (!key.startsWith(year)) {
                temp.remove(key);
            }
        }
        result = temp;
        return sort(result);
    }

    private Map<String, Integer> sort(Map<String, Integer> map) {
        return map.entrySet().
                stream().sorted(Map.Entry.
                comparingByValue(Comparator.reverseOrder())).
                collect(Collectors.toMap
                        (Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }
}
