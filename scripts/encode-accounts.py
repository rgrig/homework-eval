#!/usr/bin/env python

import sys
import hashlib

for line in sys.stdin:
  h = hashlib.sha1()
  fields = line.strip().split()
  h.update(fields[1])
  print fields[0], h.hexdigest()
