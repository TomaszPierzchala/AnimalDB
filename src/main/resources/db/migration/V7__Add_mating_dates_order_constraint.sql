
-- Validate chronological order of all non-null dates.
-- Existing CHECK constraints are intentionally preserved.

ALTER TABLE mating
    ADD CONSTRAINT chk_mating_dates_order
        CHECK (
            (mating_date IS NULL OR plug_date IS NULL OR mating_date < plug_date)
                AND (mating_date IS NULL OR pregnancy_date IS NULL OR mating_date < pregnancy_date)
                AND (mating_date IS NULL OR birth_date IS NULL OR mating_date < birth_date)
                AND (mating_date IS NULL OR weaning_date IS NULL OR mating_date < weaning_date)
                AND (plug_date IS NULL OR pregnancy_date IS NULL OR plug_date < pregnancy_date)
                AND (plug_date IS NULL OR birth_date IS NULL OR plug_date < birth_date)
                AND (plug_date IS NULL OR weaning_date IS NULL OR plug_date < weaning_date)
                AND (pregnancy_date IS NULL OR birth_date IS NULL OR pregnancy_date < birth_date)
                AND (pregnancy_date IS NULL OR weaning_date IS NULL OR pregnancy_date < weaning_date)
                AND (birth_date IS NULL OR weaning_date IS NULL OR birth_date < weaning_date)
            );
