# Advent of code 2022 - day 7

Today's task was about parsing a terminal log of browsing a filesystem (using `cd` and `ls` commands. Then recreating
that file system as a directory tree and calculating the directory sizes.

# What I learned

* It's good to have separate hierarchies for parsed tokens and the actual directory hierarchy.
  * A `Directory` in the path of a `cd` command is something different from a directory as an output of an `ls` command,
    and again something different from a directory in a directory hierarchy tree.
* Recursion is ok to use for traversing a tree, if you have a shallow tree (i.e. only a few levels of hierarchy)
* If a tree is deep enough that recursion can lead to stack overflow, you have to implement a stack yourself, to know
  your current position in the tree
* Calculating a directory size can be very easily implemented as a recursive property (i.e. directory size = size of all
  files + size of all subdirectories)
* In a directory tree it's important to store the parent directory in each node (to navigate up)
* It's convenient to implement `changeDir()` as a function of the `Directory` class. The reason is that you quite often
  navigate using relative paths (up or into a subdirectory).
* Browsing a filesystem (or any tree) is stateful - the current directory changes
* To repeat a string multiple times, you can use `String.repeat(Int)`. E.g.
  `"My string ".repeat(3) == "My string My string My string "`. Of course it also works for single character strings.

# Part 2 plot twist

It's actually very simple, instead of calculating a sum of sizes of selected directories, you have to find the smallest
directory of at least the given size.
