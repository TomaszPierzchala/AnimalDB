CREATE TABLE strain (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE transgenic_line (
    id BIGSERIAL PRIMARY KEY,
    strain_id BIGINT NOT NULL REFERENCES strain(id),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE gene (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE transgenic_line_gene (
    transgenic_line_id BIGINT NOT NULL REFERENCES transgenic_line(id) ON DELETE CASCADE,
    gene_id BIGINT NOT NULL REFERENCES gene(id),
    PRIMARY KEY (transgenic_line_id, gene_id)
);

CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);


CREATE TABLE lab_procedure (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200),
    description TEXT,
    director_id BIGINT NOT NULL REFERENCES person(id),
    start_date DATE,
    end_date DATE,
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

CREATE TABLE lab_procedure_technician (
    lab_procedure_id BIGINT NOT NULL
        REFERENCES lab_procedure(id) ON DELETE CASCADE,

    person_id BIGINT NOT NULL
        REFERENCES person(id),

    assigned_date DATE NOT NULL,
    note TEXT,

    PRIMARY KEY (lab_procedure_id, person_id)
);

CREATE TABLE mouse (
    id BIGSERIAL PRIMARY KEY,

    animal_number INTEGER NOT NULL UNIQUE,
    sex VARCHAR(1) NOT NULL CHECK (sex IN ('M', 'F')),

    strain_id BIGINT NOT NULL REFERENCES strain(id),
    transgenic_line_id BIGINT REFERENCES transgenic_line(id),
    lab_procedure_id BIGINT REFERENCES lab_procedure(id),

    mother_id BIGINT REFERENCES mouse(id),
    father_id BIGINT REFERENCES mouse(id),

    birth_date DATE,
    death_date DATE,
    CHECK (death_date IS NULL OR birth_date IS NULL OR death_date >= birth_date),

    room VARCHAR(50),
    rack VARCHAR(50),
    cage VARCHAR(50),
    origin VARCHAR(100),

    note TEXT
);

CREATE TABLE mouse_gene (
    mouse_id BIGINT NOT NULL REFERENCES mouse(id) ON DELETE CASCADE,
    gene_id BIGINT NOT NULL REFERENCES gene(id),

    genotype VARCHAR(10) NOT NULL CHECK (genotype IN ('WT', 'HETERO', 'HOMO', 'HEMI')),

    PRIMARY KEY (mouse_id, gene_id)
);

CREATE TABLE mating (
    id BIGSERIAL PRIMARY KEY,

    female_id BIGINT NOT NULL REFERENCES mouse(id),
    male_id BIGINT NOT NULL REFERENCES mouse(id),
    CHECK (female_id <> male_id),
	
    mating_date DATE,
    plug_date DATE,
    pregnancy_date DATE,
    birth_date DATE,
    weaning_date DATE,
    CHECK (mating_date IS NULL OR plug_date IS NULL OR mating_date < plug_date),
    CHECK (plug_date IS NULL OR pregnancy_date IS NULL OR plug_date < pregnancy_date),
    CHECK (pregnancy_date IS NULL OR birth_date IS NULL OR pregnancy_date < birth_date),
    CHECK (birth_date IS NULL OR weaning_date IS NULL OR birth_date < weaning_date),

    born_count INTEGER CHECK (born_count >= 0),
    alive_count INTEGER CHECK (alive_count >= 0),
    female_count INTEGER CHECK (female_count >= 0),
    male_count INTEGER CHECK (male_count >= 0),
    CHECK (alive_count IS NULL OR born_count IS NULL OR alive_count <= born_count),
    CHECK (female_count IS NULL OR alive_count IS NULL OR female_count <= alive_count),
    CHECK (male_count IS NULL OR alive_count IS NULL OR male_count <= alive_count),
    CHECK (
           female_count IS NULL
           OR male_count IS NULL
           OR alive_count IS NULL
           OR female_count + male_count <= alive_count
    ),

    note TEXT
);

COMMENT ON TABLE strain IS
'Mouse strain.';

COMMENT ON COLUMN strain.code IS
'Unique strain identifier used by the laboratory.';

COMMENT ON COLUMN strain.name IS
'Official strain name.';


COMMENT ON TABLE transgenic_line IS
'Transgenic line belonging to a mouse strain.';

COMMENT ON COLUMN transgenic_line.strain_id IS
'Parent mouse strain.';

COMMENT ON COLUMN transgenic_line.name IS
'Official transgenic line name.';


COMMENT ON TABLE gene IS
'Gene definition used for genotyping.';

COMMENT ON COLUMN gene.symbol IS
'Official gene symbol.';

COMMENT ON COLUMN gene.description IS
'Additional information about the gene.';


COMMENT ON TABLE transgenic_line_gene IS
'Genes defining a transgenic line.';

COMMENT ON COLUMN transgenic_line_gene.transgenic_line_id IS
'Referenced transgenic line.';

COMMENT ON COLUMN transgenic_line_gene.gene_id IS
'Gene belonging to the transgenic line.';


COMMENT ON TABLE person IS
'Laboratory staff member.';

COMMENT ON COLUMN person.name IS
'Full name of the staff member.';

COMMENT ON COLUMN person.description IS
'Additional information such as role or affiliation.';


COMMENT ON TABLE lab_procedure IS
'Research procedure or experimental protocol.';

COMMENT ON COLUMN lab_procedure.code IS
'Unique procedure identifier (e.g. P-32-2026).';

COMMENT ON COLUMN lab_procedure.name IS
'Procedure title.';

COMMENT ON COLUMN lab_procedure.description IS
'Detailed description of the procedure.';

COMMENT ON COLUMN lab_procedure.director_id IS
'Principal investigator responsible for the procedure.';

COMMENT ON COLUMN lab_procedure.start_date IS
'Procedure start date.';

COMMENT ON COLUMN lab_procedure.end_date IS
'Procedure completion date.';


COMMENT ON TABLE lab_procedure_technician IS
'Technicians assigned to a laboratory procedure.';

COMMENT ON COLUMN lab_procedure_technician.lab_procedure_id IS
'Referenced laboratory procedure.';

COMMENT ON COLUMN lab_procedure_technician.person_id IS
'Assigned laboratory technician.';


COMMENT ON TABLE mouse IS
'Laboratory mouse.';

COMMENT ON COLUMN mouse.animal_number IS
'Unique laboratory animal identification number.';

COMMENT ON COLUMN mouse.sex IS
'Biological sex (M = Male, F = Female).';

COMMENT ON COLUMN mouse.strain_id IS
'Mouse strain.';

COMMENT ON COLUMN mouse.transgenic_line_id IS
'Assigned transgenic line.';

COMMENT ON COLUMN mouse.lab_procedure_id IS
'Research procedure in which the mouse participates.';

COMMENT ON COLUMN mouse.mother_id IS
'Mother of the mouse.';

COMMENT ON COLUMN mouse.father_id IS
'Father of the mouse.';

COMMENT ON COLUMN mouse.birth_date IS
'Date of birth.';

COMMENT ON COLUMN mouse.death_date IS
'Date of death.';

COMMENT ON COLUMN mouse.room IS
'Animal facility room identifier.';

COMMENT ON COLUMN mouse.rack IS
'Rack identifier within the animal room.';

COMMENT ON COLUMN mouse.cage IS
'Cage identifier on the rack.';

COMMENT ON COLUMN mouse.origin IS
'Institution or supplier from which the mouse originated.';

COMMENT ON COLUMN mouse.note IS
'Additional remarks or observations.';


COMMENT ON TABLE mouse_gene IS
'Genotype of a mouse for a specific gene.';

COMMENT ON COLUMN mouse_gene.mouse_id IS
'Mouse to which the genotype belongs.';

COMMENT ON COLUMN mouse_gene.gene_id IS
'Genotyped gene.';

COMMENT ON COLUMN mouse_gene.genotype IS
'Observed genotype (WT, HETERO, HOMO or HEMI).';


COMMENT ON TABLE mating IS
'Breeding record for a mating pair.';

COMMENT ON COLUMN mating.female_id IS
'Female mouse participating in the mating.';

COMMENT ON COLUMN mating.male_id IS
'Male mouse participating in the mating.';

COMMENT ON COLUMN mating.mating_date IS
'Date when the animals were paired for mating.';

COMMENT ON COLUMN mating.plug_date IS
'Date when a vaginal plug was observed.';

COMMENT ON COLUMN mating.pregnancy_date IS
'Date when pregnancy was confirmed.';

COMMENT ON COLUMN mating.birth_date IS
'Date when the litter was born.';

COMMENT ON COLUMN mating.weaning_date IS
'Date when the litter was weaned.';

COMMENT ON COLUMN mating.born_count IS
'Total number of pups born.';

COMMENT ON COLUMN mating.alive_count IS
'Number of pups alive after birth.';

COMMENT ON COLUMN mating.female_count IS
'Number of female pups.';

COMMENT ON COLUMN mating.male_count IS
'Number of male pups.';

COMMENT ON COLUMN mating.note IS
'Additional remarks about the mating or litter.';