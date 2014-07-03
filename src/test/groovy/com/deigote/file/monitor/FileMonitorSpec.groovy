package com.deigote.file.monitor

import com.deigote.misc.Logging
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.WatchService
import java.util.logging.ConsoleHandler

class FileMonitorSpec extends Specification implements Logging {

   void tearDown() {
      cleanMetaClasses FileMonitor, FileUtils
   }

   @Unroll
   void "when a file monitor is created #recursivenessArgsLabel, is expected to be #recursiveLabel"() {
      given: 'The mandatory constructor args'
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

   void "a file monitor is runnable, so it is compatible with existing APIs such as Java Threads"() {
      expect:
      Runnable.isAssignableFrom(FileMonitor)
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

   WatchService mockFileWatcher() {
      GroovySpy(WatchService)
   }

   void cleanMetaClasses(Class ... classesToClean) {
      logger.info "Cleaning metaclasses for ${classesToClean}"
      classesToClean.each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
   }

}
