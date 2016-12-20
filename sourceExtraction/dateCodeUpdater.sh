#!/bin/bash
sed -i "s/\(release_date[^/]*=\).*/\1$(date -I)/" $1
