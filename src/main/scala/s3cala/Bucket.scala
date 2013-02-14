package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.model.{ProgressListener, ProgressEvent}

import java.io.{InputStream, File, FileInputStream}

import scala.concurrent.{Promise, Future, ExecutionContext}

/**
 * A specific S3 bucket
 */
class Bucket (
    private val client: TransferManager,
    val bucket: String
) {

    /** Downloads a key into a file */
    def get ( key: String, file: File ): Future[Unit] = {
        val result = Promise[Unit]

        val upload = client.download( bucket, key, file )

        upload.addProgressListener(new ProgressListener {
            override def progressChanged ( event: ProgressEvent ): Unit = {
                event.getEventCode match {
                    case ProgressEvent.FAILED_EVENT_CODE
                        => result.failure( upload.waitForException )
                    case ProgressEvent.CANCELED_EVENT_CODE
                        => result.failure( new Exception )
                    case ProgressEvent.COMPLETED_EVENT_CODE
                        => result.success( Unit )
                    case _ => ()
                }
            }
        })

        result.future
    }

    /** Downloads a key */
    def get
        ( key: String )
        ( implicit context: ExecutionContext )
    : Future[InputStream] = {
        val file = File.createTempFile("S3-",".tmp")
        get( key, file ).map( _ => new FileInputStream(file) )
    }

}


