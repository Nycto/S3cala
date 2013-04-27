package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.{TransferManager, Transfer}
import com.amazonaws.services.s3.model.{ProgressListener, ProgressEvent}
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.AmazonServiceException

import java.io.{InputStream, File, FileInputStream, ByteArrayInputStream}

import scala.concurrent.{Promise, Future, ExecutionContext}


/** An asynchronous S3 progress listener, per the AWS api */
private[s3cala] class Listener (
    private val result: Promise[Unit],
    private val transfer: Transfer
) extends ProgressListener {

    /** {@inheritDoc} */
    override def progressChanged ( event: ProgressEvent ): Unit = {
        event.getEventCode match {
            case ProgressEvent.FAILED_EVENT_CODE
                => result.failure( new S3Failed(transfer.waitForException) )
            case ProgressEvent.CANCELED_EVENT_CODE
                => result.failure( new S3Failed("Request Cancelled") )
            case ProgressEvent.COMPLETED_EVENT_CODE => result.success( Unit )
            case _ => ()
        }
    }
}


/**
 * A specific S3 bucket
 */
class Bucket (
    private val client: TransferManager,
    val bucket: String
) {

    /** Executes a transfer */
    private def transfer ( key: String, body: => Transfer ): Future[Unit] = {
        val result = Promise[Unit]

        try {
            val transfer = body
            transfer.addProgressListener( new Listener(result, transfer) )
        } catch {
            case err: AmazonServiceException if err.getStatusCode == 404
                => result.failure( new S3NotFound(bucket, key) )
            case err: Throwable => result.failure( new S3Failed(err) )
        }

        result.future
    }

    /** Downloads a key into a file */
    def get ( key: String, file: File ): Future[Unit]
        = transfer( key, client.download( bucket, key, file ) )

    /** Downloads a key */
    def get
        ( key: String )
        ( implicit context: ExecutionContext )
    : Future[InputStream] = {
        val file = File.createTempFile("S3-",".tmp")
        get( key, file ).map( _ => new FileInputStream(file) )
    }

    /** Uploads a file into a key */
    def put ( key: String, file: File ): Future[Unit]
        = transfer( key, client.upload( bucket, key, file ) )

    /** Uploads an input stream into a key */
    def put ( key: String, stream: InputStream ): Future[Unit] = {
        transfer( key, client.upload(
            bucket, key, stream, new ObjectMetadata
        ) )
    }

    /** Uploads a Byte Array into a key */
    def put ( key: String, bytes: Array[Byte] ): Future[Unit]
        = put( key, new ByteArrayInputStream( bytes ) )

    /** Uploads a String into a key */
    def put ( key: String, str: String ): Future[Unit]
        = put( key, str.getBytes("UTF8") )

}


