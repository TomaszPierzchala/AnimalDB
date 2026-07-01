CREATE TABLE strain (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE mouse (
    id BIGSERIAL PRIMARY KEY,
    strain_id BIGINT NOT NULL REFERENCES strain(id),

    animal_no INTEGER NOT NULL,
    sex VARCHAR(1) NOT NULL CHECK (sex IN ('M', 'F')),

    birth_date DATE,
    death_date DATE,

    procedure_name VARCHAR(100),
    location VARCHAR(50),
    rack VARCHAR(50),
    cage VARCHAR(50),
    origin VARCHAR(100),

    mother_id BIGINT REFERENCES mouse(id),
    father_id BIGINT REFERENCES mouse(id),

    note TEXT,

    transgenic_line VARCHAR(100),
    gene VARCHAR(100),
    genotype VARCHAR(100),

    UNIQUE (animal_no)
);
