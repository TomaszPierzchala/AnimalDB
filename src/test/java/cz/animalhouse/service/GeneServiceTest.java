package cz.animalhouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cz.animalhouse.dto.GeneCreateRequest;
import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.entity.Gene;
import cz.animalhouse.exception.DuplicateGeneSymbolException;
import cz.animalhouse.repository.GeneRepository;

@ExtendWith(MockitoExtension.class)
class GeneServiceTest {

    @Mock
    private GeneRepository geneRepository;

    private GeneService geneService;

    @BeforeEach
    void setUp() {
        geneService = new GeneService(geneRepository);
    }

    @Test
    void shouldFindAllGenes() {
        Gene gene1 = new Gene(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        Gene gene2 = new Gene(
                "GFP",
                "Green fluorescent protein");

        when(geneRepository.findAll())
                .thenReturn(List.of(gene1, gene2));

        List<GeneResponse> result = geneService.findAll();

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(GeneResponse::symbol)
                .containsExactly("OT1", "GFP");

        assertThat(result)
                .extracting(GeneResponse::description)
                .containsExactly(
                        "Ovalbumin-specific T-cell receptor",
                        "Green fluorescent protein");

        verify(geneRepository).findAll();
    }

    @Test
    void shouldCreateGene() {
        GeneCreateRequest request = new GeneCreateRequest(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        when(geneRepository.existsBySymbol("OT1"))
                .thenReturn(false);

        when(geneRepository.save(any(Gene.class)))
                .thenAnswer(invocation -> {
                    Gene gene = invocation.getArgument(0);
                    return gene;
                });

        GeneResponse result = geneService.create(request);

        assertThat(result.symbol()).isEqualTo("OT1");
        assertThat(result.description())
                .isEqualTo("Ovalbumin-specific T-cell receptor");

        verify(geneRepository).existsBySymbol("OT1");
        verify(geneRepository).save(any(Gene.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingGeneWithDuplicateSymbol() {
        GeneCreateRequest request = new GeneCreateRequest(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        when(geneRepository.existsBySymbol("OT1"))
                .thenReturn(true);

        assertThatThrownBy(() -> geneService.create(request))
                .isInstanceOf(DuplicateGeneSymbolException.class)
                .hasMessageContaining("OT1");

        verify(geneRepository).existsBySymbol("OT1");
        verify(geneRepository, never()).save(any(Gene.class));
    }

    @Test
    void shouldUpdateExistingGene() {
        Long id = 1L;

        Gene gene = new Gene(
                "OT1",
                "Old description");

        ReflectionTestUtils.setField(gene, "id", id);

        GeneCreateRequest request = new GeneCreateRequest(
                "GFP",
                "Green fluorescent protein");

        when(geneRepository.findById(id))
                .thenReturn(Optional.of(gene));

        when(geneRepository.existsBySymbolAndIdNot("GFP", id))
                .thenReturn(false);

        Optional<GeneResponse> result =
                geneService.update(id, request);

        assertThat(result).isPresent();

        GeneResponse response = result.orElseThrow();

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.symbol()).isEqualTo("GFP");
        assertThat(response.description())
                .isEqualTo("Green fluorescent protein");

        assertThat(gene.getSymbol()).isEqualTo("GFP");
        assertThat(gene.getDescription())
                .isEqualTo("Green fluorescent protein");

        verify(geneRepository).findById(id);
        verify(geneRepository)
                .existsBySymbolAndIdNot("GFP", id);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingGene() {
        Long id = 999L;

        GeneCreateRequest request = new GeneCreateRequest(
                "GFP",
                "Green fluorescent protein");

        when(geneRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<GeneResponse> result =
                geneService.update(id, request);

        assertThat(result).isEmpty();

        verify(geneRepository).findById(id);
        verify(geneRepository, never())
                .existsBySymbolAndIdNot(
                        request.getSymbol(),
                        id);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingGeneWithDuplicateSymbol() {
        Long id = 1L;

        Gene gene = new Gene(
                "OT1",
                "Old description");

        GeneCreateRequest request = new GeneCreateRequest(
                "GFP",
                "Green fluorescent protein");

        when(geneRepository.findById(id))
                .thenReturn(Optional.of(gene));

        when(geneRepository.existsBySymbolAndIdNot("GFP", id))
                .thenReturn(true);

        assertThatThrownBy(() -> geneService.update(id, request))
                .isInstanceOf(DuplicateGeneSymbolException.class)
                .hasMessageContaining("GFP");

        assertThat(gene.getSymbol()).isEqualTo("OT1");
        assertThat(gene.getDescription())
                .isEqualTo("Old description");

        verify(geneRepository).findById(id);
        verify(geneRepository)
                .existsBySymbolAndIdNot("GFP", id);
    }

	@Test
	void shouldDeleteExistingGene() {
	    Long id = 1L;
	
	    when(geneRepository.existsById(id))
	            .thenReturn(true);
	
	    boolean result = geneService.delete(id);
	
	    assertThat(result).isTrue();
	
	    verify(geneRepository).existsById(id);
	    verify(geneRepository).deleteById(id);
	}

	@Test
	void shouldReturnFalseWhenDeletingMissingGene() {
	    Long id = 1L;
	
	    when(geneRepository.existsById(id))
	            .thenReturn(false);
	
	    boolean result = geneService.delete(id);
	
	    assertThat(result).isFalse();
	
	    verify(geneRepository).existsById(id);
	    verify(geneRepository, never()).deleteById(id);
	}
}
