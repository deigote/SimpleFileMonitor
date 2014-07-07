package com.deigote.file.monitor

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import static java.nio.file.FileSystems.getDefault as getDefaultFS

class FileUtils {

   static Set<Path> findDirectoriesPaths(Collection<File> rootDirectories, Boolean recursive) {
      Set<Path> rootPaths = rootDirectories*.getPath().collect { getDefaultFS().getPath(it) }
      recursive ? rootPaths.inject([] as Set) { Set<Path> gatheredPaths, Path rootDirectoryPath ->
         gatheredPaths + findAllSubPaths(rootDirectoryPath)
      } : rootPaths
   }

   static Set<Path> findAllSubPaths(Path rootDirectoryPath) {
      []
   }

   static WatchService createWatchService() {
      getDefaultFS().newWatchService()
   }

}
