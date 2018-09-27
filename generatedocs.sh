#!/bin/sh

# ----------------------------------------------------------------------------------------------------
# API reference documents will be generated in ./api directory by running this script.
# After run this script, please open ./api/index.html by a web browser.
# ----------------------------------------------------------------------------------------------------

javadoc -encoding UTF-8 -charset UTF-8 -windowtitle "Vnano API Reference" -d api -sourcepath src org.vcssl.connect org.vcssl.nano org.vcssl.nano.accelerator org.vcssl.nano.assembler org.vcssl.nano.compiler org.vcssl.nano.interconnect org.vcssl.nano.lang org.vcssl.nano.linker org.vcssl.nano.memory org.vcssl.nano.processor org.vcssl.nano.spec
