CREATE SEQUENCE seq_nodes;
CREATE SEQUENCE seq_triples;
CREATE SEQUENCE seq_namespaces;

CREATE TYPE nodetype AS ENUM ('uri','bnode','string','int','double','date','boolean');

CREATE TABLE nodes (
  id        bigint     NOT NULL,
  ntype     nodetype   NOT NULL,
  svalue    text       NOT NULL,
  dvalue    double precision,
  ivalue    bigint,
  tvalue    timestamp,
  bvalue    boolean,
  ltype     bigint     REFERENCES nodes(id),
  lang      varchar(5),
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

CREATE TABLE triples (
  id        bigint     NOT NULL,
  subject   bigint     NOT NULL REFERENCES nodes(id),
  predicate bigint     NOT NULL REFERENCES nodes(id),
  object    bigint     NOT NULL REFERENCES nodes(id),
  context   bigint     NOT NULL REFERENCES nodes(id),
  creator   bigint     REFERENCES nodes(id),
  inferred  boolean    DEFAULT false,
  deleted   boolean    DEFAULT false,
  createdAt timestamp  NOT NULL DEFAULT now(),
  deletedAt timestamp,
  PRIMARY KEY(id),
  CHECK ( (deleted AND deletedAt IS NOT NULL) OR ((NOT deleted) AND deletedAt IS NULL) )
);

CREATE TABLE namespaces (
  id        bigint        NOT NULL,
  prefix    varchar(256)  NOT NULL,
  uri       varchar(2048) NOT NULL,
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

-- A table for storing metadata about the current database, e.g. version numbers for each table
CREATE TABLE metadata (
  id        serial        NOT NULL,
  mkey      varchar(16)   NOT NULL,
  mvalue    varchar(256)  NOT NULL,
  PRIMARY KEY(id)
);

-- Indexes for accessing nodes and triples efficiently
CREATE INDEX idx_node_content ON nodes USING hash(svalue);
CREATE INDEX idx_literal_lang ON nodes(lang) WHERE ntype = 'string';

CREATE INDEX idx_triples_s ON triples(subject) WHERE deleted = false;
CREATE INDEX idx_triples_o ON triples(object) WHERE deleted = false;
CREATE INDEX idx_triples_sp ON triples(subject,predicate) WHERE deleted = false;
CREATE INDEX idx_triples_po ON triples(predicate,object) WHERE deleted = false;
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_triples_cs ON triples(context,subject) WHERE deleted = false;
CREATE INDEX idx_triples_csp ON triples(context,subject,predicate) WHERE deleted = false;
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object) WHERE deleted = false;

CREATE INDEX idx_namespaces_uri ON namespaces(uri);
CREATE INDEX idx_namespaces_prefix ON namespaces(prefix);


-- a function for cleaning up table rows without incoming references

-- insert initial metadata
INSERT INTO metadata(mkey,mvalue) VALUES ('version','1');
INSERT INTO metadata(mkey,mvalue) VALUES ('created',to_char(now(),'yyyy-MM-DD HH:mm:ss TZ') );