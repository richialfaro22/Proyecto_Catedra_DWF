package com.udb.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    private int edad;

    @Override
    public void initialize(MayorDeEdad anotacion) {
        this.edad = anotacion.edad();
    }

    @Override
    public boolean isValid(LocalDate fecha, ConstraintValidatorContext ctx) {
        if (fecha == null) {
            return true; // de los nulos se encarga @NotNull
        }
        return Period.between(fecha, LocalDate.now()).getYears() >= edad;
    }
}
