package ru.danilgordienko.film_storage.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.service.MovieService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {
    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
    }

//    @Test
//    public void testGetAllMovies() throws Exception {
//        Movie movie1 = new Movie();
//        movie1.setId(1L);
//        movie1.setTitle("Movie 1");
//
//        Movie movie2 = new Movie();
//        movie2.setId(2L);
//        movie2.setTitle("Movie 2");
//
//        List<Movie> movies = List.of(movie1, movie2);
//
//        when(movieService.getAllMovies()).thenReturn(movies);
//
//        mockMvc.perform(get("/api/movies"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // ожидаем JSON
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].title").value("Movie 1"))
//                .andExpect(jsonPath("$[1].title").value("Movie 2"));
//    }

    @Test
    public void testGetAllMoviesWhenEmptyList() throws Exception {

        when(movieService.getAllMovies()).thenReturn(List.of());

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isNoContent());
    }
}
