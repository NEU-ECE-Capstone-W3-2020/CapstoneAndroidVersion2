package com.example.capstoneandroidversion2.ble

import java.util.*

object BleRepository {

    val messageStack = Stack<String>()

    /**
     * @return whether or not the value was added to the stack
     */
    fun addToStack(newMsg: String): Boolean {
        return if (messageStack.peek().equals(newMsg)) {
            false
        } else {
            messageStack.push(newMsg)
            true
        }
    }

}
