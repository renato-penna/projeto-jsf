CREATE TABLE estados (
  id  bigint NOT NULL,
  nome varchar(45) NOT NULL,
  sigla varchar(2) NOT NULL,
  PRIMARY KEY (id,sigla)
);

CREATE TABLE cidades (
  id bigint NOT NULL,
  nome varchar(100) NOT NULL,
  estados_id bigint NOT NULL,
  PRIMARY KEY (id)
) 

