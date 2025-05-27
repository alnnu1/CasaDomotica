package com.example.casadomotica;

public class EstadoFuncionAnalogo {

    public EstadoFuncionAnalogo(Funcion funcion, Integer value) {
        this.funcion = funcion;
        this.valor = value;
        this.UsarSensor = false;
    }

    public EstadoFuncionAnalogo(Funcion funcion, Boolean UsarSensor) {
        this.funcion = funcion;
        this.UsarSensor = UsarSensor;
    }

    Funcion funcion;

    Integer valor;

    Boolean UsarSensor = false;

}
