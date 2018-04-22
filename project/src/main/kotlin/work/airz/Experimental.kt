package work.airz

import org.bytedeco.javacpp.*
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.io.File

fun faceDetect() {
    var converterToMat = OpenCVFrameConverter.ToMat()

    val face_cascade = opencv_objdetect.CascadeClassifier("lbpcascade_animeface.xml")


    var grabber = OpenCVFrameGrabber.createDefault(File("output.mp4"))
    grabber.start()

    var videoMat: opencv_core.Mat
    var count = 0
    while (grabber.frameNumber < grabber.lengthInFrames) {
        var grabbed = grabber.grab() ?: break
        videoMat = converterToMat.convert(grabbed)
        var videoMatGray = opencv_core.Mat()
        opencv_imgproc.cvtColor(videoMat, videoMatGray, opencv_imgproc.COLOR_BGRA2GRAY)
        opencv_imgproc.equalizeHist(videoMatGray, videoMatGray)
        var faces = opencv_core.RectVector()

        face_cascade.detectMultiScale(videoMatGray, faces, 1.1, 5, 0, opencv_core.Size(100, 100), opencv_core.Size(1000, 1000))
        for (i in 0 until faces.size().toInt()) {
            val face_i = faces[i.toLong()]
            opencv_imgproc.rectangle(videoMat, face_i, opencv_core.Scalar(0.0, 255.0, 0.0, 4.0), 10, 1, 0)
        }
        if (faces.size() > 0) {
            opencv_highgui.imshow("face_recognizer", videoMat)
            opencv_imgcodecs.imwrite("out-$count.png", videoMat)
            count++
            println("face detected!")
        }
    }


}