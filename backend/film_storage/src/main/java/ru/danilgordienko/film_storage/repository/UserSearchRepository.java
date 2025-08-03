package ru.danilgordienko.film_storage.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.danilgordienko.film_storage.model.UserDocument;

import java.util.List;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {

    List<UserDocument> searchByUsernameContaining(String username);

}
