import okio.FileSystem
import okio.Path.Companion.toPath

sealed interface Path {
    object Root : Path
    object Up : Path
    data class Directory(val name: String) : Path
}

sealed interface Line {

    sealed interface Command : Line {
        data class Cd(val path: Path) : Command
        object Ls : Command
    }

    sealed interface FileSystemItem : Line {
        data class File(val size: Int, val name: String) : FileSystemItem
        data class Directory(val name: String) : FileSystemItem
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
        line.startsWith("dir") -> parseDirectory(line)
        """\d+ .*""".toRegex() matches line -> parseFile(line)
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

fun parseDirectory(line: String): Line.FileSystemItem.Directory {
    val result = """dir (.+)""".toRegex().matchEntire(line)
    val name = result?.groups?.get(1)?.value ?: throw IllegalStateException("dir name is null")
    return Line.FileSystemItem.Directory(name)
}

fun parseFile(line: String): Line.FileSystemItem.File {
    val result = """(\d+) (.+)""".toRegex().matchEntire(line)
    val size = result?.groups?.get(1)?.value?.toInt() ?: throw IllegalStateException("file size is null")
    val name = result.groups[2]?.value ?: throw IllegalStateException("file name is null")
    return Line.FileSystemItem.File(size, name)
}
