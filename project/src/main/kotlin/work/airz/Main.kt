package work.airz

import net.bramp.ffmpeg.FFprobe
import org.bytedeco.javacpp.*
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
//    println(ImageSearch().getVector(ImageIO.read(File("./open.png"))))
    var converterToMat = OpenCVFrameConverter.ToMat()
    var converter = Java2DFrameConverter()
    val input = File("キラキラ.mp4")
    var grabber = OpenCVFrameGrabber.createDefault(input)
    grabber.start()

    print("動画の長さ")
    calcTime(grabber.lengthInFrames, grabber.frameRate + 1)

    val currentDir = System.getProperty("user.dir")
    val ffprobeExec = File(currentDir + File.separator + "encoder", getExtByPlatform("ffprobe"))
    if (!ffprobeExec.exists()) {
        println("encoder does not exist!")
        return
    }
    val ffmprobe = FFprobe(ffprobeExec.absolutePath)
    val format = ffmprobe.probe(input.absolutePath)
    val videoFormat = format.streams[0]
    val videoFrameRate = videoFormat.r_frame_rate.toDouble()


    println("フレームレート ${videoFrameRate}")
    var whiteCount = 0
    var frameCount = 0
    var startFrame = 0
    var stopFrame = 0
    var intervalList: List<Double>? = null

    while (grabber.frameNumber < grabber.lengthInFrames && stopFrame == 0) {
        val grabbed = grabber.grab() ?: break
        if (grabbed.image == null) continue
//        if (grabber.frameNumber % 2 == 1) continue //負荷削減
        val image = converter.convert(grabbed)
        /**
         *
         * プリ
         * キラッとボタンを押した時に真っ白になるからそこで時間判定
         *
         * 曲ごとの秒数
         * レディー・アクション            120秒　赤ステージ 開始        5559700559225408535
         * Let'sプリ☆チャン              143秒　町        開始        -1122503488345012281
         * キラキラプリ☆チャンワールド     127秒　町
         * ワン・ツー・スウィーツ          119秒　花舞台     開始        3978760074848754912
         *
         * 最後に白フレームが5~10フレームくらい入る
         */

        if (startFrame == 0) intervalList = getTarget(image, videoFrameRate)

        if (intervalList != null && startFrame != 0) {
            intervalList.forEach {
                if (grabber.frameNumber - startFrame + videoFrameRate * 5 > it && ImageSearch().isSameImage(ImageSearch().getVector(image), 0, 0L)) {
                    //白フレームに入ったか確認
                    whiteCount++
                    if (whiteCount > 5) {
                        calcTime(grabber.frameNumber, videoFrameRate)
                        stopFrame = grabber.frameNumber
                        println("継続時間" + (calcTime(stopFrame, videoFrameRate) - calcTime(startFrame, videoFrameRate)))
                    }
                }
            }
        }

        if (intervalList != null && startFrame == 0) {//フレームカウントの開始
            println("frame hash${ImageSearch().getVector(image)}")
            calcTime(grabber.frameNumber, videoFrameRate)//フレームレートが多分おかしいのでffmpegからとってる
            startFrame = grabber.frameNumber
            imwrite("${String.format("%06d", grabber.frameNumber)}_${String.format("%d", ImageSearch().getVector(image))}.png", converterToMat.convert(grabbed))
            println("フレーム数" + grabber.frameNumber)
        }

        if (grabber.frameNumber % 100 == 0) calcTime(grabber.frameNumber, videoFrameRate)
    }
}

/**
 * ターゲットだった場合に曲の秒数リストを返す。
 * (会場ごとに複数の曲が存在する
 */
fun getTarget(image: BufferedImage, frameRate: Double): List<Double>? {
    val interValMap = mapOf(Pair(5559700559225408535L, listOf(120 * frameRate)), Pair(-1122503488345012281L, listOf(143 * frameRate, 127 * frameRate)), Pair(3978760074848754912L, listOf(119 * frameRate)))
    val hash = interValMap.keys.firstOrNull { ImageSearch().isSameImage(ImageSearch().getVector(image), 2, it) }
    return if (hash != null) {
        interValMap[hash]
    } else {
        null
    }
}

fun calcTime(frame: Int, frameRate: Double): Double {
    val time = frame / frameRate
    println("時間  ${String.format("%02d", (time / 60).toInt())}:${String.format("%05.2f", time % 60)}")
    return time //なんフレーム目か分かれば求まる
}

fun convert() {
    var converterToMat = OpenCVFrameConverter.ToMat()
    var converter = Java2DFrameConverter()
    var grabber = OpenCVFrameGrabber.createDefault(File("output.mp4"))
    grabber.start()

    var videoMat: opencv_core.Mat
    var count = 0
    while (grabber.frameNumber < grabber.lengthInFrames) {
        val grabbed = grabber.grab() ?: break
        val image = converter.convert(grabbed)
        if (ImageSearch().getVector(image) == -8887692564028994481L) {
            println("同じの会った")
            println(grabbed.timestamp)
            imwrite("${String.format("%06d", count)}_${String.format("%06d", ImageSearch().getVector(image))}.png", converterToMat.convert(grabbed))
            count++
        }

    }
}