package com.deigote.file.monitor

import com.deigote.misc.Logging
import spock.lang.Specification
import spock.lang.Unroll
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes

class FileUtilsSpec extends Specification implements Logging {

   void tearDown() {
      TestUtils.instance.cleanMetaClasses FileSystems, FileSystem, Files, FileUtils
   }

   @Unroll
   void "when invoked #recursiveLabel, the method findDirectoriesPaths will collect the paths of the given root directories #recursiveCallLabel"() {
      given: 'A collection of files'
      List<File> files = rawPaths.collect { Mock(File) }.eachWithIndex { File entry, int i ->
         entry.getPath() >> rawPaths[i]
      }

      and: 'A working default file system'
      List<String> rawPathsWhosePathHasBeenAskedFor = []
      List<Path> returnedPaths = [], pathsWhoseSubPathsHasBeenAskedFor = [], returnedSubpaths = []
      def mockedDefaultFS = FileSystems.getDefault()
      mockedDefaultFS.metaClass.getPath = { String rawPath ->
         rawPathsWhosePathHasBeenAskedFor << rawPath
         returnedPaths.add(Mock(Path))
         return returnedPaths.last()
      }
      GroovyMock(FileSystems, global:true)
      FileSystems.getDefault() >> mockedDefaultFS

      and: 'A method to obtain all subpaths of a given path'
      FileUtils.metaClass.'static'.findAllSubPaths = { Path path ->
         pathsWhoseSubPathsHasBeenAskedFor.add(path)
         returnedSubpaths << Mock(Path)
         return [path, returnedSubpaths.last()] as Set
      }

      when: "findDirectoriesPaths is invoked ${recursiveLabel}"
      def obtainedPaths = FileUtils.findDirectoriesPaths(files, recursiveArg)

      then: 'all the files has been asked for their path'
      rawPathsWhosePathHasBeenAskedFor.size() == rawPaths.size()
      rawPathsWhosePathHasBeenAskedFor.every { it in rawPaths }

      and: 'the paths returned by the file system, and the subpaths when recursive, are returned'
      obtainedPaths == (returnedPaths as Set) + (returnedSubpaths as Set)

      and: 'When recursive, all the paths has been asked for their subpaths and those are part of the result'
      pathsWhoseSubPathsHasBeenAskedFor == (recursiveArg ? returnedPaths : [])

      where:
      rawPaths = ['/aFile', '/anotherFile', '/aFile/aSubfile']

      recursiveArg | recursiveLabel     | recursiveCallLabel
      true         | 'as recursive'     | 'and all its subpaths'
      false        | 'as non recursive' | ''
   }

   void "the method findAllSubPaths uses a file tree walker with the provided root path"() {
      given: 'A path with some sub paths'
      def rootPath = Mock(Path)
      def subPaths = (0..2).collect { Mock(Path) }.eachWithIndex { it, idx -> it.hashCode() >> idx }
      def allPaths = [rootPath] + subPaths

      and: 'A working WalkFileTree method'
      Files.metaClass.'static'.walkFileTree = { Path path, FileVisitor fileVisitor ->
         allPaths.each { fileVisitor.preVisitDirectory(it, Mock(BasicFileAttributes)) }
      }

      when: 'findAllSubPaths is invoked for that root path'
      def obtainedPaths = FileUtils.findAllSubPaths(rootPath)

      then: 'the paths passed to the file visitor are returned as a result'
      obtainedPaths == allPaths as Set
   }

}