:: ----------------------------------------------------------------------------------------------------
:: API reference documents will be generated in ./api directory by running this script.
:: After run this script, please open ./api/index.html by a web browser.
:: ----------------------------------------------------------------------------------------------------

javadoc -encoding UTF-8 -charset UTF-8 -windowtitle "Vnano API Reference" -d api -sourcepath src org.vcssl.nano.main org.vcssl.nano.spec org.vcssl.connect org.vcssl.nano org.vcssl.nano.compiler org.vcssl.nano.interconnect org.vcssl.nano.lang org.vcssl.nano.vm org.vcssl.nano.vm.assembler org.vcssl.nano.vm.accelerator org.vcssl.nano.vm.processor org.vcssl.nano.vm.memory
