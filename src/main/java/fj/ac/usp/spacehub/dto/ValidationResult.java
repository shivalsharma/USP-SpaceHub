package fj.ac.usp.spacehub.dto;
import java.util.*;
public record ValidationResult(boolean valid, List<String> violations) {
    public static ValidationResult ok() { return new ValidationResult(true, List.of()); }
    public static ValidationResult fail(List<String> v) { return new ValidationResult(false, List.copyOf(v)); }
}