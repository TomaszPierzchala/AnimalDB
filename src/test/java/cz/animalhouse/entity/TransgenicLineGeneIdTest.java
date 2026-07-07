package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransgenicLineGeneIdTest {

    @Test
    void shouldCompareIdsByValues() {

        TransgenicLineGeneId id1 =
                new TransgenicLineGeneId(1L, 2L);

        TransgenicLineGeneId id2 =
                new TransgenicLineGeneId(1L, 2L);

        TransgenicLineGeneId different =
                new TransgenicLineGeneId(1L, 3L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());

        assertThat(id1).isNotEqualTo(different);
    }

    @Test
    void shouldExposeIdParts() {

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(10L, 20L);

        assertThat(id.getTransgenicLineId()).isEqualTo(10L);
        assertThat(id.getGeneId()).isEqualTo(20L);
    }
    
    @Test
    void shouldNotBeEqualWhenTransgenicLineIdIsDifferent() {

        TransgenicLineGeneId id1 =
                new TransgenicLineGeneId(10L, 20L);

        TransgenicLineGeneId id2 =
                new TransgenicLineGeneId(11L, 20L);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotBeEqualWhenGeneIdIsDifferent() {

        TransgenicLineGeneId id1 =
                new TransgenicLineGeneId(10L, 20L);

        TransgenicLineGeneId id2 =
                new TransgenicLineGeneId(10L, 21L);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldNotBeEqualToNull() {

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(10L, 20L);

        assertThat(id).isNotEqualTo(null);
    }
    
    @Test
    void shouldBeEqualToItself() {

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(10L, 20L);

        assertThat(id).isEqualTo(id);
    }

    @Test
    void shouldHandleNullValuesInEqualsAndHashCode() {

        TransgenicLineGeneId id1 =
                new TransgenicLineGeneId(null, 20L);

        TransgenicLineGeneId id2 =
                new TransgenicLineGeneId(null, 20L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void shouldCreateReadableToString() {

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(10L, 20L);

        assertThat(id.toString())
                .isEqualTo("(transgenicLineId=10, geneId=20)");
    }
}