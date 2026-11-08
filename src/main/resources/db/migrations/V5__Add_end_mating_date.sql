-- Add mating closure date.
ALTER TABLE mating
    ADD COLUMN end_mating_date DATE;

COMMENT ON COLUMN mating.end_mating_date IS
    'Date when the mating was closed. NULL indicates an active mating; '
    'a non-NULL value indicates a closed (inactive) mating.';


-- Automatically close mating when the weaning date is set.
CREATE OR REPLACE FUNCTION set_end_mating_date_on_weaning()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.weaning_date IS NOT NULL
       AND (
           TG_OP = 'INSERT'
           OR NEW.weaning_date IS DISTINCT FROM OLD.weaning_date
       ) THEN

        NEW.end_mating_date := NEW.weaning_date;

END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_set_end_mating_date_on_weaning
    BEFORE INSERT OR UPDATE OF weaning_date ON mating
    FOR EACH ROW
    EXECUTE FUNCTION set_end_mating_date_on_weaning();