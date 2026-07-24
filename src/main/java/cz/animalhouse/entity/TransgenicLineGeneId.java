package cz.animalhouse.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record TransgenicLineGeneId(

    @Column(name = "transgenic_line_id")
    Long transgenicLineId,

    @Column(name = "gene_id")
    Long geneId

) implements Serializable {

	private static final long serialVersionUID = 1L;
}