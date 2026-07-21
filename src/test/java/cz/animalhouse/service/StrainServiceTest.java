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

import cz.animalhouse.dto.StrainCreateRequest;
import cz.animalhouse.dto.StrainResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.exception.DuplicateStrainCodeException;
import cz.animalhouse.repository.StrainRepository;

@ExtendWith(MockitoExtension.class)
class StrainServiceTest {

    @Mock
    private StrainRepository strainRepository;

    private StrainService strainService;

    @BeforeEach
    void setUp() {
        strainService = new StrainService(strainRepository);
    }

    @Test
    void shouldFindAllStrains() {
        Strain strain1 = new Strain(
                "C57BL6",
                "C57BL/6"
        );

        Strain strain2 = new Strain(
                "BALBC",
                "BALB/c"
        );


        when(strainRepository.findAll())
                .thenReturn(List.of(strain1, strain2));

        List<StrainResponse> result = strainService.findAll();

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(StrainResponse::code)
                .containsExactly("C57BL6", "BALBC");

        assertThat(result)
                .extracting(StrainResponse::name)
                .containsExactly("C57BL/6", "BALB/c");

        verify(strainRepository).findAll();
    }

    @Test
    void shouldCreateStrain() {
        StrainCreateRequest request = new StrainCreateRequest(
                "C57BL6",
                "C57BL/6"
        );

        when(strainRepository.existsByCode("C57BL6"))
                .thenReturn(false);

        when(strainRepository.save(any(Strain.class)))
                .thenAnswer(invocation -> {
                    Strain strain = invocation.getArgument(0);
                    return strain;
                });

        StrainResponse result = strainService.create(request);

        assertThat(result.code()).isEqualTo("C57BL6");
        assertThat(result.name()).isEqualTo("C57BL/6");

        verify(strainRepository).existsByCode("C57BL6");
        verify(strainRepository).save(any(Strain.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingStrainWithDuplicateCode() {
        StrainCreateRequest request = new StrainCreateRequest(
                "C57BL6",
                "C57BL/6"
        );

        when(strainRepository.existsByCode("C57BL6"))
                .thenReturn(true);

        assertThatThrownBy(() -> strainService.create(request))
                .isInstanceOf(DuplicateStrainCodeException.class)
                .hasMessageContaining("C57BL6");

        verify(strainRepository).existsByCode("C57BL6");

        verify(strainRepository, never())
                .save(any(Strain.class));
    }

    @Test
    void shouldDeleteExistingStrain() {
        Long id = 1L;

        when(strainRepository.existsById(id))
                .thenReturn(true);

        boolean result = strainService.delete(id);

        assertThat(result).isTrue();

        verify(strainRepository).existsById(id);
        verify(strainRepository).deleteById(id);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingStrain() {
        Long id = 1L;

        when(strainRepository.existsById(id))
                .thenReturn(false);

        boolean result = strainService.delete(id);

        assertThat(result).isFalse();

        verify(strainRepository).existsById(id);

        verify(strainRepository, never())
                .deleteById(id);
    }

    @Test
    void shouldUpdateExistingStrain() {
        Long id = 1L;

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6"
        );

        StrainCreateRequest request = new StrainCreateRequest(
                "C57BL6J",
                "C57BL/6J"
        );

        when(strainRepository.findById(id))
                .thenReturn(Optional.of(strain));

        when(strainRepository.existsByCodeAndIdNot(
                "C57BL6J",
                id))
                .thenReturn(false);

        Optional<StrainResponse> result =
                strainService.update(id, request);

        assertThat(result).isPresent();

        StrainResponse response = result.orElseThrow();

        assertThat(response.code()).isEqualTo("C57BL6J");
        assertThat(response.name()).isEqualTo("C57BL/6J");

        assertThat(strain.getCode()).isEqualTo("C57BL6J");
        assertThat(strain.getName()).isEqualTo("C57BL/6J");

        verify(strainRepository).findById(id);

        verify(strainRepository)
                .existsByCodeAndIdNot("C57BL6J", id);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingStrain() {
        Long id = 999L;

        StrainCreateRequest request = new StrainCreateRequest(
                "C57BL6J",
                "C57BL/6J"
        );

        when(strainRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<StrainResponse> result =
                strainService.update(id, request);

        assertThat(result).isEmpty();

        verify(strainRepository).findById(id);

        verify(strainRepository, never())
                .existsByCodeAndIdNot(
                        request.getCode(),
                        id);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStrainWithDuplicateCode() {
        Long id = 1L;

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6"
        );

        StrainCreateRequest request = new StrainCreateRequest(
                "BALBC",
                "BALB/c"
        );

        when(strainRepository.findById(id))
                .thenReturn(Optional.of(strain));

        when(strainRepository.existsByCodeAndIdNot(
                "BALBC",
                id))
                .thenReturn(true);

        assertThatThrownBy(() ->
                strainService.update(id, request))
                .isInstanceOf(DuplicateStrainCodeException.class)
                .hasMessageContaining("BALBC");

        assertThat(strain.getCode()).isEqualTo("C57BL6");
        assertThat(strain.getName()).isEqualTo("C57BL/6");

        verify(strainRepository).findById(id);

        verify(strainRepository)
                .existsByCodeAndIdNot("BALBC", id);
    }
}
