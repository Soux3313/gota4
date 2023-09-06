#!/bin/bash
if [[ $AMAZONS_DEBUG == "true" ]]; then
    debug_flag="-debug"
fi
exec java -jar /usr/local/bin/amazonsserver.jar -hostname 0.0.0.0 $debug_flag