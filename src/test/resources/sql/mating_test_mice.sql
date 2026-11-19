INSERT INTO strain (id, code, name)
VALUES (1, 'STR', 'Strain Name');

INSERT INTO strain_transgenic_line (id, strain_id, name)
VALUES (2, 1, 'ABC12');

INSERT INTO mouse (animal_number, sex, strain_id, strain_transgenic_line_id)
SELECT 1001, 'F', strain_id, id
FROM strain_transgenic_line
ORDER BY id
    LIMIT 1;

INSERT INTO mouse (animal_number, sex, strain_id, strain_transgenic_line_id)
SELECT 1002, 'M', strain_id, id
FROM strain_transgenic_line
ORDER BY id
    LIMIT 1;