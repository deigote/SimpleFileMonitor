package com.deigote.file.monitor

import com.deigote.misc.Logging
import java.nio.file.Path

class FileMonitor implements Runnable, Logging {

   final Set<Path> monitoredPaths = [] as Set
   final FileMonitorDelegate monitorDelegate
   final Boolean recursive

   FileMonitor(
      Collection<File> rootDirectories, FileMonitorDelegate monitorDelegate,
      Boolean recursive = true
   ) {
      this.recursive = recursive
   }

   @Override
   void run() {
   }

}
