
DROP DATABASE rdfweb1;
CREATE DATABASE rdfweb1;
use rdfweb1;

# updated version w/ edits from Jim

# note: 'keyhash' was called 'key' in postgres.
#

CREATE TABLE resources (
  keyhash int(10) PRIMARY KEY NOT NULL default '0',
  value text not NULL
);
CREATE TABLE triples (
  subject int(10) NOT NULL  default '0',
  predicate int(10)  NOT NULL  default '0',
  object int(10)  NOT NULL default '0',
  assertid text NOT NULL,
  personid text NOT NULL,
  isresource tinyint(1) NOT NULL default '0'
);

ALTER TABLE triples ADD KEY (subject);
ALTER TABLE triples ADD KEY (predicate);
ALTER TABLE triples ADD KEY (object);

