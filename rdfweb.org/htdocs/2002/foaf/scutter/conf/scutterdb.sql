-- create basic tables needed by rubyrdf
-- usage: psql rdfweb1 < conf/scutterdb.sql
-- do 'drop table ...' to destroy stored data.

CREATE TABLE "triples" (
	"subject" integer,
	"predicate" integer,
	"object" integer,
	"assertid" character varying,
	"personid" character varying,
	"isresource" boolean
);

CREATE TABLE "resources" (
	"keyhash" integer,
	"value" character varying );

CREATE INDEX sub_index ON triples USING btree (subject);
CREATE INDEX pred_index ON triples USING btree (predicate);
CREATE INDEX obj_index ON triples USING btree (object);
CREATE UNIQUE INDEX resources_key_key ON resources USING btree ("keyhash");
CREATE INDEX res_key_index ON resources USING btree ("keyhash");
CREATE INDEX res_val_index ON resources USING btree (value);

