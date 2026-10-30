package com.udb.hotel.validation;

/** Expresiones regulares reutilizadas en las validaciones del lado del servidor. */
public final class Patrones {

    private Patrones() {
    }

    /** Solo letras (incluye tildes y ñ), espacios, apóstrofo y guion. */
    public static final String NOMBRE = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' -]+$";
    /** Igual que {@link #NOMBRE} pero acepta cadena vacía (campos opcionales). */
    public static final String NOMBRE_OPCIONAL = "^$|" + NOMBRE;

    /** Nombre completo: al menos dos palabras con letras (nombre y apellido). */
    public static final String NOMBRE_COMPLETO = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü'-]+( [A-Za-zÁÉÍÓÚáéíóúÑñÜü'-]+)+$";
    public static final String MSG_NOMBRE = "solo puede contener letras y espacios";

    /** Teléfono: opcional '+' inicial y de 8 a 15 dígitos, permite guiones y espacios. */
    public static final String TELEFONO = "^\\+?[0-9][0-9 -]{6,18}[0-9]$";
    public static final String TELEFONO_OPCIONAL = "^$|" + TELEFONO;
    public static final String MSG_TELEFONO = "El teléfono debe tener entre 8 y 15 dígitos (ej. 7777-8888 o +503 7777-8888)";

    /** DUI salvadoreño: 8 dígitos, guion y dígito verificador. */
    public static final String DUI = "^[0-9]{8}-[0-9]$";

    /** Pasaporte: letras mayúsculas y números de 6 a 12 caracteres. */
    public static final String PASAPORTE = "^[A-Z0-9]{6,12}$";

    /** Otro documento: alfanumérico con guiones, de 4 a 20 caracteres. */
    public static final String OTRO_DOCUMENTO = "^[A-Za-z0-9-]{4,20}$";

    /** Número de habitación: dígitos y opcionalmente una letra (101, 12B). */
    public static final String NUMERO_HABITACION = "^[0-9]{1,5}[A-Za-z]?$";

    /** Número de tarjeta: 16 dígitos. */
    public static final String TARJETA = "^[0-9]{16}$";
    public static final String TARJETA_OPCIONAL = "^$|" + TARJETA;

    /** Vencimiento de tarjeta en formato MM/AA. */
    public static final String VENCIMIENTO = "^(0[1-9]|1[0-2])/[0-9]{2}$";
    public static final String VENCIMIENTO_OPCIONAL = "^$|" + VENCIMIENTO;

    /** CVV: 3 o 4 dígitos. */
    public static final String CVV = "^[0-9]{3,4}$";
    public static final String CVV_OPCIONAL = "^$|" + CVV;
}
