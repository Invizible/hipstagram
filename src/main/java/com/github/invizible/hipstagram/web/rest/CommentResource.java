package com.github.invizible.hipstagram.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.github.invizible.hipstagram.domain.Comment;
import com.github.invizible.hipstagram.repository.CommentRepository;
import com.github.invizible.hipstagram.repository.search.CommentSearchRepository;
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
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Comment.
 */
@RestController
@RequestMapping("/api")
public class CommentResource {

    private final Logger log = LoggerFactory.getLogger(CommentResource.class);
        
    @Inject
    private CommentRepository commentRepository;
    
    @Inject
    private CommentSearchRepository commentSearchRepository;
    
    /**
     * POST  /comments : Create a new comment.
     *
     * @param comment the comment to create
     * @return the ResponseEntity with status 201 (Created) and with body the new comment, or with status 400 (Bad Request) if the comment has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/comments",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Comment> createComment(@Valid @RequestBody Comment comment) throws URISyntaxException {
        log.debug("REST request to save Comment : {}", comment);
        if (comment.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("comment", "idexists", "A new comment cannot already have an ID")).body(null);
        }
        Comment result = commentRepository.save(comment);
        commentSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/comments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("comment", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /comments : Updates an existing comment.
     *
     * @param comment the comment to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated comment,
     * or with status 400 (Bad Request) if the comment is not valid,
     * or with status 500 (Internal Server Error) if the comment couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/comments",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Comment> updateComment(@Valid @RequestBody Comment comment) throws URISyntaxException {
        log.debug("REST request to update Comment : {}", comment);
        if (comment.getId() == null) {
            return createComment(comment);
        }
        Comment result = commentRepository.save(comment);
        commentSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("comment", comment.getId().toString()))
            .body(result);
    }

    /**
     * GET  /comments : get all the comments.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of comments in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/comments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Comment>> getAllComments(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Comments");
        Page<Comment> page = commentRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/comments");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /comments/:id : get the "id" comment.
     *
     * @param id the id of the comment to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the comment, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/comments/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Comment> getComment(@PathVariable Long id) {
        log.debug("REST request to get Comment : {}", id);
        Comment comment = commentRepository.findOne(id);
        return Optional.ofNullable(comment)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /comments/:id : delete the "id" comment.
     *
     * @param id the id of the comment to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/comments/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        log.debug("REST request to delete Comment : {}", id);
        commentRepository.delete(id);
        commentSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("comment", id.toString())).build();
    }

    /**
     * SEARCH  /_search/comments?query=:query : search for the comment corresponding
     * to the query.
     *
     * @param query the query of the comment search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/comments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Comment>> searchComments(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Comments for query {}", query);
        Page<Comment> page = commentSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/comments");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
