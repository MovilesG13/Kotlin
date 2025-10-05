package com.example.monify_kotlin.core.ui

fun Float.formatMoney(): String = "%,.2f".format(this)
fun Double.formatMoney(): String = "%,.2f".format(this)
