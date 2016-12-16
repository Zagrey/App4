package com.example.service;

import com.example.repository.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import util.Greeting;

import java.util.Collection;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class GreetingServiceBeanJpa implements GreetingService {
    @Autowired
    private GreetingRepository greetingRepository;

    @Cacheable(
            value = "greetings", key = "100"
            )
    @Override
    public Collection<Greeting> findAll() {

        Collection<Greeting> greetings = greetingRepository.findAll();

        return greetings;
    }

    @Cacheable(
            value = "greetings",
            key = "#id")
    @Override
    public Greeting findOne(Long id) {

        Greeting greeting = greetingRepository.findOne(id);

        return greeting;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CachePut(
            value = "greetings",
            key = "#result.id")
    @CacheEvict(
            value = "greetings",
            key = "100")
    public Greeting create(Greeting greeting) {

        // Ensure the entity object to be created does NOT exist in the
        // repository. Prevent the default behavior of save() which will update
        // an existing entity if the entity matching the supplied id exists.
        if (greeting.getId() != null) {
            // Cannot create Greeting with specified ID value
            return null;
        }

        Greeting savedGreeting = greetingRepository.save(greeting);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return savedGreeting;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CachePut(
            value = "greetings",
            key = "#greeting.id")
    public Greeting update(Greeting greeting) {

        // Ensure the entity object to be updated exists in the repository to
        // prevent the default behavior of save() which will persist a new
        // entity if the entity matching the id does not exist
        Greeting greetingToUpdate = findOne(greeting.getId());
        if (greetingToUpdate == null) {
            // Cannot update Greeting that hasn't been persisted
            return null;
        }

        greetingToUpdate.setText(greeting.getText());
        Greeting updatedGreeting = greetingRepository.save(greetingToUpdate);

        return updatedGreeting;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(
            value = "greetings",
            key = "#id")
    public void delete(Long id) {

        greetingRepository.delete(id);

    }

}