import okio.FileSystem
import okio.Path.Companion.toPath

//region file system hierarchy
private data class File(val parent: Directory, val size: Int, val name: String)

private data class Directory(
    val parent: Directory?,
    val name: String,
    val directories: MutableList<Directory> = mutableListOf(),
    val files: MutableList<File> = mutableListOf(),
) {

    val size: Int get() = directories.sumOf { it.size } + files.sumOf { it.size }

    /**
     * Returns the [Directory] object referenced by the given [Path] relative to this [Directory].
     */
    fun changeDir(path: Path): Directory =
        when (path) {
            Path.Root -> rootDir
            Path.Up -> parent ?: throw IllegalStateException("Impossible to navigate up from directory $this")
            is Path.Directory -> directories.first { it.name == path.name }
        }
}

// To make sure there is only one root dir
private val rootDir = Directory(parent = null, name = "/")
//endregion

private const val totalDiskSpace = 70_000_000
private const val requiredSpaceForUpdate = 30_000_000
private const val maxOccupiedSpace = totalDiskSpace - requiredSpaceForUpdate

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

    val minSizeToBeDeleted = rootDir.size - maxOccupiedSpace

    val sizes = mutableListOf<Int>()
    rootDir.storeSizeAndSubdirsSizes(sizes)
    val smallestDirSizeToBeDeleted = sizes.filter { it >= minSizeToBeDeleted }.min()
    println(smallestDirSizeToBeDeleted)
}

/**
 * Prints a directory and its children recursively. Each directory with increased indentation, starting at given [offset].
 */
private fun Directory.print(offset: Int = 0) {
    val indent = " ".repeat(offset)
    println("${indent}dir $name")

    val childrenOffset = offset + 2
    directories.forEach { it.print(childrenOffset) }

    val childrenIndent = " ".repeat(childrenOffset)
    files.forEach { file -> with(file) { println("$childrenIndent$size $name") } }
}

/**
 * Gets size of this directory and its all subdirectories recursively and stores them in [sizes].
 */
private fun Directory.storeSizeAndSubdirsSizes(sizes: MutableList<Int>) {
    sizes.add(size)
    directories.forEach { it.storeSizeAndSubdirsSizes(sizes) }
}
