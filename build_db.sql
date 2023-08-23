create extension if not exists "pg_trgm";


create table if not exists pessoas (
                                       id uuid NOT NULL,
                                       apelido varchar(32) not null unique,
                                       nome varchar(100) not null,
                                       nascimento date not null,
                                       stack varchar(32)[],
                                       search VARCHAR(2000)
);

CREATE INDEX idx_search ON pessoas USING gin (search gin_trgm_ops);

CREATE OR REPLACE FUNCTION generate_search_value(nome varchar, apelido varchar, stack varchar[])
    RETURNS VARCHAR
    IMMUTABLE
    LANGUAGE SQL
AS $$
SELECT
                LOWER(nome) || '-' || LOWER(apelido) ||
                CASE
                    WHEN stack IS NULL THEN ''
                    ELSE '-' || array_to_string(stack, '-')
                    END
$$;

CREATE OR REPLACE FUNCTION update_search_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.search = generate_search_value(NEW.nome, NEW.apelido, NEW.stack);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_search_on_insert
    BEFORE INSERT ON pessoas
    FOR EACH ROW
EXECUTE FUNCTION update_search_column();