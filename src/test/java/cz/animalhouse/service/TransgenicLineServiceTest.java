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

import cz.animalhouse.dto.TransgenicLineRequest;
import cz.animalhouse.dto.TransgenicLineResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.TransgenicLine;
import cz.animalhouse.exception.DuplicateTransgenicLineNameException;
import cz.animalhouse.exception.StrainNotFoundException;
import cz.animalhouse.repository.StrainRepository;
import cz.animalhouse.repository.TransgenicLineRepository;

@ExtendWith(MockitoExtension.class)
class TransgenicLineServiceTest {

    @Mock
    private TransgenicLineRepository transgenicLineRepository;

    @Mock
    private StrainRepository strainRepository;

    private TransgenicLineService transgenicLineService;

    @BeforeEach
    void setUp() {
        transgenicLineService = new TransgenicLineService(
                transgenicLineRepository,
                strainRepository);
    }

    @Test
    void shouldFindAllTransgenicLines() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        TransgenicLine line1 =
                new TransgenicLine(strain, "OT-I");

        TransgenicLine line2 =
                new TransgenicLine(strain, "OT-II");

        when(transgenicLineRepository.findAll())
                .thenReturn(List.of(line1, line2));

        List<TransgenicLineResponse> result =
                transgenicLineService.findAll();

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(TransgenicLineResponse::name)
                .containsExactly("OT-I", "OT-II");
    }

    @Test
    void shouldCreateTransgenicLine() {

        final Long strainId = 1L;

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        TransgenicLineRequest request =
                new TransgenicLineRequest(
                        strainId,
                        "OT-I");

        when(transgenicLineRepository.existsByName("OT-I"))
                .thenReturn(false);

        when(strainRepository.findById(strainId))
                .thenReturn(Optional.of(strain));

        when(transgenicLineRepository.save(
                org.mockito.ArgumentMatchers.any(
                        TransgenicLine.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        TransgenicLineResponse result =
                transgenicLineService.create(request);

        assertThat(result.name()).isEqualTo("OT-I");
        assertThat(result.strainCode()).isEqualTo("C57BL6");

        verify(transgenicLineRepository)
                .save(org.mockito.ArgumentMatchers.any(
                        TransgenicLine.class));
    }

    @Test
    void shouldRejectDuplicateNameDuringCreate() {

        TransgenicLineRequest request =
                new TransgenicLineRequest(
                        1L,
                        "OT-I");

        when(transgenicLineRepository.existsByName("OT-I"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                transgenicLineService.create(request))
                .isInstanceOf(DuplicateTransgenicLineNameException.class)
                .hasMessageContaining("Transgenic line with name 'OT-I' already exists");

        verify(strainRepository, never())
                .findById(1L);

        verify(transgenicLineRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingTransgenicLineForMissingStrain() {
        final Long strainId = 999L;

        TransgenicLineRequest request =
                new TransgenicLineRequest(
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
                .save(any(TransgenicLine.class));
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

        TransgenicLine transgenicLine = new TransgenicLine(
                oldStrain,
                "OT-I"
        );

        TransgenicLineRequest request =
                new TransgenicLineRequest(
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


        Optional<TransgenicLineResponse> result =
                transgenicLineService.update(id, request);


        assertThat(result).isPresent();

        TransgenicLineResponse response = result.orElseThrow();

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

        TransgenicLineRequest request =
                new TransgenicLineRequest(
                        strainId,
                        "OT-I");

        when(transgenicLineRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<TransgenicLineResponse> result =
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

        TransgenicLine transgenicLine = new TransgenicLine(
                oldStrain,
                "OT-I"
        );

        TransgenicLineRequest request =
                new TransgenicLineRequest(
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
                        DuplicateTransgenicLineNameException.class)
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
