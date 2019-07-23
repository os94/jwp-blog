package techcourse.myblog.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@AutoConfigureWebTestClient
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArticleControllerTests {
    @Autowired
    private WebTestClient webTestClient;

    private static final String URI_ARTICLES = "/articles";
    private static int ARTICLE_ID;

    @BeforeEach
    void 게시글_작성() {
        webTestClient.post()
                .uri(URI_ARTICLES)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("title", "title")
                        .with("coverUrl", "coverUrl")
                        .with("contents", "contents"))
                .exchange()
                .expectStatus().isFound()
                .expectBody().consumeWith(response -> {
            String path = response.getResponseHeaders().getLocation().getPath();
            int index = path.lastIndexOf("/");
            ARTICLE_ID = Integer.parseInt(path.substring(index + 1));
        });
    }

    @Test
    void 게시글_조회() {
        statusWith(HttpMethod.GET, URI_ARTICLES + "/" + ARTICLE_ID).isOk();
    }

    @Test
    void 게시글_수정_페이지_이동() {
        statusWith(HttpMethod.GET, URI_ARTICLES + "/" + ARTICLE_ID + "/edit").isOk();
    }

    @Test
    void 게시글_수정() {
        webTestClient.put()
                .uri(URI_ARTICLES + "/" + ARTICLE_ID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("title", "updated_title")
                        .with("coverUrl", "updated_coverUrl")
                        .with("contents", "updated_contents"))
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*articles/" + ARTICLE_ID);
    }

    @AfterEach
    void 게시글_삭제() {
        statusWith(HttpMethod.DELETE, URI_ARTICLES + "/" + ARTICLE_ID)
                .isFound()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/");
    }

    private StatusAssertions statusWith(HttpMethod httpMethod, String uri) {
        return webTestClient.method(httpMethod)
                .uri(uri)
                .exchange()
                .expectStatus();
    }
}
