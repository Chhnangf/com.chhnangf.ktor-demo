package com.chhnangf.user

import com.chhnangf.units.EncryptSha512Util
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

data class UserAccount(
    @SerialName("id") val id: Int,
    @SerialName("password") val password: String,
    @SerialName("mail") private val _email: String,
    @SerialName("created_at") private val _createDate: String,
) {
    constructor(
        id: Int,
        password: String,
        email: String,
        createDate: Instant,
    ) : this(
        id,
        password,
        email,
        createDate.toString(),
    )

    companion object

}

@Serializable
data class AccountRegisteredStruct (
    val email: String,
    @SerialName("password") private val _password: String,
) {
    val password: String get() = EncryptSha512Util.encrypt(_password)
}

@Serializable
data class AccountSignatureByEmailStruct(val email: String, @SerialName("password") private val _password: String) {
    /**
     * The password is already encrypted by SHA-512
     */
    val password: String get() = EncryptSha512Util.encryptOrNull(_password) ?: UUID.randomUUID().toString()
}

