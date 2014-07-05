package com.deigote.file.monitor

import com.deigote.misc.Logging

import java.nio.file.Path
import java.nio.file.WatchKey
import java.nio.file.WatchService
import static com.deigote.file.monitor.FileUtils.findDirectoriesPaths
import static com.deigote.file.monitor.FileUtils.createWatchService
import static java.nio.file.StandardWatchEventKinds.*

class FileMonitor implements Runnable, Logging {

   final Set<Path> monitoredPaths = [] as Set
   final FileMonitorDelegate monitorDelegate
   final Boolean recursive
   final WatchService fileWatcher = createWatchService()

   private Boolean shouldRun = true

   FileMonitor(
      Collection<File> rootDirectories, FileMonitorDelegate monitorDelegate,
      Boolean recursive = true
   ) {
      this.monitorDelegate = monitorDelegate
      this.recursive = recursive
      monitoredPaths = findDirectoriesPaths(rootDirectories, recursive).each { Path monitoredDir ->
         logger.info "Monitoring ${monitoredDir} for sources changes"
         monitoredDir.register(fileWatcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE)
      }
   }

   @Override
   void run() {
      while (shouldRun) {
         WatchKey watchKey = fileWatcher.take()
         logger.info "File watcher return ${watchKey} with events ${watchKey.pollEvents()}"
         monitorDelegate.processEvent(watchKey)
      }
   }

   void stop() {
      shouldRun = false
   }

}
