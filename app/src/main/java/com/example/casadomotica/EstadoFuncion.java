package com.example.casadomotica;

public class EstadoFuncion {

    public EstadoFuncion(Funcion funcion, EstadoDigital estadoDigital) {
        this.funcion = funcion;
        this.estadoDigital = estadoDigital;
    }
    EstadoDigital estadoDigital;

    Funcion funcion;

    public String getEstadoDeFuncion()  {
        return String.format("%d,%d",funcion.getValor(), estadoDigital.getValor());
    }
}
