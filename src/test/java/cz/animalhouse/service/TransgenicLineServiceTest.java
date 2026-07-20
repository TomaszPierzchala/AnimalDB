package cz.animalhouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        Long strainId = 1L;

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OT-I");

        verify(strainRepository, never())
                .findById(1L);

        verify(transgenicLineRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldReturnEmptyWhenUpdatedRecordDoesNotExist() {

        Long id = 10L;

        TransgenicLineRequest request =
                new TransgenicLineRequest(
                        1L,
                        "OT-I");

        when(transgenicLineRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<TransgenicLineResponse> result =
                transgenicLineService.update(id, request);

        assertThat(result).isEmpty();

        verify(strainRepository, never())
                .findById(1L);
    }

    @Test
    void shouldDeleteExistingTransgenicLine() {

        Long id = 10L;

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

        Long id = 10L;

        when(transgenicLineRepository.existsById(id))
                .thenReturn(false);

        boolean result =
                transgenicLineService.delete(id);

        assertThat(result).isFalse();

        verify(transgenicLineRepository, never())
                .deleteById(id);
    }
}
