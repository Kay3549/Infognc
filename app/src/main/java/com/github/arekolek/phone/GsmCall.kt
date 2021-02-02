package com.github.arekolek.phone

data class GsmCall(val status: Status, val displayName: String?) {

  enum class Status {
    CONNECTING,
    DIALING,
    RINGING,
    ACTIVE,
    DISCONNECTED,
    UNKNOWN
  }
}