package com.deigote.file.monitor

import com.deigote.misc.Logging
import java.nio.file.Path


class FileMonitor implements Logging {

   private Set<Path> monitoredPaths = [] as Set
   FileMonitorDelegate monitorDelegate
   Boolean recursive

   FileMonitor(
      Collection<File> rootDirectories, FileMonitorDelegate monitorDelegate,
      Boolean recursive = true
   ) {
      this.recursive = recursive
   }
}
