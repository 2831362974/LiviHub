package com.liviHub.model.ES;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "blogs")
public class EsBlog {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;

    public EsBlog(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

}