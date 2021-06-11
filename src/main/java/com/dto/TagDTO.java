package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO implements Comparable<TagDTO> {
    private String name;
    private double weight;

    @Override
    public int compareTo(TagDTO o) {
        return normalize(o.getWeight()) - normalize(this.getWeight());
    }

    private int normalize(double tagWeight){
        return (int) (tagWeight*100);
    }
}
