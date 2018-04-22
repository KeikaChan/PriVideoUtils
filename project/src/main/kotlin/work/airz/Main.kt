package work.airz


import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_highgui.*
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import org.bytedeco.javacpp.opencv_imgproc.*
import org.bytedeco.javacpp.opencv_objdetect.*
import org.bytedeco.javacv.FFmpegFrameGrabber
import java.io.File


fun main(args: Array<String>) {
    var converterToMat = OpenCVFrameConverter.ToMat()

    val face_cascade = CascadeClassifier("lbpcascade_animeface.xml")


    var grabber = FFmpegFrameGrabber(File("output.mp4"))
    grabber.start()

    var videoMat: Mat
    var count=0
    while (grabber.frameNumber < grabber.lengthInFrames) {
        var grabbed = grabber.grabImage() ?: break
        videoMat = converterToMat.convert(grabbed)
        var videoMatGray = Mat()
        cvtColor(videoMat, videoMatGray, COLOR_BGRA2GRAY)
        equalizeHist(videoMatGray, videoMatGray)
        var faces = RectVector()

        face_cascade.detectMultiScale(videoMatGray, faces,1.1,5,0,Size(100,100),Size(1000,1000))
        for (i in 0 until faces!!.size().toInt()) {
            val face_i = faces[i.toLong()]
            rectangle(videoMat, face_i, Scalar(0.0, 255.0, 0.0, 4.0),10,1,0)
        }
        if (faces!!.size() > 0) {
            imshow("face_recognizer", videoMat)
            imwrite("out-$count.png",videoMat)
            count++
            println("face detected!")
        }
    }

}