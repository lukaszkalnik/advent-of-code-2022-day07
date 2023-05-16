/**
 * Path of the [Line.Command.Cd] command.
 */
sealed interface Path {
    object Root : Path
    object Up : Path
    data class Directory(val name: String) : Path
}

/**
 * Parsed input line
 */
sealed interface Line {

    sealed interface Command : Line {
        data class Cd(val path: Path) : Command
        object Ls : Command
    }

    /**
     * Possible outputs of the [Line.Command.Ls] command.
     */
    sealed interface FileSystemItem : Line {
        data class File(val size: Int, val name: String) : FileSystemItem
        data class Directory(val name: String) : FileSystemItem
    }
}

fun parse(line: String): Line =
    when {
        line == "$ ls" -> Line.Command.Ls
        line.startsWith("$ cd") -> parseCd(line)
        line.startsWith("dir") -> parseDirectory(line)
        """\d+ .*""".toRegex() matches line -> parseFile(line)
        else -> throw IllegalStateException("unrecognized line: $line")
    }

private fun parseCd(line: String): Line.Command.Cd {
    val result = """\$ cd (.*)""".toRegex().matchEntire(line)
    val pathString = result?.groups?.get(1)?.value ?: throw IllegalStateException("cd command path is null")
    val path = when (pathString) {
        "/" -> Path.Root
        ".." -> Path.Up
        else -> Path.Directory(pathString)
    }
    return Line.Command.Cd(path)
}

private fun parseDirectory(line: String): Line.FileSystemItem.Directory {
    val result = """dir (.+)""".toRegex().matchEntire(line)
    val name = result?.groups?.get(1)?.value ?: throw IllegalStateException("dir name is null")
    return Line.FileSystemItem.Directory(name)
}

private fun parseFile(line: String): Line.FileSystemItem.File {
    val result = """(\d+) (.+)""".toRegex().matchEntire(line)
    val size = result?.groups?.get(1)?.value?.toInt() ?: throw IllegalStateException("file size is null")
    val name = result.groups[2]?.value ?: throw IllegalStateException("file name is null")
    return Line.FileSystemItem.File(size, name)
}
