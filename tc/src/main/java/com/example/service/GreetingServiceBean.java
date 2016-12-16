package com.example.service;

import util.Greeting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


//@Service
public class GreetingServiceBean implements GreetingService {

    private static AtomicLong nextId;
    private static Map<Long, Greeting> greetingMap;

    private static Greeting save(Greeting greeting) {
        if (greetingMap == null) {
            greetingMap = new HashMap<>();
            nextId = new AtomicLong(0);
        }

        nextId.incrementAndGet();
        greeting.setId(nextId.get());

        greetingMap.put(greeting.getId(), greeting);
        return greeting;
    }

    static {
        Greeting g1 = new Greeting();
        g1.setText("Hello World!");
        save(g1);

        Greeting g2 = new Greeting();
        g2.setText("Hola Mundo!");
        save(g2);
    }

    private static boolean remove(Long id) {
        Greeting deletedGreeting = greetingMap.remove(id);
        if (deletedGreeting == null) {
            return false;
        }
        return true;
    }


    @Override
    public Collection<Greeting> findAll() {

        Collection<Greeting> greetings = greetingMap.values();

        return greetings;
    }

    @Override
    public Greeting findOne(Long id) {

        Greeting greeting = greetingMap.get(id);

        return greeting;
    }

    @Override
    public Greeting create(Greeting greeting) {

        Greeting savedGreeting = save(greeting);

        return savedGreeting;
    }

    @Override
    public Greeting update(Greeting greeting) {

        Greeting updatedGreeting = save(greeting);

        return updatedGreeting;
    }

    @Override
    public void delete(Long id) {

        remove(id);

    }


}

