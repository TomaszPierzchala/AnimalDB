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

import cz.animalhouse.dto.StrainTransgenicLineRequest;
import cz.animalhouse.dto.StrainTransgenicLineResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.StrainTransgenicLine;
import cz.animalhouse.exception.DuplicateStrainTransgenicLineNameException;
import cz.animalhouse.exception.StrainNotFoundException;
import cz.animalhouse.repository.StrainRepository;
import cz.animalhouse.repository.StrainTransgenicLineRepository;

@ExtendWith(MockitoExtension.class)
class StrainTransgenicLineServiceTest {

    @Mock
    private StrainTransgenicLineRepository transgenicLineRepository;

    @Mock
    private StrainRepository strainRepository;

    private StrainTransgenicLineService transgenicLineService;

    @BeforeEach
    void setUp() {
        transgenicLineService = new StrainTransgenicLineService(
                transgenicLineRepository,
                strainRepository);
    }

    @Test
    void shouldFindAllTransgenicLines() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        StrainTransgenicLine line1 =
                new StrainTransgenicLine(strain, "OT-I");

        StrainTransgenicLine line2 =
                new StrainTransgenicLine(strain, "OT-II");

        when(transgenicLineRepository.findAll())
                .thenReturn(List.of(line1, line2));

        List<StrainTransgenicLineResponse> result =
                transgenicLineService.findAll();

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(StrainTransgenicLineResponse::name)
                .containsExactly("OT-I", "OT-II");
    }

    @Test
    void shouldCreateTransgenicLine() {

        final Long strainId = 1L;

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        strainId,
                        "OT-I");

        when(transgenicLineRepository.existsByName("OT-I"))
                .thenReturn(false);

        when(strainRepository.findById(strainId))
                .thenReturn(Optional.of(strain));

        when(transgenicLineRepository.save(
                org.mockito.ArgumentMatchers.any(
                        StrainTransgenicLine.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        StrainTransgenicLineResponse result =
                transgenicLineService.create(request);

        assertThat(result.name()).isEqualTo("OT-I");
        assertThat(result.strainCode()).isEqualTo("C57BL6");

        verify(transgenicLineRepository)
                .save(org.mockito.ArgumentMatchers.any(
                        StrainTransgenicLine.class));
    }

    @Test
    void shouldRejectDuplicateNameDuringCreate() {

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        1L,
                        "OT-I");

        when(transgenicLineRepository.existsByName("OT-I"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                transgenicLineService.create(request))
                .isInstanceOf(DuplicateStrainTransgenicLineNameException.class)
                .hasMessageContaining("Strain/transgenic line with name 'OT-I' already exists");

        verify(strainRepository, never())
                .findById(1L);

        verify(transgenicLineRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingTransgenicLineForMissingStrain() {
        final Long strainId = 999L;

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        strainId,
                        "OT-I"
                );

        when(transgenicLineRepository.existsByName("OT-I"))
                .thenReturn(false);

        when(strainRepository.findById(strainId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transgenicLineService.create(request))
                .isInstanceOf(StrainNotFoundException.class)
                .hasMessageContaining("Strain with ID 999 does not exist");

        verify(transgenicLineRepository)
                .existsByName("OT-I");

        verify(strainRepository)
                .findById(strainId);

        verify(transgenicLineRepository, never())
                .save(any(StrainTransgenicLine.class));
    }

    @Test
    void shouldUpdateExistingTransgenicLine() {
        final Long id = 1L;
        final Long newStrainId = 20L;

        Strain oldStrain = new Strain(
                "C57BL6",
                "C57BL/6"
        );

        Strain newStrain = new Strain(
                "BALBC",
                "BALB/c"
        );

        StrainTransgenicLine transgenicLine = new StrainTransgenicLine(
                oldStrain,
                "OT-I"
        );

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        newStrainId,
                        "OT-II"
                );

        when(transgenicLineRepository.findById(id))
                .thenReturn(Optional.of(transgenicLine));

        when(transgenicLineRepository
                .existsByNameAndIdNot("OT-II", id))
                .thenReturn(false);

        when(strainRepository.findById(newStrainId))
                .thenReturn(Optional.of(newStrain));


        Optional<StrainTransgenicLineResponse> result =
                transgenicLineService.update(id, request);


        assertThat(result).isPresent();

        StrainTransgenicLineResponse response = result.orElseThrow();

        assertThat(response.name()).isEqualTo("OT-II");
        assertThat(response.strainCode()).isEqualTo("BALBC");
        assertThat(response.strainName()).isEqualTo("BALB/c");

        assertThat(transgenicLine.getName()).isEqualTo("OT-II");
        assertThat(transgenicLine.getStrain()).isSameAs(newStrain);

        verify(transgenicLineRepository).findById(id);

        verify(transgenicLineRepository)
                .existsByNameAndIdNot("OT-II", id);

        verify(strainRepository).findById(newStrainId);
    }

    @Test
    void shouldReturnEmptyWhenUpdatedRecordDoesNotExist() {

        final Long id = 10L;
        final Long strainId = 1L;

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        strainId,
                        "OT-I");

        when(transgenicLineRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<StrainTransgenicLineResponse> result =
                transgenicLineService.update(id, request);

        assertThat(result).isEmpty();

        verify(strainRepository, never())
                .findById(strainId);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTransgenicLineWithDuplicateName() {
        final Long id = 1L;
        final Long newStrainId = 20L;

        Strain oldStrain = new Strain(
                "C57BL6",
                "C57BL/6"
        );

        StrainTransgenicLine transgenicLine = new StrainTransgenicLine(
                oldStrain,
                "OT-I"
        );

        StrainTransgenicLineRequest request =
                new StrainTransgenicLineRequest(
                        newStrainId,
                        "OT-II"
                );

        when(transgenicLineRepository.findById(id))
                .thenReturn(Optional.of(transgenicLine));

        when(transgenicLineRepository
                .existsByNameAndIdNot("OT-II", id))
                .thenReturn(true);

        assertThatThrownBy(() ->
                transgenicLineService.update(id, request))
                .isInstanceOf(
                        DuplicateStrainTransgenicLineNameException.class)
                .hasMessageContaining("OT-II");

        assertThat(transgenicLine.getName()).isEqualTo("OT-I");
        assertThat(transgenicLine.getStrain()).isSameAs(oldStrain);

        verify(transgenicLineRepository).findById(id);

        verify(transgenicLineRepository)
                .existsByNameAndIdNot("OT-II", id);

        verify(strainRepository, never()).findById(newStrainId);
    }

    @Test
    void shouldDeleteExistingTransgenicLine() {

        final Long id = 10L;

        when(transgenicLineRepository.existsById(id))
                .thenReturn(true);

        boolean result =
                transgenicLineService.delete(id);

        assertThat(result).isTrue();

        verify(transgenicLineRepository)
                .deleteById(id);
    }

    @Test
    void shouldNotDeleteMissingTransgenicLine() {

        final Long id = 10L;

        when(transgenicLineRepository.existsById(id))
                .thenReturn(false);

        boolean result =
                transgenicLineService.delete(id);

        assertThat(result).isFalse();

        verify(transgenicLineRepository, never())
                .deleteById(id);
    }
}
