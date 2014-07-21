package com.deigote.file.monitor

import com.deigote.misc.Logging
import org.concordion.integration.junit4.ConcordionRunner
import org.junit.runner.RunWith
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.WatchKey
import java.nio.file.WatchService

@RunWith(ConcordionRunner)
class SimpleFileMonitorIntegrationSpec extends Specification implements Logging {

   void cleanup() {
      cleanMetaClasses FileMonitor, FileUtils
   }

   @Unroll
   void "when a file monitor is created #recursivenessArgsLabel, is expected to be #recursiveLabel"() {
      given: 'The mandatory constructor args '
      List mandatoryArgs = [[], Mock(FileMonitorDelegate)]

      and: 'Working dependencies'
      GroovyMock(FileUtils, global:true)
      FileUtils.findDirectoriesPaths(_,_) >> { files, rec -> []}

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

   @Unroll
   void "when created (as #recursivenessLabel), a file monitor will use FileUtils to find the directories paths (as #recursivenessLabel) and register them with the file watcher"() {
      given: 'A set of files'
      List<File> rootFiles = ['/aFile', '/anotherFile', '/aFile/aSubfile'].collect { Mock(File) }
      List<Path> rootPaths = rootFiles.collect { Mock(Path) }, receivedPaths = []

      and: 'Working dependencies'
      GroovyMock(FileUtils, global:true)
      def watchService = GroovyMock(WatchService)

      when: "A file monitor is created for that list of files as $recursivenessLabel"
      FileMonitor fileMonitor = new FileMonitor(rootFiles, Mock(FileMonitorDelegate), recursive)

      then: "A watch service is created to monitor the files"
      1 * FileUtils.createWatchService() >> watchService

      and: "The FileUtils is asked to find the given directories as $recursivenessLabel"
      1 * FileUtils.findDirectoriesPaths(rootFiles, recursive) >> rootPaths

      and: "The watch service is asked to monitor the paths returned by FileUtils"
      rootPaths.each { returnedPath ->
         interaction {
            1 * returnedPath.register(watchService, *_)
         }
      }

      where:
      recursive | recursivenessLabel
      true      | 'recursive'
      false     | 'non recursive'
   }

   void "a file monitor is runnable, so it is compatible with existing APIs such as Java Threads"() {
      expect:
      Runnable.isAssignableFrom(FileMonitor)
   }

   void "when a file monitor is runned, it takes events from its file watcher and pass them to its delegate"() {
      given: 'A mocked watch service that always return some watch key'
      List takenWatchKeys = []
      def watchService = [ take: { ->
         if (takenWatchKeys.size() > 10) sleep 100
         takenWatchKeys << Mock(WatchKey)
         takenWatchKeys.last()
      }] as WatchService

      and: 'A delegate who just stores what it gets'
      List passedWatchKeys = []
      FileMonitorDelegate monitorDelegate = [
         processEvent: { e -> passedWatchKeys << e }
      ] as FileMonitorDelegate

      and: 'A mocked FileUtils to return the mocked watch service'
      GroovyMock(FileUtils, global:true)
      FileUtils.createWatchService() >> watchService
      FileUtils.findDirectoriesPaths(_,_) >> [ Mock(Path)]

      when: 'A file monitor is run for an amount of time'
      FileMonitor fileMonitor = new FileMonitor([ Mock(File) ], monitorDelegate)
      Thread fileMonitorRunner = new Thread(fileMonitor)
      fileMonitorRunner.start()
      sleep 100
      fileMonitor.stop()
      sleep 100

      then: 'All the keys that it takes from the watch service are passed to the delegate'
      logger.info "${takenWatchKeys} were taken and ${passedWatchKeys} were received"
      takenWatchKeys.size() == passedWatchKeys.size()
   }

   void cleanMetaClasses(Class ... classesToClean) {
      logger.info "Cleaning metaclasses for ${classesToClean}"
      classesToClean.each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
   }

}
