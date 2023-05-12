package com.ljm.mapstruct.valid;

import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.util.Objects;

@Component
public class Validator {
    public Long validateId(Long id) throws ValidationException {
        if(Objects.equals(id, -1L)){
            throw new ValidationException("Invalid value in ID");
        }
        return id;
    }
}
