package com.win11launcher.command.commands

import android.content.Context
import android.os.Environment
import com.win11launcher.command.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileCommands {
    
    fun getListCommand() = CommandDefinition(
        name = "ls",
        category = CommandCategory.FILE,
        description = "List directory contents",
        usage = "ls [path] [--long] [--all] [--size]",
        examples = listOf(
            "ls",
            "ls /sdcard",
            "ls --long",
            "ls --all --size"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "Directory path to list",
                defaultValue = "."
            ),
            CommandParameter(
                name = "long",
                type = ParameterType.BOOLEAN,
                description = "Long format with details",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "all",
                type = ParameterType.BOOLEAN,
                description = "Show hidden files",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "size",
                type = ParameterType.BOOLEAN,
                description = "Show file sizes",
                defaultValue = "false"
            )
        ),
        aliases = listOf("list", "dir"),
        executor = ListCommandExecutor()
    )
    
    fun getCopyCommand() = CommandDefinition(
        name = "cp",
        category = CommandCategory.FILE,
        description = "Copy files or directories",
        usage = "cp <source> <destination> [--recursive]",
        examples = listOf(
            "cp file.txt backup.txt",
            "cp /sdcard/file.txt /sdcard/backup/",
            "cp --recursive /sdcard/folder /sdcard/backup/"
        ),
        parameters = listOf(
            CommandParameter(
                name = "source",
                type = ParameterType.PATH,
                description = "Source file or directory",
                required = true
            ),
            CommandParameter(
                name = "destination",
                type = ParameterType.PATH,
                description = "Destination path",
                required = true
            ),
            CommandParameter(
                name = "recursive",
                type = ParameterType.BOOLEAN,
                description = "Copy directories recursively",
                defaultValue = "false"
            )
        ),
        aliases = listOf("copy"),
        executor = CopyCommandExecutor()
    )
    
    fun getMoveCommand() = CommandDefinition(
        name = "mv",
        category = CommandCategory.FILE,
        description = "Move or rename files",
        usage = "mv <source> <destination>",
        examples = listOf(
            "mv oldname.txt newname.txt",
            "mv /sdcard/file.txt /sdcard/documents/"
        ),
        parameters = listOf(
            CommandParameter(
                name = "source",
                type = ParameterType.PATH,
                description = "Source file or directory",
                required = true
            ),
            CommandParameter(
                name = "destination",
                type = ParameterType.PATH,
                description = "Destination path",
                required = true
            )
        ),
        aliases = listOf("move", "rename"),
        executor = MoveCommandExecutor()
    )
    
    fun getRemoveCommand() = CommandDefinition(
        name = "rm",
        category = CommandCategory.FILE,
        description = "Remove files or directories",
        usage = "rm <path> [--recursive] [--force]",
        examples = listOf(
            "rm file.txt",
            "rm --recursive folder/",
            "rm --force --recursive temp/"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "File or directory to remove",
                required = true
            ),
            CommandParameter(
                name = "recursive",
                type = ParameterType.BOOLEAN,
                description = "Remove directories recursively",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "force",
                type = ParameterType.BOOLEAN,
                description = "Force removal without confirmation",
                defaultValue = "false"
            )
        ),
        aliases = listOf("delete", "del"),
        executor = RemoveCommandExecutor()
    )
    
    fun getMkdirCommand() = CommandDefinition(
        name = "mkdir",
        category = CommandCategory.FILE,
        description = "Create directories",
        usage = "mkdir <path> [--parents]",
        examples = listOf(
            "mkdir newfolder",
            "mkdir --parents /sdcard/deep/nested/folder"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "Directory path to create",
                required = true
            ),
            CommandParameter(
                name = "parents",
                type = ParameterType.BOOLEAN,
                description = "Create parent directories as needed",
                defaultValue = "false"
            )
        ),
        aliases = listOf("makedir"),
        executor = MkdirCommandExecutor()
    )
    
    fun getCatCommand() = CommandDefinition(
        name = "cat",
        category = CommandCategory.FILE,
        description = "Display file contents",
        usage = "cat <file> [--lines=N]",
        examples = listOf(
            "cat file.txt",
            "cat --lines=50 largefile.txt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to display",
                required = true
            ),
            CommandParameter(
                name = "lines",
                type = ParameterType.INTEGER,
                description = "Maximum lines to display",
                defaultValue = "1000"
            )
        ),
        aliases = listOf("view", "type"),
        executor = CatCommandExecutor()
    )

    fun getPwdCommand() = CommandDefinition(
        name = "pwd",
        category = CommandCategory.FILE,
        description = "Print current working directory",
        usage = "pwd",
        examples = listOf("pwd"),
        parameters = emptyList(),
        aliases = listOf("cwd"),
        executor = PwdCommandExecutor()
    )

    fun getTouchCommand() = CommandDefinition(
        name = "touch",
        category = CommandCategory.FILE,
        description = "Create empty files or update timestamps",
        usage = "touch <file> [--create-dirs]",
        examples = listOf(
            "touch newfile.txt",
            "touch --create-dirs /sdcard/new/folder/file.txt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to create or update",
                required = true
            ),
            CommandParameter(
                name = "create-dirs",
                type = ParameterType.BOOLEAN,
                description = "Create parent directories if needed",
                defaultValue = "false"
            )
        ),
        aliases = listOf("create"),
        executor = TouchCommandExecutor()
    )

    fun getFindCommand() = CommandDefinition(
        name = "find",
        category = CommandCategory.FILE,
        description = "Search for files and directories",
        usage = "find <path> [--name=pattern] [--type=f|d] [--size=+/-N] [--limit=N]",
        examples = listOf(
            "find /sdcard --name=\"*.txt\"",
            "find . --type=d --name=\"*cache*\"",
            "find /sdcard --size=+1M --limit=10"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "Directory to search in",
                required = true
            ),
            CommandParameter(
                name = "name",
                type = ParameterType.STRING,
                description = "File name pattern (supports wildcards)",
                required = false
            ),
            CommandParameter(
                name = "type",
                type = ParameterType.ENUM,
                description = "File type: f=file, d=directory",
                options = listOf("f", "d"),
                required = false
            ),
            CommandParameter(
                name = "size",
                type = ParameterType.STRING,
                description = "Size filter (+/-N with K/M/G suffix)",
                required = false
            ),
            CommandParameter(
                name = "limit",
                type = ParameterType.INTEGER,
                description = "Maximum results to return",
                defaultValue = "100"
            )
        ),
        aliases = listOf("search"),
        executor = FindCommandExecutor()
    )

    fun getGrepCommand() = CommandDefinition(
        name = "grep",
        category = CommandCategory.FILE,
        description = "Search for patterns within files",
        usage = "grep <pattern> <file> [--ignore-case] [--line-numbers] [--count]",
        examples = listOf(
            "grep \"TODO\" /sdcard/notes.txt",
            "grep --ignore-case --line-numbers \"error\" /sdcard/log.txt",
            "grep --count \"import\" /sdcard/code.kt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "pattern",
                type = ParameterType.STRING,
                description = "Pattern to search for",
                required = true
            ),
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to search in",
                required = true
            ),
            CommandParameter(
                name = "ignore-case",
                type = ParameterType.BOOLEAN,
                description = "Case-insensitive search",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "line-numbers",
                type = ParameterType.BOOLEAN,
                description = "Show line numbers",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "count",
                type = ParameterType.BOOLEAN,
                description = "Show only count of matches",
                defaultValue = "false"
            )
        ),
        aliases = listOf("search-text"),
        executor = GrepCommandExecutor()
    )

    fun getStatCommand() = CommandDefinition(
        name = "stat",
        category = CommandCategory.FILE,
        description = "Display file or directory statistics",
        usage = "stat <path>",
        examples = listOf(
            "stat /sdcard/file.txt",
            "stat /sdcard/documents/"
        ),
        parameters = listOf(
            CommandParameter(
                name = "path",
                type = ParameterType.PATH,
                description = "File or directory to analyze",
                required = true
            )
        ),
        aliases = listOf("info"),
        executor = StatCommandExecutor()
    )

    fun getHeadCommand() = CommandDefinition(
        name = "head",
        category = CommandCategory.FILE,
        description = "Display first lines of a file",
        usage = "head <file> [--lines=N]",
        examples = listOf(
            "head file.txt",
            "head --lines=20 largefile.txt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to display",
                required = true
            ),
            CommandParameter(
                name = "lines",
                type = ParameterType.INTEGER,
                description = "Number of lines to display",
                defaultValue = "10"
            )
        ),
        aliases = listOf("top"),
        executor = HeadCommandExecutor()
    )

    fun getTailCommand() = CommandDefinition(
        name = "tail",
        category = CommandCategory.FILE,
        description = "Display last lines of a file",
        usage = "tail <file> [--lines=N]",
        examples = listOf(
            "tail file.txt",
            "tail --lines=20 largefile.txt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to display",
                required = true
            ),
            CommandParameter(
                name = "lines",
                type = ParameterType.INTEGER,
                description = "Number of lines to display",
                defaultValue = "10"
            )
        ),
        aliases = listOf("bottom"),
        executor = TailCommandExecutor()
    )

    fun getWcCommand() = CommandDefinition(
        name = "wc",
        category = CommandCategory.FILE,
        description = "Count lines, words, and characters in files",
        usage = "wc <file> [--lines] [--words] [--chars]",
        examples = listOf(
            "wc file.txt",
            "wc --lines file.txt",
            "wc --words --chars file.txt"
        ),
        parameters = listOf(
            CommandParameter(
                name = "file",
                type = ParameterType.PATH,
                description = "File to count",
                required = true
            ),
            CommandParameter(
                name = "lines",
                type = ParameterType.BOOLEAN,
                description = "Show only line count",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "words",
                type = ParameterType.BOOLEAN,
                description = "Show only word count",
                defaultValue = "false"
            ),
            CommandParameter(
                name = "chars",
                type = ParameterType.BOOLEAN,
                description = "Show only character count",
                defaultValue = "false"
            )
        ),
        aliases = listOf("count"),
        executor = WcCommandExecutor()
    )
}

class ListCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: arguments.firstOrNull() ?: "."
        val longFormat = parameters["long"]?.toBoolean() ?: false
        val showAll = parameters["all"]?.toBoolean() ?: false
        val showSize = parameters["size"]?.toBoolean() ?: false
        
        return try {
            val actualPath = if (path == ".") {
                Environment.getExternalStorageDirectory().absolutePath
            } else {
                path
            }
            
            val directory = File(actualPath)
            
            if (!directory.exists()) {
                return CommandResult(
                    success = false,
                    output = "Directory not found: $actualPath",
                    executionTimeMs = 0
                )
            }
            
            if (!directory.isDirectory) {
                return CommandResult(
                    success = false,
                    output = "Not a directory: $actualPath",
                    executionTimeMs = 0
                )
            }
            
            val files = directory.listFiles()?.filter { file ->
                showAll || !file.name.startsWith(".")
            }?.sortedBy { it.name } ?: emptyList()
            
            val output = buildString {
                appendLine("Contents of $actualPath:")
                appendLine()
                
                if (files.isEmpty()) {
                    appendLine("Directory is empty")
                } else {
                    files.forEach { file ->
                        when {
                            longFormat -> {
                                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    .format(Date(file.lastModified()))
                                val size = if (file.isFile()) formatFileSize(file.length()) else "<DIR>"
                                val permissions = getPermissionsString(file)
                                appendLine("$permissions  $size  $date  ${file.name}")
                            }
                            showSize -> {
                                val size = if (file.isFile()) formatFileSize(file.length()) else "<DIR>"
                                appendLine("${size.padEnd(10)}  ${file.name}")
                            }
                            else -> {
                                val indicator = if (file.isDirectory) "/" else ""
                                appendLine("${file.name}$indicator")
                            }
                        }
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error listing directory: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = size.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", value, units[unitIndex])
    }
    
    private fun getPermissionsString(file: File): String {
        return buildString {
            append(if (file.isDirectory) "d" else "-")
            append(if (file.canRead()) "r" else "-")
            append(if (file.canWrite()) "w" else "-")
            append(if (file.canExecute()) "x" else "-")
            append("------") // Placeholder for group/other permissions
        }
    }
}

class CopyCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val source = parameters["source"] ?: arguments.getOrNull(0)
        val destination = parameters["destination"] ?: arguments.getOrNull(1)
        val recursive = parameters["recursive"]?.toBoolean() ?: false
        
        if (source == null || destination == null) {
            return CommandResult(
                success = false,
                output = "Both source and destination are required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val sourceFile = File(source)
            val destFile = File(destination)
            
            if (!sourceFile.exists()) {
                return CommandResult(
                    success = false,
                    output = "Source file not found: $source",
                    executionTimeMs = 0
                )
            }
            
            val result = if (sourceFile.isDirectory) {
                if (!recursive) {
                    return CommandResult(
                        success = false,
                        output = "Use --recursive to copy directories",
                        executionTimeMs = 0
                    )
                }
                copyDirectory(sourceFile, destFile)
            } else {
                copyFile(sourceFile, destFile)
            }
            
            if (result) {
                CommandResult(
                    success = true,
                    output = "Successfully copied $source to $destination",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Failed to copy $source to $destination",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error copying file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun copyFile(source: File, destination: File): Boolean {
        return try {
            val destFile = if (destination.isDirectory) {
                File(destination, source.name)
            } else {
                destination
            }
            
            destFile.parentFile?.mkdirs()
            source.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun copyDirectory(source: File, destination: File): Boolean {
        return try {
            val destDir = if (destination.exists() && destination.isDirectory) {
                File(destination, source.name)
            } else {
                destination
            }
            
            destDir.mkdirs()
            
            source.listFiles()?.forEach { file ->
                val destFile = File(destDir, file.name)
                if (file.isDirectory) {
                    copyDirectory(file, destFile)
                } else {
                    copyFile(file, destFile)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

class MoveCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val source = parameters["source"] ?: arguments.getOrNull(0)
        val destination = parameters["destination"] ?: arguments.getOrNull(1)
        
        if (source == null || destination == null) {
            return CommandResult(
                success = false,
                output = "Both source and destination are required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val sourceFile = File(source)
            val destFile = File(destination)
            
            if (!sourceFile.exists()) {
                return CommandResult(
                    success = false,
                    output = "Source file not found: $source",
                    executionTimeMs = 0
                )
            }
            
            val finalDest = if (destFile.isDirectory) {
                File(destFile, sourceFile.name)
            } else {
                destFile
            }
            
            finalDest.parentFile?.mkdirs()
            
            val success = sourceFile.renameTo(finalDest)
            
            if (success) {
                CommandResult(
                    success = true,
                    output = "Successfully moved $source to ${finalDest.absolutePath}",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Failed to move $source to $destination",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error moving file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class RemoveCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: arguments.firstOrNull()
        val recursive = parameters["recursive"]?.toBoolean() ?: false
        val force = parameters["force"]?.toBoolean() ?: false
        
        if (path == null) {
            return CommandResult(
                success = false,
                output = "Path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(path)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $path",
                    executionTimeMs = 0
                )
            }
            
            val success = if (file.isDirectory) {
                if (!recursive) {
                    return CommandResult(
                        success = false,
                        output = "Use --recursive to remove directories",
                        executionTimeMs = 0
                    )
                }
                file.deleteRecursively()
            } else {
                file.delete()
            }
            
            if (success) {
                CommandResult(
                    success = true,
                    output = "Successfully removed $path",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Failed to remove $path",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error removing file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class MkdirCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: arguments.firstOrNull()
        val parents = parameters["parents"]?.toBoolean() ?: false
        
        if (path == null) {
            return CommandResult(
                success = false,
                output = "Path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val directory = File(path)
            
            if (directory.exists()) {
                return CommandResult(
                    success = false,
                    output = "Directory already exists: $path",
                    executionTimeMs = 0
                )
            }
            
            val success = if (parents) {
                directory.mkdirs()
            } else {
                directory.mkdir()
            }
            
            if (success) {
                CommandResult(
                    success = true,
                    output = "Successfully created directory: $path",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Failed to create directory: $path",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error creating directory: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class CatCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val filePath = parameters["file"] ?: arguments.firstOrNull()
        val maxLines = parameters["lines"]?.toIntOrNull() ?: 1000
        
        if (filePath == null) {
            return CommandResult(
                success = false,
                output = "File path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (!file.isFile) {
                return CommandResult(
                    success = false,
                    output = "Not a file: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (file.length() > 1024 * 1024) { // 1MB limit
                return CommandResult(
                    success = false,
                    output = "File too large to display (>1MB): $filePath",
                    executionTimeMs = 0
                )
            }
            
            val content = file.readLines().take(maxLines).joinToString("\n")
            
            CommandResult(
                success = true,
                output = content,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error reading file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class PwdCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        return try {
            val currentDir = Environment.getExternalStorageDirectory().absolutePath
            CommandResult(
                success = true,
                output = "Current working directory: $currentDir",
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error getting current directory: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class TouchCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val filePath = parameters["file"] ?: arguments.firstOrNull()
        val createDirs = parameters["create-dirs"]?.toBoolean() ?: false
        
        if (filePath == null) {
            return CommandResult(
                success = false,
                output = "File path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (createDirs) {
                file.parentFile?.mkdirs()
            }
            
            val success = if (file.exists()) {
                // Update timestamp for existing file
                file.setLastModified(System.currentTimeMillis())
                true
            } else {
                // Create new empty file
                file.createNewFile()
            }
            
            if (success) {
                val action = if (file.exists()) "Updated" else "Created"
                CommandResult(
                    success = true,
                    output = "$action file: $filePath",
                    executionTimeMs = 0
                )
            } else {
                CommandResult(
                    success = false,
                    output = "Failed to touch file: $filePath",
                    executionTimeMs = 0
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error touching file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class FindCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: arguments.firstOrNull()
        val namePattern = parameters["name"]
        val typeFilter = parameters["type"]
        val sizeFilter = parameters["size"]
        val limit = parameters["limit"]?.toIntOrNull() ?: 100
        
        if (path == null) {
            return CommandResult(
                success = false,
                output = "Path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val directory = File(path)
            
            if (!directory.exists()) {
                return CommandResult(
                    success = false,
                    output = "Directory not found: $path",
                    executionTimeMs = 0
                )
            }
            
            val results = findFiles(directory, namePattern, typeFilter, sizeFilter, limit)
            
            val output = buildString {
                appendLine("Found ${results.size} matches in $path:")
                appendLine()
                results.forEach { file ->
                    val type = if (file.isDirectory) "d" else "f"
                    val size = if (file.isFile) formatFileSize(file.length()) else "<DIR>"
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(file.lastModified()))
                    
                    appendLine("$type  ${size.padEnd(10)}  $date  ${file.absolutePath}")
                }
                
                if (results.size >= limit) {
                    appendLine()
                    appendLine("(Limited to $limit results)")
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error searching files: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun findFiles(
        directory: File,
        namePattern: String?,
        typeFilter: String?,
        sizeFilter: String?,
        limit: Int
    ): List<File> {
        val results = mutableListOf<File>()
        
        fun searchRecursively(dir: File) {
            if (results.size >= limit) return
            
            try {
                dir.listFiles()?.forEach { file ->
                    if (results.size >= limit) return
                    
                    var matches = true
                    
                    // Name pattern matching
                    if (namePattern != null) {
                        val regex = namePattern.replace("*", ".*").replace("?", ".").toRegex(RegexOption.IGNORE_CASE)
                        matches = matches && regex.matches(file.name)
                    }
                    
                    // Type filtering
                    if (typeFilter != null) {
                        matches = matches && when (typeFilter) {
                            "f" -> file.isFile
                            "d" -> file.isDirectory
                            else -> true
                        }
                    }
                    
                    // Size filtering
                    if (sizeFilter != null && file.isFile) {
                        matches = matches && matchesSize(file.length(), sizeFilter)
                    }
                    
                    if (matches) {
                        results.add(file)
                    }
                    
                    if (file.isDirectory) {
                        searchRecursively(file)
                    }
                }
            } catch (e: Exception) {
                // Skip directories we can't read
            }
        }
        
        searchRecursively(directory)
        return results
    }
    
    private fun matchesSize(fileSize: Long, sizeFilter: String): Boolean {
        try {
            val operator = if (sizeFilter.startsWith("+")) "+" else if (sizeFilter.startsWith("-")) "-" else "="
            val sizeStr = sizeFilter.drop(if (operator != "=") 1 else 0)
            
            val multiplier = when (sizeStr.last().lowercaseChar()) {
                'k' -> 1024L
                'm' -> 1024L * 1024L
                'g' -> 1024L * 1024L * 1024L
                else -> 1L
            }
            
            val sizeValue = sizeStr.dropLast(if (sizeStr.last().isLetter()) 1 else 0).toLong() * multiplier
            
            return when (operator) {
                "+" -> fileSize > sizeValue
                "-" -> fileSize < sizeValue
                else -> fileSize == sizeValue
            }
        } catch (e: Exception) {
            return true
        }
    }
    
    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = size.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", value, units[unitIndex])
    }
}

class GrepCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val pattern = parameters["pattern"] ?: arguments.getOrNull(0)
        val filePath = parameters["file"] ?: arguments.getOrNull(1)
        val ignoreCase = parameters["ignore-case"]?.toBoolean() ?: false
        val showLineNumbers = parameters["line-numbers"]?.toBoolean() ?: false
        val onlyCount = parameters["count"]?.toBoolean() ?: false
        
        if (pattern == null || filePath == null) {
            return CommandResult(
                success = false,
                output = "Pattern and file path are required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (!file.isFile) {
                return CommandResult(
                    success = false,
                    output = "Not a file: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (file.length() > 10 * 1024 * 1024) { // 10MB limit
                return CommandResult(
                    success = false,
                    output = "File too large for grep (>10MB): $filePath",
                    executionTimeMs = 0
                )
            }
            
            val regexOptions = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
            val regex = pattern.toRegex(regexOptions)
            
            val lines = file.readLines()
            val matches = mutableListOf<Pair<Int, String>>()
            
            lines.forEachIndexed { index, line ->
                if (regex.containsMatchIn(line)) {
                    matches.add(index + 1 to line)
                }
            }
            
            val output = if (onlyCount) {
                matches.size.toString()
            } else {
                buildString {
                    appendLine("Searching for '$pattern' in $filePath:")
                    appendLine()
                    
                    if (matches.isEmpty()) {
                        appendLine("No matches found")
                    } else {
                        matches.forEach { (lineNumber, line) ->
                            if (showLineNumbers) {
                                appendLine("$lineNumber: $line")
                            } else {
                                appendLine(line)
                            }
                        }
                        appendLine()
                        appendLine("Found ${matches.size} matches")
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error searching file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class StatCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val path = parameters["path"] ?: arguments.firstOrNull()
        
        if (path == null) {
            return CommandResult(
                success = false,
                output = "Path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(path)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $path",
                    executionTimeMs = 0
                )
            }
            
            val output = buildString {
                appendLine("File: ${file.absolutePath}")
                appendLine("Type: ${if (file.isDirectory) "Directory" else "File"}")
                appendLine("Size: ${if (file.isFile) formatFileSize(file.length()) else calculateDirectorySize(file)}")
                appendLine("Permissions: ${getPermissionsString(file)}")
                appendLine("Modified: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified()))}")
                appendLine("Readable: ${file.canRead()}")
                appendLine("Writable: ${file.canWrite()}")
                appendLine("Executable: ${file.canExecute()}")
                
                if (file.isDirectory) {
                    val children = file.listFiles()?.size ?: 0
                    appendLine("Children: $children")
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error getting file stats: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
    
    private fun calculateDirectorySize(directory: File): String {
        var size = 0L
        var count = 0
        
        fun calculateRecursively(dir: File) {
            try {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        size += file.length()
                        count++
                    } else if (file.isDirectory) {
                        calculateRecursively(file)
                    }
                }
            } catch (e: Exception) {
                // Skip directories we can't read
            }
        }
        
        calculateRecursively(directory)
        return "${formatFileSize(size)} ($count files)"
    }
    
    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = size.toDouble()
        var unitIndex = 0
        
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", value, units[unitIndex])
    }
    
    private fun getPermissionsString(file: File): String {
        return buildString {
            append(if (file.isDirectory) "d" else "-")
            append(if (file.canRead()) "r" else "-")
            append(if (file.canWrite()) "w" else "-")
            append(if (file.canExecute()) "x" else "-")
            append("------") // Placeholder for group/other permissions
        }
    }
}

class HeadCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val filePath = parameters["file"] ?: arguments.firstOrNull()
        val lines = parameters["lines"]?.toIntOrNull() ?: 10
        
        if (filePath == null) {
            return CommandResult(
                success = false,
                output = "File path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (!file.isFile) {
                return CommandResult(
                    success = false,
                    output = "Not a file: $filePath",
                    executionTimeMs = 0
                )
            }
            
            val content = file.readLines().take(lines).joinToString("\n")
            
            CommandResult(
                success = true,
                output = content,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error reading file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class TailCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val filePath = parameters["file"] ?: arguments.firstOrNull()
        val lines = parameters["lines"]?.toIntOrNull() ?: 10
        
        if (filePath == null) {
            return CommandResult(
                success = false,
                output = "File path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (!file.isFile) {
                return CommandResult(
                    success = false,
                    output = "Not a file: $filePath",
                    executionTimeMs = 0
                )
            }
            
            val allLines = file.readLines()
            val content = allLines.takeLast(lines).joinToString("\n")
            
            CommandResult(
                success = true,
                output = content,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error reading file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}

class WcCommandExecutor : CommandExecutor {
    override suspend fun execute(
        context: Context,
        parameters: Map<String, String>,
        arguments: List<String>
    ): CommandResult {
        val filePath = parameters["file"] ?: arguments.firstOrNull()
        val showLines = parameters["lines"]?.toBoolean() ?: false
        val showWords = parameters["words"]?.toBoolean() ?: false
        val showChars = parameters["chars"]?.toBoolean() ?: false
        
        if (filePath == null) {
            return CommandResult(
                success = false,
                output = "File path is required",
                executionTimeMs = 0
            )
        }
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return CommandResult(
                    success = false,
                    output = "File not found: $filePath",
                    executionTimeMs = 0
                )
            }
            
            if (!file.isFile) {
                return CommandResult(
                    success = false,
                    output = "Not a file: $filePath",
                    executionTimeMs = 0
                )
            }
            
            val content = file.readText()
            val lines = content.lines()
            val words = content.split(Regex("\\s+")).filter { it.isNotEmpty() }
            val chars = content.length
            
            val output = when {
                showLines -> lines.size.toString()
                showWords -> words.size.toString()
                showChars -> chars.toString()
                else -> {
                    buildString {
                        appendLine("File: $filePath")
                        appendLine("Lines: ${lines.size}")
                        appendLine("Words: ${words.size}")
                        appendLine("Characters: $chars")
                    }
                }
            }
            
            CommandResult(
                success = true,
                output = output,
                executionTimeMs = 0
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "Error analyzing file: ${e.message}",
                executionTimeMs = 0
            )
        }
    }
}