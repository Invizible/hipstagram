package com.github.invizible.hipstagram.repository.search;

import com.github.invizible.hipstagram.domain.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Post entity.
 */
public interface PostSearchRepository extends ElasticsearchRepository<Post, Long> {
}
