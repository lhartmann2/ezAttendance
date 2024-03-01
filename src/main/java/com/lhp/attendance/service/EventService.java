package com.lhp.attendance.service;

import com.lhp.attendance.entity.Event;

import java.util.List;

public interface EventService {

    List<Event> findAll();

    Event save(Event event);

    void deleteById(int id);

    Event findById(int id);

}
