#! /usr/bin/env python

import csv, os, sys

APP_HOME = os.environ['FINBASE_HOME']

# Load the spouse dictionary for distant supervision.
# A person can have multiple spouses
has_equity_transactions = set()
related_companies = set()
lines = open(APP_HOME + '/data/labeled/jiaoyi.csv').readlines()
for line in lines:
  name1, name2, relation = line.strip().split('\t')
  has_equity_transactions.add((name1, name2))  # Add a spouse relation pair
  related_companies.add(name1)    # Record the person as married
  related_companies.add(name2)

# Load relations of people that are not spouse
# The non-spouse KB lists incompatible relations, e.g. childrens, siblings, parents.
#non_equity_transactions = set()
#lines = open(FINBASE_HOME + '/data/labeled/non-spouses.tsv').readlines()
#for line in lines:
 # name1, name2, relation = line.strip().split('\t')
  # non_equity_transactions.add((name1, name2))  # Add a non-spouse relation pair

# For each input tuple
for row in sys.stdin:
  parts = row.strip().split('\t')
  sentence_id, p1_id, p1_text, p2_id, p2_text = parts

  p1_text = p1_text.strip()
  p2_text = p2_text.strip()
  p1_text_lower = p1_text.lower()
  p2_text_lower = p2_text.lower()

  # DS rule 1: true if they appear in spouse KB, false if they appear in non-spouse KB
  is_true = '\N'
  if (p1_text_lower, p2_text_lower) in has_equity_transactions or \
     (p2_text_lower, p1_text_lower) in has_equity_transactions:
    is_true = '1'
  # elif (p1_text_lower, p2_text_lower) in non_spouses or \
  #     (p2_text_lower, p1_text_lower) in non_spouses:
  #  is_true = '0'
  # DS rule 3: false if they appear to be in same person
  elif (p1_text == p2_text) or (p1_text in p2_text) or (p2_text in p1_text):
    is_true = '0'
  # DS rule 4 false if they are both married, but not married to each other:
  # elif p1_text_lower in related_companies and p2_text_lower in related_companies:
    is_true = '0'

  # Output relation candidates into output table
  print '\t'.join([
    p1_id, p2_id, sentence_id,
    "%s-%s" %(p1_text, p2_text),
    is_true,
    "%s-%s" %(p1_id, p2_id),
    '\N'   # leave "id" blank for system!
    ])

  # TABLE FORMAT: CREATE TABLE has_spouse(
  # person1_id bigint,
  # person2_id bigint,
  # sentence_id bigint,
  # description text,
  # is_true boolean,
  # relation_id bigint, -- unique identifier for has_spouse
  # id bigint   -- reserved for DeepDive
  # );
