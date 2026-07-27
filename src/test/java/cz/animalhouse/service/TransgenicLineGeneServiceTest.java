package cz.animalhouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import cz.animalhouse.dto.TransgenicLineGeneRequest;
import cz.animalhouse.dto.TransgenicLineGeneRowResponse;
import cz.animalhouse.entity.Gene;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.TransgenicLine;
import cz.animalhouse.entity.TransgenicLineGene;
import cz.animalhouse.entity.TransgenicLineGeneId;
import cz.animalhouse.exception.DuplicateTransgenicLineGeneException;
import cz.animalhouse.exception.GeneNotFoundException;
import cz.animalhouse.exception.TransgenicLineGeneNotFoundException;
import cz.animalhouse.exception.TransgenicLineNotFoundException;
import cz.animalhouse.repository.GeneRepository;
import cz.animalhouse.repository.TransgenicLineGeneRepository;
import cz.animalhouse.repository.TransgenicLineRepository;

@ExtendWith(MockitoExtension.class)
class TransgenicLineGeneServiceTest {

    @Mock
    private TransgenicLineGeneRepository
            transgenicLineGeneRepository;

    @Mock
    private TransgenicLineRepository
            transgenicLineRepository;

    @Mock
    private GeneRepository geneRepository;

    private TransgenicLineGeneService service;

    @BeforeEach
    void setUp() {
        service = new TransgenicLineGeneService(
                transgenicLineGeneRepository,
                transgenicLineRepository,
                geneRepository
        );
    }

