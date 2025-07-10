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