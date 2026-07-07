package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MouseGeneIdTest {

    @Test
    void shouldCreateIdWithMouseIdAndGeneId() {

        MouseGeneId id = new MouseGeneId(10L, 20L);

        assertThat(id.getMouseId()).isEqualTo(10L);
        assertThat(id.getGeneId()).isEqualTo(20L);
    }

    @Test
    void shouldBeEqualWhenBothIdPartsAreEqual() {

        MouseGeneId id1 = new MouseGeneId(10L, 20L);
        MouseGeneId id2 = new MouseGeneId(10L, 20L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenMouseIdIsDifferent() {

        MouseGeneId id1 = new MouseGeneId(10L, 20L);
        MouseGeneId id2 = new MouseGeneId(11L, 20L);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotBeEqualWhenGeneIdIsDifferent() {

        MouseGeneId id1 = new MouseGeneId(10L, 20L);
        MouseGeneId id2 = new MouseGeneId(10L, 21L);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotBeEqualToNull() {

        MouseGeneId id = new MouseGeneId(10L, 20L);

        assertThat(id).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {

        MouseGeneId id = new MouseGeneId(10L, 20L);

        assertThat(id).isNotEqualTo("10-20");
    }

    @Test
    void shouldBeEqualToItself() {

        MouseGeneId id = new MouseGeneId(10L, 20L);

        assertThat(id).isEqualTo(id);
    }

    @Test
    void shouldHandleNullValuesInEqualsAndHashCode() {

        MouseGeneId id1 = new MouseGeneId(null, 20L);
        MouseGeneId id2 = new MouseGeneId(null, 20L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void shouldCreateReadableToString() {

        MouseGeneId id = new MouseGeneId(10L, 20L);

        assertThat(id.toString())
                .isEqualTo("(mouseId=10, geneId=20)");
    }
}