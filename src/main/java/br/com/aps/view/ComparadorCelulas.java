package br.com.aps.view;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Locale;

/**
 * Decide a ordem das linhas quando o usuario clica no cabecalho de uma coluna (as setinhas).
 * Sem isto, o Swing compara tudo como TEXTO e o "10" ficaria antes do "2".
 *   - numeros sao comparados como numeros;
 *   - datas ("27/10/2026" ou "27/10/2026 14:30") sao comparadas como datas;
 *   - textos sao comparados sem diferenciar maiusculas e acentos;
 *   - celulas vazias ou com "-" ficam sempre no comeco (na ordem crescente).
 */
final class ComparadorCelulas implements Comparator<Object> {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm[:ss]");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final Collator textos = Collator.getInstance(new Locale("pt", "BR"));

    ComparadorCelulas() {
        textos.setStrength(Collator.PRIMARY); // ignora maiusculas e acentos
    }

    @Override
    public int compare(Object a, Object b) {
        Object chaveA = chave(a);
        Object chaveB = chave(b);

        if (chaveA == null || chaveB == null) {
            return chaveA == chaveB ? 0 : (chaveA == null ? -1 : 1);
        }
        if (chaveA instanceof Double numeroA && chaveB instanceof Double numeroB) {
            return Double.compare(numeroA, numeroB);
        }
        if (chaveA instanceof LocalDateTime dataA && chaveB instanceof LocalDateTime dataB) {
            return dataA.compareTo(dataB);
        }
        return textos.compare(String.valueOf(a), String.valueOf(b));
    }

    /** Transforma o conteudo da celula em Double, LocalDateTime ou null (vazio). Outros textos viram String. */
    private Object chave(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.doubleValue();
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty() || texto.equals("-")) {
            return null;
        }
        if (texto.matches("-?\\d+([.,]\\d+)?")) {
            return Double.valueOf(texto.replace(',', '.'));
        }
        if (texto.matches("\\d{2}/\\d{2}/\\d{4}( \\d{2}:\\d{2}(:\\d{2})?)?")) {
            try {
                return texto.length() == 10
                    ? LocalDate.parse(texto, DATA).atStartOfDay()
                    : LocalDateTime.parse(texto, DATA_HORA);
            } catch (DateTimeParseException erro) {
                return texto; // data impossivel (ex.: 31/02): trata como texto
            }
        }
        return texto;
    }
}
