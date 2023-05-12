import okio.FileSystem
import okio.Path.Companion.toPath

sealed interface Path {
    object Root : Path
    object Up : Path
    data class Directory(val name: String) : Path
}

sealed interface Line {

    sealed interface Command {
        data class Cd(val path: Path) : Line
        object Ls : Line
    }

    sealed interface FileSystemItem {
        data class File(val size: Int, val name: String) : Line
        data class Directory(val name: String)
    }
}

fun main(args: Array<String>) {
    val path = "input.txt".toPath()
    val input = FileSystem.SYSTEM.read(path) { readUtf8() }.dropLast(1)
    input.split("\n").map { line -> parse(line) }.forEach { println(it) }
}

fun parse(line: String): Line =
    when {
        line == "$ ls" -> Line.Command.Ls
        line.startsWith("$ cd") -> parseCd(line)
        line.startsWith("dir") -> Line.Command.Ls//parseDirectory(line)
        """\d+ .*""".toRegex() matches line -> Line.Command.Ls //parseFile(line)
        else -> throw IllegalStateException("unrecognized line: $line")
    }

fun parseCd(line: String): Line.Command.Cd {
    val result = """\$ cd (.*)""".toRegex().matchEntire(line)
    val pathString = result?.groups?.get(1)?.value ?: throw IllegalStateException("cd command path is null")
    val path = when (pathString) {
        "/" -> Path.Root
        ".." -> Path.Up
        else -> Path.Directory(pathString)
    }
    return Line.Command.Cd(path)
}