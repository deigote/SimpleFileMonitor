package com.deigote.file.monitor

import java.nio.file.WatchKey

interface FileMonitorDelegate {

   void processEvent(WatchKey watchKey)

}
