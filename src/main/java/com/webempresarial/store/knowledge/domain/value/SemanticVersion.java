package com.webempresarial.store.knowledge.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value Object que representa una versión semántica con formato:
 *
 * <pre>
 * MAJOR.MINOR.PATCH
 * </pre>
 *
 * Ejemplos válidos:
 *
 * <pre>
 * 1.0.0
 * 1.2.3
 * 2.0.0
 * </pre>
 */
@Embeddable
public class SemanticVersion implements Comparable<SemanticVersion> {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$");

    @Column(
            name = "version_major",
            nullable = false
    )
    private int major;

    @Column(
            name = "version_minor",
            nullable = false
    )
    private int minor;

    @Column(
            name = "version_patch",
            nullable = false
    )
    private int patch;

    /**
     * Constructor requerido por JPA.
     */
    protected SemanticVersion() {
    }

    private SemanticVersion(
            int major,
            int minor,
            int patch
    ) {
        validatePart("major", major);
        validatePart("minor", minor);
        validatePart("patch", patch);

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static SemanticVersion of(
            int major,
            int minor,
            int patch
    ) {
        return new SemanticVersion(
                major,
                minor,
                patch
        );
    }

    public static SemanticVersion initial() {
        return new SemanticVersion(1, 0, 0);
    }

    public static SemanticVersion parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La versión semántica es obligatoria"
            );
        }

        String normalized = value.trim();
        Matcher matcher = VERSION_PATTERN.matcher(normalized);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "La versión debe cumplir el formato MAJOR.MINOR.PATCH. "
                            + "Ejemplo: 1.0.0"
            );
        }

        try {
            return new SemanticVersion(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "La versión contiene un valor numérico demasiado grande",
                    exception
            );
        }
    }

    private static void validatePart(
            String name,
            int value
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "La parte " + name
                            + " de la versión no puede ser negativa"
            );
        }
    }

    public SemanticVersion nextMajor() {
        if (major == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "No es posible incrementar la versión major"
            );
        }

        return new SemanticVersion(
                major + 1,
                0,
                0
        );
    }

    public SemanticVersion nextMinor() {
        if (minor == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "No es posible incrementar la versión minor"
            );
        }

        return new SemanticVersion(
                major,
                minor + 1,
                0
        );
    }

    public SemanticVersion nextPatch() {
        if (patch == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "No es posible incrementar la versión patch"
            );
        }

        return new SemanticVersion(
                major,
                minor,
                patch + 1
        );
    }

    public boolean isNewerThan(SemanticVersion other) {
        Objects.requireNonNull(
                other,
                "La versión comparada es obligatoria"
        );

        return compareTo(other) > 0;
    }

    public boolean isOlderThan(SemanticVersion other) {
        Objects.requireNonNull(
                other,
                "La versión comparada es obligatoria"
        );

        return compareTo(other) < 0;
    }

    public boolean isSameOrNewerThan(SemanticVersion other) {
        Objects.requireNonNull(
                other,
                "La versión comparada es obligatoria"
        );

        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(SemanticVersion other) {
        Objects.requireNonNull(
                other,
                "La versión comparada es obligatoria"
        );

        int majorComparison =
                Integer.compare(this.major, other.major);

        if (majorComparison != 0) {
            return majorComparison;
        }

        int minorComparison =
                Integer.compare(this.minor, other.minor);

        if (minorComparison != 0) {
            return minorComparison;
        }

        return Integer.compare(
                this.patch,
                other.patch
        );
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SemanticVersion that)) {
            return false;
        }

        return major == that.major
                && minor == that.minor
                && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                major,
                minor,
                patch
        );
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}