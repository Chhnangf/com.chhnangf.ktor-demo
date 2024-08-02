package com.chhnangf.user

import com.chhnangf.units.CodableException

class AccountInformationManager(private val serverDaoImpl: ServerDaoImpl) {
    suspend fun register(accountStruct:AccountRegisteredStruct) :Int {
        serverDaoImpl.creat(accountStruct)
        return serverDaoImpl.search(accountStruct)?.id
            ?: throw CodableException(-337, "Feature not supported by unknown gender.")
    }
    suspend fun search(accountStruct: AccountRegisteredStruct):Int {
        return serverDaoImpl.search(accountStruct)?.id
            ?: throw CodableException(-337, "Feature not supported by unknown gender.")
    }
}