DROP TABLE IF EXISTS articles CASCADE;

CREATE TABLE articles(
    article_id bigint,    -- identifier of article
    text       text       -- all text in the article
  );

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


DROP TABLE IF EXISTS org_mentions CASCADE;
CREATE TABLE org_mentions(
  sentence_id text,
  start_position int,
  length int,
  text text,
  mention_id text  -- unique identifier for people_mentions
  );


DROP TABLE IF EXISTS has_relation CASCADE;
CREATE TABLE has_relation(
  org1_id text,
  org2_id text,
  sentence_id text,
  description text,
  is_true boolean,
  relation_id text, -- unique identifier for has_spouse
  id bigint   -- reserved for DeepDive
  );

DROP TABLE IF EXISTS has_relation_features CASCADE;
CREATE TABLE has_relation_features(
  relation_id text,
  feature text);
