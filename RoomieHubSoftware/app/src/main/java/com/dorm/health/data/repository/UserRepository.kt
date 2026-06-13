package com.dorm.health.data.repository

import com.dorm.health.data.database.dao.UserInfoDao
import com.dorm.health.data.database.entities.UserInfo
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userInfoDao: UserInfoDao) {
    val userInfo: Flow<UserInfo?> = userInfoDao.getUserInfo()

    suspend fun ensureDefaultUser() {
        if (userInfoDao.getUserInfoOnce() == null) {
            userInfoDao.insert(UserInfo())
        }
    }

    suspend fun updateNickname(nickname: String) {
        userInfoDao.getUserInfoOnce()?.let {
            userInfoDao.update(it.copy(nickname = nickname))
        }
    }

    suspend fun updateAvatar(uri: String?) {
        userInfoDao.getUserInfoOnce()?.let {
            userInfoDao.update(it.copy(avatarUri = uri))
        }
    }
}
