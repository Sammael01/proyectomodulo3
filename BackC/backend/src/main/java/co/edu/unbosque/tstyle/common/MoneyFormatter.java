package co.edu.unbosque.tstyle.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Da formato a los valores monetarios mostrados al usuario
 * (comprobante PDF y correo), por ejemplo {@code $ 1.250.000,00}.
 *
 * @author Equipo T Style
 */
public final class MoneyFormatter {

    private static final Locale COLOMBIA = Locale.of("es", "CO");

    private MoneyFormatter() {
    }

    /**
     * Formatea un valor con separador de miles, dos decimales y símbolo $.
     *
     * @param value valor a formatear
     * @return texto formateado
     */
    public static String format(BigDecimal value) {

        BigDecimal safeValue = (value == null) ? BigDecimal.ZERO : value;

        // DecimalFormat no es seguro entre hilos, por eso se crea en cada llamada.
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(COLOMBIA));

        return "$ " + format.format(safeValue);
    }
}
