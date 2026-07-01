CREATE TABLE mating (
    id BIGSERIAL PRIMARY KEY,
    strain_id BIGINT NOT NULL REFERENCES strain(id),

    female_id BIGINT REFERENCES mouse(id),
    male_id BIGINT REFERENCES mouse(id),

    mating_no INTEGER,
    mating_date DATE,
    plug_date DATE,
    pregnancy_date DATE,
    parturition_date DATE,
    weaning_date DATE,

    birth_pups_count INTEGER,
    female_pups_count INTEGER,
    male_pups_count INTEGER,
    total_pups_count INTEGER,

    cage_label VARCHAR(50),
    comments TEXT
);
