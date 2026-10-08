-- Many petshop customers have no e-mail: it becomes optional (2026-10-05).
-- CPF stays required and is validated by the API.
ALTER TABLE customer ALTER COLUMN email DROP NOT NULL;
