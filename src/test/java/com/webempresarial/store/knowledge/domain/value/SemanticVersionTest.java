package com.webempresarial.store.knowledge.domain.value;

import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionTest {

    @Test
    void shouldCreateSemanticVersion() {
        SemanticVersion version =
                SemanticVersion.of(1, 2, 3);

        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
        assertEquals("1.2.3", version.toString());
    }

    @Test
    void shouldCreateInitialVersion() {
        SemanticVersion version =
                SemanticVersion.initial();

        assertEquals("1.0.0", version.toString());
    }

    @Test
    void shouldParseValidVersion() {
        SemanticVersion version =
                SemanticVersion.parse(" 2.5.10 ");

        assertEquals(
                SemanticVersion.of(2, 5, 10),
                version
        );
    }

    @Test
    void shouldGenerateNextMajorVersion() {
        SemanticVersion version =
                SemanticVersion.of(1, 8, 15);

        assertEquals(
                SemanticVersion.of(2, 0, 0),
                version.nextMajor()
        );
    }

    @Test
    void shouldGenerateNextMinorVersion() {
        SemanticVersion version =
                SemanticVersion.of(1, 8, 15);

        assertEquals(
                SemanticVersion.of(1, 9, 0),
                version.nextMinor()
        );
    }

    @Test
    void shouldGenerateNextPatchVersion() {
        SemanticVersion version =
                SemanticVersion.of(1, 8, 15);

        assertEquals(
                SemanticVersion.of(1, 8, 16),
                version.nextPatch()
        );
    }

    @Test
    void shouldCompareMajorVersions() {
        SemanticVersion first =
                SemanticVersion.of(2, 0, 0);

        SemanticVersion second =
                SemanticVersion.of(1, 99, 99);

        assertTrue(first.isNewerThan(second));
        assertTrue(second.isOlderThan(first));
    }

    @Test
    void shouldCompareMinorVersions() {
        SemanticVersion first =
                SemanticVersion.of(1, 5, 0);

        SemanticVersion second =
                SemanticVersion.of(1, 4, 99);

        assertTrue(first.isNewerThan(second));
    }

    @Test
    void shouldComparePatchVersions() {
        SemanticVersion first =
                SemanticVersion.of(1, 5, 2);

        SemanticVersion second =
                SemanticVersion.of(1, 5, 1);

        assertTrue(first.isNewerThan(second));
    }

    @Test
    void shouldRecognizeEquivalentVersions() {
        SemanticVersion first =
                SemanticVersion.parse("1.0.0");

        SemanticVersion second =
                SemanticVersion.of(1, 0, 0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        assertFalse(first.isNewerThan(second));
        assertFalse(first.isOlderThan(second));
        assertTrue(first.isSameOrNewerThan(second));
    }

    @Test
    void shouldRejectNegativeParts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.of(-1, 0, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.of(1, -1, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.of(1, 0, -1)
        );
    }

    @Test
    void shouldRejectNullOrBlankValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse(" ")
        );
    }

    @Test
    void shouldRejectIncompleteVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("1")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("1.0")
        );
    }

    @Test
    void shouldRejectVersionPrefix() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("v1.0.0")
        );
    }

    @Test
    void shouldRejectNegativeVersionText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("1.0.-1")
        );
    }

    @Test
    void shouldRejectLeadingZeros() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("01.0.0")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("1.01.0")
        );
    }

    @Test
    void shouldRejectNonNumericVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SemanticVersion.parse("1.A.0")
        );
    }

    @Test
    void shouldRejectNullComparison() {
        SemanticVersion version =
                SemanticVersion.initial();

        assertThrows(
                NullPointerException.class,
                () -> version.compareTo(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> version.isNewerThan(null)
        );
    }
}