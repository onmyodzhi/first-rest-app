package com.aleksandr.spring.firstRestApp.controllers;

import com.aleksandr.spring.firstRestApp.dto.PersonDTO;
import com.aleksandr.spring.firstRestApp.models.Person;
import com.aleksandr.spring.firstRestApp.services.PeopleService;
import com.aleksandr.spring.firstRestApp.util.PersonErrorResponse;
import com.aleksandr.spring.firstRestApp.util.PersonNotCreateException;
import com.aleksandr.spring.firstRestApp.util.PersonNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PeopleController {

    private final PeopleService peopleService;
    private final ModelMapper modelMapper;

    @GetMapping()
    public List<PersonDTO> getPeople() {
        return peopleService.findAll().stream().map(p -> modelMapper.map(p, PersonDTO.class))
                .collect(Collectors.toList());
    }


    @GetMapping("/{id}")
    public PersonDTO getPersonById(@PathVariable("id") int id) {
        return convertToDTO(peopleService.findOne(id));
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                "Person with this id wasn`t found",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid PersonDTO personDTO,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField())
                        .append(" - ")
                        .append(fieldError.getDefaultMessage())
                        .append("\n");
            }

            throw new PersonNotCreateException(errors.toString());
        }

        peopleService.save(convertToPerson(personDTO));

        return ResponseEntity.ok(HttpStatus.OK);
    }

    private Person convertToPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotCreateException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private PersonDTO convertToDTO(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }
}
