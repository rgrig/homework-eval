#!/usr/bin/env python

import sys
import hashlib

for line in sys.stdin:
  h = hashlib.sha1()
  fields = line.strip().split()
  h.update(fields[2])
  print fields[0], fields[1], h.hexdigest()
