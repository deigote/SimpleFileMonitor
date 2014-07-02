package com.deigote.file.monitor

import com.deigote.misc.Logging
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.ConsoleHandler

class FileMonitorSpec extends Specification implements Logging {

   @Unroll
   void "when a file monitor is created #recursivenessArgsLabel, is expected to be #recursiveLabel"() {
      //setup: 'An empty monitor delegate'
      //FileMonitorDelegate monitorDelegate = Mock(FileMonitorDelegate)
      given: 'The mandatory constructor args'
      List mandatoryArgs = [[], Mock(FileMonitorDelegate)]

      when: 'A file monitor is constructed with the specified recursiveness'
      List constructorArgs = mandatoryArgs + recursivenessArg
      logger.info "Building a file monitor with the args ${constructorArgs}"
      FileMonitor fileMonitor = new FileMonitor(*constructorArgs)

      then: 'The file monitor recursiveness is as expected'
      fileMonitor.recursive == expectedRecursiveness

      where:
      recursivenessArg | expectedRecursiveness | recursivenessArgsLabel
      []               | true                  | 'with no recursiveness specified'
      true             | true                  | 'as recursive'
      false            | false                 | 'as non recursive'

      recursiveLabel = expectedRecursiveness ? 'recursive' : 'non recursive'
   }

}
