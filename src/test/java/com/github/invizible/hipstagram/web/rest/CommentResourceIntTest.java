package com.github.invizible.hipstagram.web.rest;

import com.github.invizible.hipstagram.HipstagramApp;
import com.github.invizible.hipstagram.domain.Comment;
import com.github.invizible.hipstagram.repository.CommentRepository;
import com.github.invizible.hipstagram.repository.search.CommentSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the CommentResource REST controller.
 *
 * @see CommentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HipstagramApp.class)
@WebAppConfiguration
@IntegrationTest
public class CommentResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TEXT = "AA";
    private static final String UPDATED_TEXT = "BB";

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_DATE_STR = dateTimeFormatter.format(DEFAULT_DATE);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private CommentSearchRepository commentSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restCommentMockMvc;

    private Comment comment;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CommentResource commentResource = new CommentResource();
        ReflectionTestUtils.setField(commentResource, "commentSearchRepository", commentSearchRepository);
        ReflectionTestUtils.setField(commentResource, "commentRepository", commentRepository);
        this.restCommentMockMvc = MockMvcBuilders.standaloneSetup(commentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        commentSearchRepository.deleteAll();
        comment = new Comment();
        comment.setText(DEFAULT_TEXT);
        comment.setDate(DEFAULT_DATE);
    }

    @Test
    @Transactional
    public void createComment() throws Exception {
        int databaseSizeBeforeCreate = commentRepository.findAll().size();

        // Create the Comment

        restCommentMockMvc.perform(post("/api/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isCreated());

        // Validate the Comment in the database
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(databaseSizeBeforeCreate + 1);
        Comment testComment = comments.get(comments.size() - 1);
        assertThat(testComment.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testComment.getDate()).isEqualTo(DEFAULT_DATE);

        // Validate the Comment in ElasticSearch
        Comment commentEs = commentSearchRepository.findOne(testComment.getId());
        assertThat(commentEs).isEqualToComparingFieldByField(testComment);
    }

    @Test
    @Transactional
    public void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentRepository.findAll().size();
        // set the field null
        comment.setText(null);

        // Create the Comment, which fails.

        restCommentMockMvc.perform(post("/api/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isBadRequest());

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentRepository.findAll().size();
        // set the field null
        comment.setDate(null);

        // Create the Comment, which fails.

        restCommentMockMvc.perform(post("/api/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isBadRequest());

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllComments() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);

        // Get all the comments
        restCommentMockMvc.perform(get("/api/comments?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(comment.getId().intValue())))
                .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
                .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE_STR)));
    }

    @Test
    @Transactional
    public void getComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);

        // Get the comment
        restCommentMockMvc.perform(get("/api/comments/{id}", comment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(comment.getId().intValue()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE_STR));
    }

    @Test
    @Transactional
    public void getNonExistingComment() throws Exception {
        // Get the comment
        restCommentMockMvc.perform(get("/api/comments/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);
        commentSearchRepository.save(comment);
        int databaseSizeBeforeUpdate = commentRepository.findAll().size();

        // Update the comment
        Comment updatedComment = new Comment();
        updatedComment.setId(comment.getId());
        updatedComment.setText(UPDATED_TEXT);
        updatedComment.setDate(UPDATED_DATE);

        restCommentMockMvc.perform(put("/api/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedComment)))
                .andExpect(status().isOk());

        // Validate the Comment in the database
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(databaseSizeBeforeUpdate);
        Comment testComment = comments.get(comments.size() - 1);
        assertThat(testComment.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testComment.getDate()).isEqualTo(UPDATED_DATE);

        // Validate the Comment in ElasticSearch
        Comment commentEs = commentSearchRepository.findOne(testComment.getId());
        assertThat(commentEs).isEqualToComparingFieldByField(testComment);
    }

    @Test
    @Transactional
    public void deleteComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);
        commentSearchRepository.save(comment);
        int databaseSizeBeforeDelete = commentRepository.findAll().size();

        // Get the comment
        restCommentMockMvc.perform(delete("/api/comments/{id}", comment.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean commentExistsInEs = commentSearchRepository.exists(comment.getId());
        assertThat(commentExistsInEs).isFalse();

        // Validate the database is empty
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);
        commentSearchRepository.save(comment);

        // Search the comment
        restCommentMockMvc.perform(get("/api/_search/comments?query=id:" + comment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comment.getId().intValue())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE_STR)));
    }
}
