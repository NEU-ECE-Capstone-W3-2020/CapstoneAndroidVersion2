package com.example.capstoneandroidversion2.ble

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

object BusHolder {
    val bus = Bus(ThreadEnforcer.ANY)
}