import okio.FileSystem
import okio.Path.Companion.toPath

//region parsing
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
//endregion

//region file system hierarchy
data class File(val parent: Directory, val size: Int, val name: String)

data class Directory(
    val parent: Directory?,
    val name: String,
    val directories: MutableList<Directory> = mutableListOf(),
    val files: MutableList<File> = mutableListOf(),
) {

    val size: Int get() = directories.sumOf { it.size } + files.sumOf { it.size }

    fun changeDir(path: Path): Directory =
        when (path) {
            Path.Root -> rootDir
            Path.Up -> parent ?: throw IllegalStateException("Impossible to navigate up from directory $this")
            is Path.Directory -> directories.first { it.name == path.name }
        }
}

// To make sure there is only one root dir
val rootDir = Directory(parent = null, name = "/")
//endregion

fun main(args: Array<String>) {
    val path = "input.txt".toPath()
    val input = FileSystem.SYSTEM.read(path) { readUtf8() }.dropLast(1)
    val terminalOutput = input.split("\n").map { line -> parse(line) }

    var currentDir: Directory = rootDir
    terminalOutput.forEach { line ->
        when (line) {
            is Line.Command.Cd -> currentDir = currentDir.changeDir(line.path)
            is Line.Command.Ls -> {}

            is Line.FileSystemItem.File -> currentDir.files.add(
                with(line) {
                    File(
                        parent = currentDir,
                        size = size,
                        name = name,
                    )
                }
            )

            is Line.FileSystemItem.Directory -> currentDir.directories.add(
                with(line) {
                    Directory(
                        parent = currentDir,
                        name = name,
                    )
                }
            )
        }
    }

}

//region parsing
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
//endregion
