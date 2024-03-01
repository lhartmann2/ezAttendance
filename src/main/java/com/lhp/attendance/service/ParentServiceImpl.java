package com.lhp.attendance.service;

import com.lhp.attendance.dao.ParentRepository;
import com.lhp.attendance.entity.Parent;
import com.lhp.attendance.entity.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;

    public ParentServiceImpl(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    @Override
    public List<Parent> findAll() {
        return StreamSupport.stream(parentRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Parent findById(int id) {
        Optional<Parent> result = parentRepository.findById(id);
        Parent parent = null;
        if(result.isPresent()) {
            parent = result.get();
        }
        return parent;
    }

    @Override
    public Parent save(Parent parent) {
        parent.setFirstName(StringUtils.capitalize(parent.getFirstName()));
        parent.setLastName(StringUtils.capitalize(parent.getLastName()));
        parent.setCity(StringUtils.capitalize(parent.getCity()));
        return parentRepository.save(parent);
    }

    @Override
    public void deleteById(int id) {
        parentRepository.deleteById(id);
    }

    @Override
    public List<Student> getStudentsByParentId(int id) {
        return parentRepository.getStudentsByParentId(id);
    }

    @Override
    public List<Parent> findParentsByStudentId(int id) {
        return parentRepository.findParentsByStudentId(id);
    }
}