    @Test
    void shouldFindAllAssignmentsGroupedByTransgenicLine() {
        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine line1 = createTransgenicLine(
                10L,
                strain,
                "OT-I"
        );

        TransgenicLine line2 = createTransgenicLine(
                20L,
                strain,
                "OT-II"
        );

        Gene gene1 = createGene(
                100L,
                "OT1",
                "Ovalbumin-specific T-cell receptor"
        );

        Gene gene2 = createGene(
                200L,
                "GFP",
                "Green fluorescent protein"
        );

        Gene gene3 = createGene(
                300L,
                "CRE",
                "Cre recombinase"
        );

        TransgenicLineGene assignment1 =
                new TransgenicLineGene(line1, gene1);

        TransgenicLineGene assignment2 =
                new TransgenicLineGene(line1, gene2);

        TransgenicLineGene assignment3 =
                new TransgenicLineGene(line2, gene3);

        when(transgenicLineGeneRepository.findAll())
                .thenReturn(List.of(
                        assignment1,
                        assignment2,
                        assignment3
                ));

        List<TransgenicLineGeneRowResponse> result =
                service.findAllGrouped();

        assertThat(result).hasSize(2);

        TransgenicLineGeneRowResponse firstRow =
                result.get(0);

        assertThat(firstRow.transgenicLineId())
                .isEqualTo(10L);

        assertThat(firstRow.transgenicLineName())
                .isEqualTo("OT-I");

        assertThat(firstRow.strainId())
                .isEqualTo(1L);

        assertThat(firstRow.strainCode())
                .isEqualTo("C57BL6");

        assertThat(firstRow.strainName())
                .isEqualTo("C57BL/6");

        assertThat(firstRow.genes())
                .extracting(gene -> gene.symbol())
                .containsExactly("OT1", "GFP");

        TransgenicLineGeneRowResponse secondRow =
                result.get(1);

        assertThat(secondRow.transgenicLineId())
                .isEqualTo(20L);

        assertThat(secondRow.transgenicLineName())
        		.isEqualTo("OT-II");
        
        assertThat(secondRow.genes())
                .extracting(gene -> gene.symbol())
                .containsExactly("CRE");

        verify(transgenicLineGeneRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoAssignmentsExist() {
        when(transgenicLineGeneRepository.findAll())
                .thenReturn(List.of());

        List<TransgenicLineGeneRowResponse> result =
                service.findAllGrouped();

        assertThat(result).isEmpty();

        verify(transgenicLineGeneRepository).findAll();
    }

    @Test
    void shouldCreateAssignment() {
        Long lineId = 10L;
        Long geneId = 100L;

        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine line = createTransgenicLine(
                lineId,
                strain,
                "OT-I"
        );

        Gene gene = createGene(
                geneId,
                "OT1",
                "Ovalbumin-specific T-cell receptor"
        );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        lineId,
                        geneId
                );

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(false);

        when(transgenicLineRepository.findById(lineId))
                .thenReturn(Optional.of(line));

        when(geneRepository.findById(geneId))
                .thenReturn(Optional.of(gene));

        TransgenicLineGeneRowResponse result =
                service.create(request);

        assertThat(result.transgenicLineId())
                .isEqualTo(lineId);

        assertThat(result.transgenicLineName())
                .isEqualTo("OT-I");

        assertThat(result.strainCode())
                .isEqualTo("C57BL6");

        assertThat(result.genes()).hasSize(1);

        assertThat(result.genes().getFirst().id())
                .isEqualTo(geneId);

        assertThat(result.genes().getFirst().symbol())
                .isEqualTo("OT1");

        ArgumentCaptor<TransgenicLineGene> captor =
                ArgumentCaptor.forClass(
                        TransgenicLineGene.class
                );

        verify(transgenicLineGeneRepository)
                .save(captor.capture());

        TransgenicLineGene savedAssignment =
                captor.getValue();
        
        assertThat(savedAssignment.getTransgenicLine())
                .isSameAs(line);

        assertThat(savedAssignment.getGene())
                .isSameAs(gene);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateAssignment() {
        Long lineId = 7L;
        Long geneId = 31L;

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        lineId,
                        geneId
                );

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(
                        DuplicateTransgenicLineGeneException.class
                )
                .hasMessageContaining("7")
                .hasMessageContaining("31");

        verify(transgenicLineRepository, never())
                .findById(any());

        verify(geneRepository, never())
                .findById(any());

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAssignmentForMissingTransgenicLine() {
        Long lineId = 999L;
        Long geneId = 100L;

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        lineId,
                        geneId
                );

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(false);

        when(transgenicLineRepository.findById(lineId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(
                        TransgenicLineNotFoundException.class
                )
                .hasMessageContaining("999");

        verify(geneRepository, never())
                .findById(any());

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAssignmentForMissingGene() {
        Long lineId = 10L;
        Long geneId = 999L;

        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine line = createTransgenicLine(
                lineId,
                strain,
                "OT-I"
        );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        lineId,
                        geneId
                );

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(false);

        when(transgenicLineRepository.findById(lineId))
                .thenReturn(Optional.of(line));

        when(geneRepository.findById(geneId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(GeneNotFoundException.class)
                .hasMessageContaining("999");
        
        verify(transgenicLineRepository).findById(lineId);

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateAssignmentWhenCompositeIdChanges() {
        Long currentLineId = 10L;
        Long currentGeneId = 100L;

        Long newLineId = 20L;
        Long newGeneId = 200L;

        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine currentLine =
                createTransgenicLine(
                        currentLineId,
                        strain,
                        "OT-I"
                );

        Gene currentGene = createGene(
                currentGeneId,
                "OT1",
                "Old gene"
        );

        TransgenicLineGene currentAssignment =
                new TransgenicLineGene(
                        currentLine,
                        currentGene
                );

        TransgenicLine newLine =
                createTransgenicLine(
                        newLineId,
                        strain,
                        "OT-II"
                );

        Gene newGene = createGene(
                newGeneId,
                "GFP",
                "Green fluorescent protein"
        );

        TransgenicLineGeneId currentId =
                new TransgenicLineGeneId(
                        currentLineId,
                        currentGeneId
                );

        TransgenicLineGeneId newId =
                new TransgenicLineGeneId(
                        newLineId,
                        newGeneId
                );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        newLineId,
                        newGeneId
                );

        when(transgenicLineGeneRepository.findById(currentId))
                .thenReturn(Optional.of(currentAssignment));

        when(transgenicLineGeneRepository.existsById(newId))
                .thenReturn(false);

        when(transgenicLineRepository.findById(newLineId))
                .thenReturn(Optional.of(newLine));

        when(geneRepository.findById(newGeneId))
                .thenReturn(Optional.of(newGene));

        TransgenicLineGeneRowResponse result =
                service.update(
                        currentLineId,
                        currentGeneId,
                        request
                );

        assertThat(result.transgenicLineId())
                .isEqualTo(newLineId);

        assertThat(result.transgenicLineName())
                .isEqualTo("OT-II");

        assertThat(result.genes()).hasSize(1);

        assertThat(result.genes().getFirst().id())
                .isEqualTo(newGeneId);

        assertThat(result.genes().getFirst().symbol())
                .isEqualTo("GFP");

        verify(transgenicLineGeneRepository)
                .delete(currentAssignment);

        ArgumentCaptor<TransgenicLineGene> captor =
                ArgumentCaptor.forClass(
                        TransgenicLineGene.class
                );

        verify(transgenicLineGeneRepository)
                .save(captor.capture());

        TransgenicLineGene newAssignment =
                captor.getValue();

        assertThat(newAssignment.getTransgenicLine())
                .isSameAs(newLine);

        assertThat(newAssignment.getGene())
                .isSameAs(newGene);
    }

    @Test
    void shouldNotDeleteAndInsertWhenCompositeIdHasNotChanged() {
        Long lineId = 10L;
        Long geneId = 100L;

        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine line = createTransgenicLine(
                lineId,
                strain,
                "OT-I"
        );

        Gene gene = createGene(
                geneId,
                "OT1",
                "Ovalbumin-specific T-cell receptor"
        );

        TransgenicLineGene currentAssignment =
                new TransgenicLineGene(line, gene);

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.findById(id))
                .thenReturn(Optional.of(currentAssignment));

        when(transgenicLineRepository.findById(lineId))
                .thenReturn(Optional.of(line));

        when(geneRepository.findById(geneId))
                .thenReturn(Optional.of(gene));

        TransgenicLineGeneRowResponse result =
                service.update(
                        lineId,
                        geneId,
                        request
                );

        assertThat(result.transgenicLineId())
                .isEqualTo(lineId);

        assertThat(result.genes().getFirst().id())
                .isEqualTo(geneId);

        verify(transgenicLineGeneRepository, never())
                .existsById(id);

        verify(transgenicLineGeneRepository, never())
                .delete(any());

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatedAssignmentDoesNotExist() {
        Long lineId = 10L;
        Long geneId = 100L;

        TransgenicLineGeneId currentId =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        20L,
                        200L
                );

        when(transgenicLineGeneRepository.findById(currentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.update(
                        lineId,
                        geneId,
                        request
                ))
                .isInstanceOf(
                        TransgenicLineGeneNotFoundException.class
                )
                .hasMessageContaining("10")
                .hasMessageContaining("100");

        verify(transgenicLineGeneRepository, never())
                .delete(any());

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingAssignment() {
        Long currentLineId = 10L;
        Long currentGeneId = 100L;

        Long newLineId = 20L;
        Long newGeneId = 200L;

        Strain strain = createStrain(
                1L,
                "C57BL6",
                "C57BL/6"
        );

        TransgenicLine currentLine =
                createTransgenicLine(
                        currentLineId,
                        strain,
                        "OT-I"
                );

        Gene currentGene = createGene(
                currentGeneId,
                "OT1",
                "Old gene"
        );

        TransgenicLineGene currentAssignment =
                new TransgenicLineGene(
                        currentLine,
                        currentGene
                );

        TransgenicLineGeneId currentId =
                new TransgenicLineGeneId(
                        currentLineId,
                        currentGeneId
                );

        TransgenicLineGeneId newId =
                new TransgenicLineGeneId(
                        newLineId,
                        newGeneId
                );

        TransgenicLineGeneRequest request =
                new TransgenicLineGeneRequest(
                        newLineId,
                        newGeneId
                );

        when(transgenicLineGeneRepository.findById(currentId))
                .thenReturn(Optional.of(currentAssignment));

        when(transgenicLineGeneRepository.existsById(newId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.update(
                        currentLineId,
                        currentGeneId,
                        request
                ))
                .isInstanceOf(
                        DuplicateTransgenicLineGeneException.class
                )
                .hasMessageContaining("20")
                .hasMessageContaining("200");

        verify(transgenicLineRepository, never())
                .findById(any());

        verify(geneRepository, never())
                .findById(any());

        verify(transgenicLineGeneRepository, never())
                .delete(any());

        verify(transgenicLineGeneRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteExistingAssignment() {
        Long lineId = 10L;
        Long geneId = 100L;

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(true);

        boolean result = service.delete(
                lineId,
                geneId
        );

        assertThat(result).isTrue();

        verify(transgenicLineGeneRepository)
                .existsById(id);

        verify(transgenicLineGeneRepository)
                .deleteById(id);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingAssignment() {
        Long lineId = 10L;
        Long geneId = 100L;

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        lineId,
                        geneId
                );

        when(transgenicLineGeneRepository.existsById(id))
                .thenReturn(false);

        boolean result = service.delete(
                lineId,
                geneId
        );

        assertThat(result).isFalse();

        verify(transgenicLineGeneRepository)
                .existsById(id);

        verify(transgenicLineGeneRepository, never())
                .deleteById(id);
    }

    @Test
    void shouldDeleteAllAssignmentsForTransgenicLine() {
        Long transgenicLineId = 10L;

        when(
                transgenicLineGeneRepository
                        .deleteByTransgenicLine_Id(
                                transgenicLineId
                        )
        ).thenReturn(3L);

        long result =
                service.deleteAllByTransgenicLineId(
                        transgenicLineId
                );

        assertThat(result).isEqualTo(3L);

        verify(transgenicLineGeneRepository)
                .deleteByTransgenicLine_Id(
                        transgenicLineId
                );

        verifyNoMoreInteractions(
                transgenicLineGeneRepository
        );
    }

    @Test
    void shouldReturnZeroWhenNoAssignmentsExistForTransgenicLine() {
        Long transgenicLineId = 999L;

        when(
                transgenicLineGeneRepository
                        .deleteByTransgenicLine_Id(
                                transgenicLineId
                        )
        ).thenReturn(0L);

        long result =
                service.deleteAllByTransgenicLineId(
                        transgenicLineId
                );

        assertThat(result).isZero();

        verify(transgenicLineGeneRepository)
                .deleteByTransgenicLine_Id(
                        transgenicLineId
                );
    }

    private Strain createStrain(
            Long id,
            String code,
            String name) {

        Strain strain = new Strain(code, name);

        ReflectionTestUtils.setField(
                strain,
                "id",
                id
        );

        return strain;
    }

    private TransgenicLine createTransgenicLine(
            Long id,
            Strain strain,
            String name) {

        TransgenicLine line =
                new TransgenicLine(strain, name);

        ReflectionTestUtils.setField(
                line,
                "id",
                id
        );

        return line;
    }

    private Gene createGene(
            Long id,
            String symbol,
            String description) {

        Gene gene = new Gene(symbol, description);

        ReflectionTestUtils.setField(
                gene,
                "id",
                id
        );

        return gene;
    }
}