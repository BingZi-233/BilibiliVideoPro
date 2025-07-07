package online.bingzi.bilibili.video.pro.internal.database.transaction

import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.error.ErrorHandler
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo

/**
 * 简化的事务管理器
 * 提供基础的事务支持
 */
object SimpleTransactionManager {

    /**
     * 事务执行结果
     */
    sealed class TransactionResult<T> {
        data class Success<T>(val result: T) : TransactionResult<T>()
        data class Failure<T>(val exception: Exception) : TransactionResult<T>()
    }

    /**
     * 执行简单事务
     */
    inline fun <T> executeTransaction(
        crossinline operation: () -> T
    ): TransactionResult<T> {
        return try {
            console().sendInfo("transactionStarted")

            val result = operation()

            console().sendInfo("transactionSuccess")
            TransactionResult.Success(result)

        } catch (e: Exception) {
            console().sendInfo("transactionFailed", e.message ?: "unknown")

            // 记录错误
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.DATABASE,
                component = "TransactionManager",
                operation = "executeTransaction",
                exception = e,
                metadata = mapOf("operation" to "database_transaction")
            )

            TransactionResult.Failure(e)
        }
    }

    /**
     * 批量执行操作
     */
    inline fun <T> executeBatch(
        batchSize: Int = 1000,
        crossinline operation: () -> List<T>
    ): TransactionResult<List<T>> {
        return try {
            console().sendInfo("batchOperationStarted", batchSize.toString())

            val result = operation()

            console().sendInfo("batchOperationCompleted", result.size.toString())
            TransactionResult.Success(result)

        } catch (e: Exception) {
            console().sendInfo("batchOperationFailed", e.message ?: "unknown")

            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.DATABASE,
                component = "TransactionManager",
                operation = "executeBatch",
                exception = e,
                metadata = mapOf(
                    "operation" to "batch_database",
                    "batch_size" to batchSize
                )
            )

            TransactionResult.Failure(e)
        }
    }

    /**
     * 检查数据库连接状态
     */
    fun checkConnectionHealth(): Boolean {
        return try {
            DatabaseManager.isConnectionValid()
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.DATABASE,
                component = "TransactionManager",
                operation = "checkConnectionHealth",
                exception = e
            )
            false
        }
    }
}