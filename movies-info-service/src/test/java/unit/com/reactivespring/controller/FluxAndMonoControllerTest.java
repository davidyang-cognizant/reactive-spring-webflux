package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

// Gives access to all endpoints in controller
@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void flux() {
        webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .hasSize(3);
    }

    @Test
    void flux_apr2() {
        var flux = webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void flux_apr3() {
        var flux = webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                   var responseBody = listEntityExchangeResult.getResponseBody();
                   assert (Objects.requireNonNull(responseBody).size() == 3);
                });
    }

    @Test
    void stream() {
        var flux = webTestClient
                .get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }

    @Test
    void mono() {
        var flux = webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Hello world", responseBody);
                });
    }
}