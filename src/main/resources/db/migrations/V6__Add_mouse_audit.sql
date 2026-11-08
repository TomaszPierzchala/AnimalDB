CREATE OR REPLACE FUNCTION audit_mouse_changes()
RETURNS TRIGGER AS $$
DECLARE
    col_name TEXT;
    column_label TEXT;
    old_data JSONB;
    new_data JSONB;
    old_value TEXT;
    new_value TEXT;
    log_lines TEXT := '';
BEGIN
    old_data := to_jsonb(OLD);
    new_data := to_jsonb(NEW);

    FOR col_name IN
        SELECT key
        FROM jsonb_each(new_data)
        WHERE key NOT IN ('id', 'note')
        ORDER BY key
    LOOP
        IF old_data -> col_name IS DISTINCT FROM new_data -> col_name THEN

            column_label := CASE col_name
                WHEN 'strain_id' THEN 'Strain'
                WHEN 'mother_id' THEN 'Mother Animal Number'
                WHEN 'father_id' THEN 'Father Animal Number'
                ELSE INITCAP(REPLACE(col_name, '_', ' '))
            END;

            CASE col_name

                WHEN 'strain_id' THEN
                    SELECT quote_literal(code || ' — ' || name)
                    INTO old_value
                    FROM strain
                    WHERE id = OLD.strain_id;

                    SELECT quote_literal(code || ' — ' || name)
                    INTO new_value
                    FROM strain
                    WHERE id = NEW.strain_id;

                WHEN 'mother_id' THEN
                    SELECT animal_number::TEXT
                    INTO old_value
                    FROM mouse
                    WHERE id = OLD.mother_id;

                    SELECT animal_number::TEXT
                    INTO new_value
                    FROM mouse
                    WHERE id = NEW.mother_id;

                WHEN 'father_id' THEN
                    SELECT animal_number::TEXT
                    INTO old_value
                    FROM mouse
                    WHERE id = OLD.father_id;

                    SELECT animal_number::TEXT
                    INTO new_value
                    FROM mouse
                    WHERE id = NEW.father_id;

                ELSE
                    old_value := old_data ->> col_name;
                    new_value := new_data ->> col_name;

            END CASE;

            old_value := COALESCE(old_value, 'NULL');
            new_value := COALESCE(new_value, 'NULL');

            log_lines := log_lines
                || CASE
                    WHEN log_lines = '' THEN ''
                    ELSE E'\n'
                   END
                || CURRENT_DATE::TEXT
                || ' column ' || column_label
                || ' value changed from '
                || old_value
                || ' to '
                || new_value;

        END IF;
    END LOOP;

    IF log_lines <> '' THEN
        NEW.note := CONCAT_WS(
            E'\n',
            NULLIF(NEW.note, ''),
            log_lines
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_mouse_changes
    BEFORE UPDATE ON mouse
    FOR EACH ROW
    EXECUTE FUNCTION audit_mouse_changes();