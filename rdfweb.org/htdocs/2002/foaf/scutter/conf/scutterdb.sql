-- create basic tables needed by rubyrdf
-- usage: psql rdfweb1 < conf/scutterdb.sql


CREATE TABLE "triples" (
	"subject" integer,
	"predicate" integer,
	"object" integer,
	"assertid" character varying,
	"personid" character varying,
	"isresource" boolean
);

--
-- TOC Entry ID 3 (OID 31578)
--
-- Name: resources Type: TABLE Owner: pgsql
--

CREATE TABLE "resources" (
	"key" integer,
	"value" character varying
);

--
-- TOC Entry ID 4 (OID 16569)
--
-- Name: "sub_index" Type: INDEX Owner: pgsql
--

CREATE INDEX sub_index ON triples USING btree (subject);

--
-- TOC Entry ID 5 (OID 16570)
--
-- Name: "pred_index" Type: INDEX Owner: pgsql
--

CREATE INDEX pred_index ON triples USING btree (predicate);

--
-- TOC Entry ID 6 (OID 16571)
--
-- Name: "obj_index" Type: INDEX Owner: pgsql
--

CREATE INDEX obj_index ON triples USING btree (object);

--
-- TOC Entry ID 7 (OID 31583)
--
-- Name: "resources_key_key" Type: INDEX Owner: pgsql
--

CREATE UNIQUE INDEX resources_key_key ON resources USING btree ("key");

--
-- TOC Entry ID 8 (OID 31584)
--
-- Name: "res_key_index" Type: INDEX Owner: pgsql
--

CREATE INDEX res_key_index ON resources USING btree ("key");

--
-- TOC Entry ID 9 (OID 31585)
--
-- Name: "res_val_index" Type: INDEX Owner: pgsql
--

CREATE INDEX res_val_index ON resources USING btree (value);

