-- Read-only. Lists customers whose CPF would be rejected by the API's validation (roadmap 3.4).
-- Such customers can still be viewed, but saving an edit fails until the CPF is corrected.
-- (E-mail is optional and not format-checked since V2__customer_email_optional.)
--
-- Usage, on the database server:  sudo -u postgres psql -d petshop -f check-customer-data.sql
-- Mirrors com.petshop.api.common.validation.CpfValidator.

WITH c AS (
    SELECT id, name, cpf,
           regexp_replace(cpf, '\D', '', 'g') AS d
    FROM customer
),
checked AS (
    SELECT *,
        CASE
            WHEN cpf !~ '^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$' THEN 'formato'
            WHEN d ~ '^(\d)\1{10}$'                     THEN 'dígitos repetidos'
            WHEN ((SELECT sum(substr(d, i, 1)::int * (11 - i)) FROM generate_series(1, 9) i) * 10 % 11) % 10
                 <> substr(d, 10, 1)::int                THEN 'dígito verificador'
            WHEN ((SELECT sum(substr(d, i, 1)::int * (12 - i)) FROM generate_series(1, 10) i) * 10 % 11) % 10
                 <> substr(d, 11, 1)::int                THEN 'dígito verificador'
        END AS cpf_problem
    FROM c
)
SELECT id, name, cpf, cpf_problem
FROM checked
WHERE cpf_problem IS NOT NULL
ORDER BY id;

SELECT count(*) AS total_customers FROM customer;
