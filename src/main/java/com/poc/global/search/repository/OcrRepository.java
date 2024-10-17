package com.poc.global.search.repository;

import com.poc.global.search.entity.Tokens;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OcrRepository extends MongoRepository<Tokens, String> {
	Optional<Tokens> findByToken(String token);
}
