ALTER TABLE transgenic_line_gene
DROP CONSTRAINT transgenic_line_gene_gene_id_fkey;

ALTER TABLE transgenic_line_gene
ADD CONSTRAINT transgenic_line_gene_gene_id_fkey
FOREIGN KEY (gene_id)
REFERENCES gene(id)
ON DELETE CASCADE;


ALTER TABLE lab_procedure_technician
DROP CONSTRAINT lab_procedure_technician_person_id_fkey;

ALTER TABLE lab_procedure_technician
ADD CONSTRAINT lab_procedure_technician_person_id_fkey
FOREIGN KEY (person_id)
REFERENCES person(id)
ON DELETE CASCADE;


ALTER TABLE mouse_gene
DROP CONSTRAINT mouse_gene_gene_id_fkey;

ALTER TABLE mouse_gene
ADD CONSTRAINT mouse_gene_gene_id_fkey
FOREIGN KEY (gene_id)
REFERENCES gene(id)
ON DELETE CASCADE;