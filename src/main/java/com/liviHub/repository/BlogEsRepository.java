package com.liviHub.repository;

import com.liviHub.model.ES.EsBlog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogEsRepository extends ElasticsearchRepository<EsBlog, String> {
    List<EsBlog> findByTitleContainingOrContentContaining(String title, String content);
}