
CREATE DATABASE rdf;
USE rdf;

CREATE TABLE rdf3 (
  subj varchar(255) NOT NULL default '',
  pred varchar(255) NOT NULL default '',
  obj text NOT NULL,
  type tinyint(4) NOT NULL default '0',
  source int(11) NOT NULL default '0',
  PRIMARY KEY  (subj(120),pred(120),obj(120),source),
  KEY source (source),
  KEY subj (subj),
  KEY obj (obj(100)),
  KEY pred (pred),
  KEY subjpred (subj(117),pred(117)),
  KEY predobj (pred(117),obj(117))
) TYPE=MyISAM;

CREATE TABLE urlsnew (
  url text NOT NULL,
  localcache text NOT NULL,
  stamp timestamp(14) NOT NULL,
  status tinyint(4) NOT NULL default '0',
  error text NOT NULL,
  triples int(11) NOT NULL default '0',
  ref int(11) NOT NULL auto_increment,
  PRIMARY KEY  (url(255)),
  UNIQUE KEY ref (ref)
) TYPE=MyISAM;

CREATE TABLE deletetable (
  id int(11) NOT NULL default '0'
) TYPE=MyISAM;

CREATE TABLE provenance (
  id int(11) NOT NULL default '0',
  txt varchar(255) NOT NULL default '',
  PRIMARY KEY  (id,txt)
) TYPE=MyISAM;

CREATE TABLE robots (
  domain text NOT NULL,
  localcache text NOT NULL,
  stamp timestamp(14) NOT NULL,
  ok tinyint(4) NOT NULL default '0'
) TYPE=MyISAM;


CREATE TABLE rdf3b (
  subj varchar(255) NOT NULL default '',
  pred varchar(255) NOT NULL default '',
  obj text NOT NULL,
  type tinyint(4) NOT NULL default '0',
  source int(11) NOT NULL default '0',
  PRIMARY KEY  (subj(120),pred(120),obj(120),source),
  KEY source (source),
  KEY subj (subj),
  KEY obj (obj(100)),
  KEY pred (pred),
  KEY subjpred (subj(117),pred(117)),
  KEY predobj (pred(117),obj(117))
) TYPE=MyISAM;