package interfaces;

/**
 * Interface: Searchable
 * Any class that can be searched by a keyword should implement this.
 */
public interface Searchable {
    boolean matches(String keyword);
}
