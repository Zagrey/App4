package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import util.Greeting;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {

}
