package com.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class StatisticsResponse {
    private int postCount;
    private int likesCount;
    private int dislikesCount;
    private int viewsCount;
    @Setter
    private long firstPublication;

    public StatisticsResponse(int postCount, int likesCount, int dislikesCount, int viewsCount) {
        this.postCount = postCount;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.viewsCount = viewsCount;
    }
}
