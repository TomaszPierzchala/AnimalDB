
-- Initial data for AnimalDB

-- Genes

INSERT INTO public.gene (id, symbol, description)
VALUES (1, 'GFP', 'Green Fluorescent Protein');

INSERT INTO public.gene (id, symbol, description)
VALUES (2, 'MELL1', 'Metabolically efficient and long-lived One');

INSERT INTO public.gene (id, symbol, description)
VALUES (3, 'MELL7', 'Metabolically efficient and long-lived Seven');


-- Strains

INSERT INTO public.strain (id, code, name)
VALUES (1, 'STR', 'Strain Name');

INSERT INTO public.strain (id, code, name)
VALUES (2, 'LONG LIVE', '100');


-- Transgenic lines

INSERT INTO public.transgenic_line (id, strain_id, name)
VALUES (1, 1, 'PRG1');

INSERT INTO public.transgenic_line (id, strain_id, name)
VALUES (2, 2, 'PRG2');


-- Transgenic line genes

INSERT INTO public.transgenic_line_gene (transgenic_line_id, gene_id)
VALUES (1, 1);

INSERT INTO public.transgenic_line_gene (transgenic_line_id, gene_id)
VALUES (2, 2);

INSERT INTO public.transgenic_line_gene (transgenic_line_id, gene_id)
VALUES (2, 3);


-- Synchronize sequences after inserting explicit IDs

SELECT pg_catalog.setval('public.gene_id_seq', 3, true);

SELECT pg_catalog.setval('public.strain_id_seq', 2, true);

SELECT pg_catalog.setval('public.transgenic_line_id_seq', 2, true);

SELECT pg_catalog.setval('public.mouse_id_seq', 1, true);
