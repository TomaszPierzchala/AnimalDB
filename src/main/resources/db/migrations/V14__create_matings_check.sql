-------  UNIQUE FEMALE FOR AN ACTIVE MATING  -------
CREATE UNIQUE INDEX uq_mating_active_female
    ON mating (female_id)
    WHERE end_mating_date IS NULL;


----- ACTIVE MATING CAN HAVE ONLY UP TO TWO MALES -----
CREATE OR REPLACE FUNCTION check_active_male_matings()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_mating_date IS NULL THEN

        -- Serialize concurrent changes for the same male
        PERFORM 1
        FROM mouse
        WHERE id = NEW.male_id
        FOR UPDATE;

IF (
SELECT COUNT(*)
  FROM mating
 WHERE male_id = NEW.male_id
   AND end_mating_date IS NULL
   AND id <> NEW.id
     ) >= 2 THEN
            RAISE EXCEPTION
                'Male mouse % already has two active matings',
                NEW.male_id;
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_active_male_matings
    BEFORE INSERT OR UPDATE OF male_id, end_mating_date
                         ON mating
                             FOR EACH ROW
                             EXECUTE FUNCTION check_active_male_matings();


-----  CHECKS VALID SEX OF MATING'S MALE & FEMALE  -----
CREATE OR REPLACE FUNCTION validate_mating_parents_sex()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM mouse
        WHERE id = NEW.female_id
          AND sex = 'F'
    ) THEN
        RAISE EXCEPTION
            'Mouse with id % must exists and be female (F)',
            NEW.female_id;
END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM mouse
        WHERE id = NEW.male_id
          AND sex = 'M'
    ) THEN
        RAISE EXCEPTION
            'Mouse with id % must exists and be male (M)',
            NEW.male_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_mating_parents_sex
    BEFORE INSERT OR UPDATE OF female_id, male_id
                         ON mating
                             FOR EACH ROW
                             EXECUTE FUNCTION validate_mating_parents_sex();