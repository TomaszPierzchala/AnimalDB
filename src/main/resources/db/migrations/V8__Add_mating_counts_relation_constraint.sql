
ALTER TABLE mating
    ADD
        CONSTRAINT chk_mating_counts CHECK (
            -- Non-negative counts
            (born_count IS NULL OR born_count >= 0)
                AND (alive_count IS NULL OR alive_count >= 0)
                AND (female_count IS NULL OR female_count >= 0)
                AND (male_count IS NULL OR male_count >= 0)

                -- Alive cannot exceed born
                AND (alive_count IS NULL OR born_count IS NULL
                OR alive_count <= born_count)

                -- Female and male counts cannot exceed alive
                AND (female_count IS NULL OR alive_count IS NULL
                OR female_count <= alive_count)
                AND (male_count IS NULL OR alive_count IS NULL
                OR male_count <= alive_count)
                AND (female_count IS NULL OR male_count IS NULL
                OR alive_count IS NULL
                OR female_count + male_count <= alive_count)

                -- Female and male counts cannot exceed born
                AND (female_count IS NULL OR born_count IS NULL
                OR female_count <= born_count)
                AND (male_count IS NULL OR born_count IS NULL
                OR male_count <= born_count)
                AND (female_count IS NULL OR male_count IS NULL
                OR born_count IS NULL
                OR female_count + male_count <= born_count)
            );
