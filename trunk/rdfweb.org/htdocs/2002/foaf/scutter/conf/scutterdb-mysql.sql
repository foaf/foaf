
DROP DATABASE rdfweb1;
CREATE DATABASE rdfweb1;
use rdfweb1;

CREATE TABLE triples (
  subject int(10) DEFAULT '0' NOT NULL,
  predicate int(10) DEFAULT '0' NOT NULL,
  object int(10) DEFAULT '0' NOT NULL,
  assertid text,
  personid text,
  isresource bool
);

# note: 'keyhash' was called 'key' in postgres.
#
CREATE TABLE resources (
  keyhash int(10) unsigned DEFAULT '0' NOT NULL,
  value text
);

# how to recreate these in mysql?
# 

#CREATE INDEX sub_index ON triples USING btree (subject);
#CREATE INDEX pred_index ON triples USING btree (predicate);
#CREATE INDEX obj_index ON triples USING btree (object);
#CREATE UNIQUE INDEX resources_key_key ON resources USING btree ("key");
#CREATE INDEX res_key_index ON resources USING btree ("key");
#CREATE INDEX res_val_index ON resources USING btree (value);

