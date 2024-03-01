package com.lhp.attendance.service;

import com.lhp.attendance.dao.EventRepository;
import com.lhp.attendance.entity.Event;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Event> findAll() {
        return StreamSupport.stream(eventRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Event save(Event event) {
        event.setName(StringUtils.capitalize(event.getName()));
        return eventRepository.save(event);
    }

    @Override
    public void deleteById(int id) {
        eventRepository.deleteById(id);
    }

    @Override
    public Event findById(int id) {
        Optional<Event> result = eventRepository.findById(id);
        Event event = null;
        if(result.isPresent()) {
            event = result.get();
        }
        return event;
    }
}
