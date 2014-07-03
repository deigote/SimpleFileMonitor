package com.deigote.file.monitor

import com.deigote.misc.Logging

import java.nio.file.Path
import java.nio.file.WatchService

import static com.deigote.file.monitor.FileUtils.findDirectoriesPaths
import static com.deigote.file.monitor.FileUtils.createWatchService
import static java.nio.file.StandardWatchEventKinds.*

class FileMonitor implements Runnable, Logging {

   final Set<Path> monitoredPaths = [] as Set
   final FileMonitorDelegate monitorDelegate
   final Boolean recursive
   final WatchService fileWatcher = createWatchService()

   FileMonitor(
      Collection<File> rootDirectories, FileMonitorDelegate monitorDelegate,
      Boolean recursive = true
   ) {
      this.recursive = recursive
      monitoredPaths = findDirectoriesPaths(rootDirectories, recursive).each { Path monitoredDir ->
         logger.info "Monitoring ${monitoredDir} for sources changes"
         monitoredDir.register(fileWatcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE)
      }
   }

   @Override
   void run() {
   }

}
