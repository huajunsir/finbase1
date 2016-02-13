import csv, os, sys

#APP_HOME = os.environ['FINBASE_HOME']

# Load the spouse dictionary for distant supervision.
# A person can have multiple spouses
has_jiaoyi = set()
related_companies = set()
lines = open( '../data/labeled/guquan_jiaoyi.csv').readlines()
i=0
for line in lines: 
  i=i+1
  name1, name2 = line.strip().split(',')[0:2]
  name1=name1.replace('"','').replace('(','').replace(')','')
  name2=name2.replace('"','').replace('(','').replace(')','') 
  if(i<500):print(name1 + ':' + name2)
  has_jiaoyi.add((name1, name2))  # Add a spouse relation pair
  related_companies.add(name1)    # Record the person as married
  related_companies.add(name2)
