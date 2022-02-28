package com.rasalexman.erb.ui.base

interface IBaseHost {
    val navControllerId: Int

    fun unbindNavController()
    fun bindNavController()
}