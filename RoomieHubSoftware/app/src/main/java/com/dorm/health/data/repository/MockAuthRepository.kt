package com.dorm.health.data.repository

import com.dorm.health.data.model.MockUserAccount
import com.dorm.health.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 假用户端认证：演示账号登录，无需真实后端。
 */
class MockAuthRepository(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository,
    private val environmentRepository: EnvironmentRepository
) {
    val demoAccounts: List<MockUserAccount> = listOf(
        MockUserAccount(
            id = "u001",
            username = "zhangsan",
            password = "123456",
            nickname = "张三",
            studentId = "2024001001",
            dormName = "6号楼302",
            dormBuilding = "6号楼",
            bedNumber = "302-A"
        ),
        MockUserAccount(
            id = "u002",
            username = "lisi",
            password = "123456",
            nickname = "李四",
            studentId = "2024001002",
            dormName = "6号楼302",
            dormBuilding = "6号楼",
            bedNumber = "302-B"
        ),
        MockUserAccount(
            id = "u003",
            username = "wangwu",
            password = "123456",
            nickname = "王五",
            studentId = "2024002008",
            dormName = "7号楼105",
            dormBuilding = "7号楼",
            bedNumber = "105-A"
        )
    )

    private val _currentUser = MutableStateFlow<MockUserAccount?>(null)
    val currentUser: StateFlow<MockUserAccount?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(preferencesManager.loggedInUserId != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val userId = preferencesManager.loggedInUserId ?: return
        demoAccounts.find { it.id == userId }?.let { _currentUser.value = it }
    }

    suspend fun login(username: String, password: String): Result<MockUserAccount> {
        val input = username.trim()
        if (input.isBlank()) {
            return Result.failure(IllegalArgumentException("请输入学号或用户名"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("请输入密码"))
        }

        val account = demoAccounts.find {
            it.username.equals(input, ignoreCase = true) ||
                it.studentId.equals(input, ignoreCase = true)
        } ?: return Result.failure(IllegalArgumentException("用户不存在，请选择演示账号或检查输入"))

        if (password != account.password) {
            return Result.failure(IllegalArgumentException("密码错误，演示密码为 123456"))
        }

        applySession(account)
        return Result.success(account)
    }

    suspend fun loginWithDemoAccount(account: MockUserAccount): Result<MockUserAccount> {
        applySession(account)
        return Result.success(account)
    }

    fun logout() {
        preferencesManager.loggedInUserId = null
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    private suspend fun applySession(account: MockUserAccount) {
        preferencesManager.loggedInUserId = account.id
        _currentUser.value = account
        _isLoggedIn.value = true
        userRepository.ensureDefaultUser()
        userRepository.updateNickname(account.nickname)
        environmentRepository.updateDormName(account.dormName)
    }
}
