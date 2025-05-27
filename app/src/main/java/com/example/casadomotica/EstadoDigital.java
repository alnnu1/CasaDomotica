package com.example.casadomotica;

public enum EstadoDigital {
    FORCE_OFF(0),
    FORCE_ON(1),
    SENSOR_ON(2); // Don't forget this semicolon!

    private final int valor; // Declare a private field to hold the integer value

    // Constructor to initialize the 'valor' for each enum constant
    EstadoDigital(int valor) {
        this.valor = valor;
    }

    // Public method to get the associated integer value
    public int getValor() {
        return valor;
    }

    public static EstadoDigital fromValor(int value) {
        for (EstadoDigital estado : EstadoDigital.values()) { // Iterate through all enum constants
            if (estado.getValor() == value) {         // Check if the enum's value matches the input
                return estado;                        // Return the matching enum constant
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