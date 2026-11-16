INSERT INTO strain (id, code, name)
VALUES (1, 'STR', 'Strain Name');

INSERT INTO mouse (animal_number, sex, strain_id)
SELECT 1001, 'F', id
FROM strain
ORDER BY id
    LIMIT 1;

INSERT INTO mouse (animal_number, sex, strain_id)
SELECT 1002, 'M', id
FROM strain
ORDER BY id
    LIMIT 1;