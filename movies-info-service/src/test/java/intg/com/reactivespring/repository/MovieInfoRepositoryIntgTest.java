package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest //execute tests faster
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieInfo = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // .blockLast() - ensure this gets completed before the test is called. Only used for test cases
        movieInfoRepository.saveAll(movieInfo)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        var flux = movieInfoRepository.findAll().log();

        StepVerifier.create(flux).expectNextCount(3).verifyComplete();
    }

    @Test
    void findById() {
        var mono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(mono).expectNextCount(1).verifyComplete();
        StepVerifier.create(mono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        var movieInfo = new MovieInfo(null, "Dark Knight Rises2",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var mono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(mono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Dark Knight Rises2", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        // block - returns movie rather than flux/mono
        var movie = movieInfoRepository.findById("abc").block();
        assert movie != null;
        movie.setYear(2022);

        var mono = movieInfoRepository.save(movie);

        StepVerifier.create(mono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals(2022, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteById() {
        movieInfoRepository.deleteById("abc").block();
        var flux = movieInfoRepository.findAll().log();

        StepVerifier.create(flux).expectNextCount(2).verifyComplete();
    }


}