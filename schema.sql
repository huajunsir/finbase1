DROP TABLE IF EXISTS sentences CASCADE;
CREATE TABLE sentences(
  document_id text,
  sentence text,
  words text[],
  lemma text[],
  pos_tags text[],
  dependencies text[],
  ner_tags text[],
  sentence_offset bigint,
  sentence_id text -- unique identifier for sentences
  );


DROP TABLE IF EXISTS people_mentions CASCADE;
CREATE TABLE company_mentions(
  sentence_id text,
  start_position int,
  length int,
  text text,
  mention_id text  -- unique identifier for people_mentions
  );


DROP TABLE IF EXISTS has_spouse CASCADE;
CREATE TABLE buy(
  company1_id text,
  company2_id text,
  sentence_id text,
  description text,
  is_true boolean,
  relation_id text, -- unique identifier for has_spouse
  id bigint   -- reserved for DeepDive
  );

DROP TABLE IF EXISTS has_spouse_features CASCADE;
CREATE TABLE buy_features(
  relation_id text,
  feature text);
