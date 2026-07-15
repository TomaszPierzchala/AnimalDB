package cz.animalhouse.dto;

import cz.animalhouse.entity.Strain;

public class StrainResponse {

    private Long id;
    private String code;
    private String name;

    public StrainResponse(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static StrainResponse fromEntity(Strain strain) {
        return new StrainResponse(
                strain.getId(),
                strain.getCode(),
                strain.getName());
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}