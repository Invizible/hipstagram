package com.github.invizible.hipstagram.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.github.invizible.hipstagram.domain.Post;
import com.github.invizible.hipstagram.repository.PostRepository;
import com.github.invizible.hipstagram.repository.UserRepository;
import com.github.invizible.hipstagram.repository.search.PostSearchRepository;
import com.github.invizible.hipstagram.security.SecurityUtils;
import com.github.invizible.hipstagram.web.rest.util.HeaderUtil;
import com.github.invizible.hipstagram.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * REST controller for managing Post.
 */
@RestController
@RequestMapping("/api")
public class PostResource {

    private final Logger log = LoggerFactory.getLogger(PostResource.class);
        
    @Inject
    private PostRepository postRepository;
    
    @Inject
    private PostSearchRepository postSearchRepository;

    @Inject
    private UserRepository userRepository;
    
    /**
     * POST  /posts : Create a new post.
     *
     * @param post the post to create
     * @return the ResponseEntity with status 201 (Created) and with body the new post, or with status 400 (Bad Request) if the post has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Post> createPost(@RequestBody Post post) throws URISyntaxException {
        log.debug("REST request to save Post : {}", post);
        if (post.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("post", "idexists", "A new post cannot already have an ID")).body(null);
        }
        post.setAuthor(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
        Post result = postRepository.save(post);
        postSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/posts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("post", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /posts : Updates an existing post.
     *
     * @param post the post to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated post,
     * or with status 400 (Bad Request) if the post is not valid,
     * or with status 500 (Internal Server Error) if the post couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@postRepository.findOne(#post.id).author.login==principal.username")
    public ResponseEntity<Post> updatePost(@Valid @RequestBody Post post) throws URISyntaxException {
        log.debug("REST request to update Post : {}", post);
        if (post.getId() == null) {
            return createPost(post);
        }
        Post result = postRepository.save(post);
        postSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("post", post.getId().toString()))
            .body(result);
    }

    /**
     * GET  /posts : get all the posts.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of posts in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Post>> getAllPosts(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Posts");
        Page<Post> page = postRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/posts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /posts/:id : get the "id" post.
     *
     * @param id the id of the post to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the post, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/posts/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        log.debug("REST request to get Post : {}", id);
        Post post = postRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(post)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /posts/:id : delete the "id" post.
     *
     * @param id the id of the post to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/posts/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@postRepository.findOne(#id).author.login==principal.username")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.debug("REST request to delete Post : {}", id);
        postRepository.delete(id);
        postSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("post", id.toString())).build();
    }

    /**
     * SEARCH  /_search/posts?query=:query : search for the post corresponding
     * to the query.
     *
     * @param query the query of the post search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/posts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Posts for query {}", query);
        Page<Post> page = postSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/posts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
