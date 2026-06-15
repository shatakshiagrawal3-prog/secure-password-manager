package interfaces;

/**
 * Interface: Exportable
 * Any class that can be exported to CSV or displayed
 * as a string should implement this.
 */
public interface Exportable {
    String toCSV();
    String toDisplayString();
}
