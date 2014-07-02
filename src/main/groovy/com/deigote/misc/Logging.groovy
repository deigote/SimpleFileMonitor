package com.deigote.misc

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by deigote on 03/07/14.
 */
trait Logging {

   Logger getLogger() {
      LoggerFactory.getLogger(this.getClass())
   }

}
