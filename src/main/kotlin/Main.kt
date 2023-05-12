import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
    val path = "input.txt".toPath()
    val input = FileSystem.SYSTEM.read(path) { readUtf8() }.dropLast(1)

}