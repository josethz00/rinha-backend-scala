CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table if not exists pessoas (
    id uuid DEFAULT uuid_generate_v4 (),
    apelido varchar(32) not null,
    nome varchar(100) not null,
    nascimento date not null,
    stack varchar(32)
);