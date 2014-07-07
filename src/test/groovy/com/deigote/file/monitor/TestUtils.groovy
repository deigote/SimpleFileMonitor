package com.deigote.file.monitor

import com.deigote.misc.Logging
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.FileSystems
import java.nio.file.Path

@Singleton
class TestUtils implements Logging {

   void cleanMetaClasses(Class ... classesToClean) {
      logger.info "Cleaning metaclasses for ${classesToClean}"
      classesToClean.each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
   }

}