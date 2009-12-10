#!/usr/bin/env python

import sys
import hashlib

h = hashlib.sha1()
h.update(sys.stdin.readline().strip())
print h.hexdigest()
