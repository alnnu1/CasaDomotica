package com.example.casadomotica;

public enum Funcion {
    PUERTA_GARAJE(1),
    AGUA_PLANTA(2),
    LUZ_SEGUNDA_PLANTA(3),
    LUZ_PRIMERA_PLANTA(4),
    LUZ_AFUERA(5),
    VENTILADOR(6),
    ROPA(7),
    SEGURIDAD_PUERTA(8),
    ELEVADOR(9),
    ESTASEGUNDAPLANTA(10); // Don't forget the semicolon here!

    private final int valor; // Field to hold the integer value

    // Constructor to initialize the value
    Funcion(int valor) {
        this.valor = valor;
    }

    // Getter method to retrieve the value
    public int getValor() {
        return valor;
    }

    public static Funcion fromValor(int value) {
        for (Funcion funcion : Funcion.values()) { // Iterate through all enum constants
            if (funcion.getValor() == value) {         // Check if the enum's value matches the input
                return funcion;                        // Return the matching enum constant
            }
        }
        // If no matching enum constant is found, you should handle it.
        // Options:
        // 1. Throw an IllegalArgumentException
        throw new IllegalArgumentException("No Funciones enum constant with value: " + value);
        // 2. Return null (less common, usually requires null checks everywhere)
        // return null;
        // 3. Return a default/UNKNOWN enum constant (if you have one)
        // return Funciones.UNKNOWN; // If you added an UNKNOWN constant
    }
}
