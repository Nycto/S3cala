package com.roundeights.s3cala

import java.io.{File, IOException}
import java.io.{InputStream, FileInputStream, FileOutputStream}
import org.apache.commons.io.IOUtils

import scala.concurrent.{Promise, Future, ExecutionContext}


/** Companion */
object LocalS3 {

    /** Ensures that the given directory exists */
    private[s3cala] def ensureDir ( dir: File ): File = {
        if ( !dir.exists && !dir.mkdir )
            throw new S3Failed("Could not create dir: " + dir)
        if ( !dir.isDirectory )
            throw new S3Failed("Not a dir: " + dir)
        dir
    }

    /** Creates a temporary directory */
    private def tempDir: File = {
        val temp = File.createTempFile("S3-",".tmp")
        if ( !temp.delete )
            throw new S3Failed("Could not delete temporary file: " + temp)
        ensureDir( temp )
    }
}

/**
 * S3 Client wrapper
 */
class LocalS3 (
    private val root: File,
    private val context: ExecutionContext
) extends S3 {

    /** Alternate constructor that uses a temporary directory */
    def this ( context: ExecutionContext ) = this( LocalS3.tempDir, context )

    /** {@inheritDoc} */
    override def bucket ( bucket: String )
        = new LocalBucket( bucket, new File(root, bucket), context )

    /** {@inheritDoc} */
    override def close: Unit = ()
}


/**
 * A specific S3 bucket
 */
class LocalBucket (
    private val bucket: String,
    private val dir: File,
    implicit private val context: ExecutionContext
) extends Bucket {

    /** Generates a file from a key */
    private def find ( key: String ) = new File( dir, key )

    /** {@inheritDoc} */
    override def get ( key: String, file: File ): Future[Unit] = {
        Future {
            LocalS3.ensureDir( dir )
            IOUtils.copy(
                new FileInputStream( find(key) ),
                new FileOutputStream( file )
            )
        }
    }

    /** {@inheritDoc} */
    override def get ( key: String ): Future[InputStream] = Future {
        LocalS3.ensureDir( dir )
        val file = find(key)
        if ( !file.exists )
            throw new S3NotFound( bucket, key )
        new FileInputStream( file )
    }

    /** {@inheritDoc} */
    override def put ( key: String, file: File ): Future[Unit] = {
        Future {
            LocalS3.ensureDir( dir )
            IOUtils.copy(
                new FileInputStream( file ),
                new FileOutputStream( find(key) )
            )
        }
    }

    /** {@inheritDoc} */
    override def put ( key: String, stream: InputStream ): Future[Unit] = {
        Future {
            LocalS3.ensureDir( dir )
            IOUtils.copy( stream, new FileOutputStream( find(key) ) )
        }
    }

}

